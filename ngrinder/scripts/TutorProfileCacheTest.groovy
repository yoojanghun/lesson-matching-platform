import static net.grinder.script.Grinder.grinder
import net.grinder.script.GTest
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import net.grinder.plugin.http.HTTPRequest
import net.grinder.plugin.http.HTTPPluginControl
import HTTPClient.NVPair
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ===============================================================================
 *  TutorProfileCacheTest.groovy  --  테스트 B (v3)
 *
 *  [수정 이력]
 *  v1  최초 작성
 *  v2  버그 Fix 1~6 (profileRequest/reviewRequest 분리, INCLUDE_REVIEWS=false,
 *                     HOT/TAIL 티어 구분, warn 레벨, ThreadLocal 등)
 *  v3  (현재) 실제 DB ID 분포에 맞춰 수정
 *    ┌─────────────────────────────────────────────────────────────────────┐
 *    │  [Issue 1] ID 구조가 불연속  (가장 중요)                           │
 *    │  data-perf.sql 분석 결과:                                          │
 *    │    1~17   : COMPLETED 튜터 (소량 고정 ID)                          │
 *    │    18~1000: 존재하지 않음  → 이 범위를 뽑으면 전부 404            │
 *    │    1001~2000: COMPLETED 튜터 1000명 (대량 데이터)                  │
 *    │                                                                     │
 *    │  이전 pickTutorId() 는 TAIL 구간을 11~1000 으로 잡아              │
 *    │  대부분의 TAIL 요청이 404 → 에러율/응답시간 왜곡                  │
 *    │                                                                     │
 *    │  수정 → 실제 존재하는 ID 배열에서 선택                            │
 *    │    HOT  : 1~10  (소량 고정 튜터 중 최상위 10명)                   │
 *    │    TAIL : 1001~2000 (대량 튜터 풀에서 랜덤)                        │
 *    └─────────────────────────────────────────────────────────────────────┘
 *    ┌─────────────────────────────────────────────────────────────────────┐
 *    │  [Issue 2] X-Cache 헤더 null 시 무음(無音) 처리                    │
 *    │  xCache == null 이면 HIT/MISS 아무것도 기록되지 않아               │
 *    │  "미스가 없다"고 착각하기 쉬움.                                    │
 *    │  → null 일 때 1회만 warn 출력 (AtomicBoolean 로 중복 방지)         │
 *    └─────────────────────────────────────────────────────────────────────┘
 *    ┌─────────────────────────────────────────────────────────────────────┐
 *    │  [Issue 3] ThreadLocal.withInitial({ }) Groovy 클로저 위험         │
 *    │  nGrinder 의 Groovy 버전에 따라 Supplier 변환이 실패 가능          │
 *    │  → 익명 클래스로 교체                                               │
 *    └─────────────────────────────────────────────────────────────────────┘
 *
 *  [실행 전 체크리스트]
 *  [ ] BASE_URL 을 실제 서버 주소로 변경
 *  [ ] HOT_IDS  / TAIL_MIN / TAIL_MAX 를 실제 DB 기준으로 확인
 *        SELECT MIN(tutor_id), MAX(tutor_id), COUNT(*)
 *        FROM tutor_account WHERE profile_status = 'COMPLETED';
 *  [ ] SINGLE_HOT_KEY_MODE: 단일 핫키=true, 편중 분포=false
 *  [ ] INCLUDE_REVIEWS: 리뷰 캐시 구현 완료 후 true
 *  [ ] Redis flushdb, redis-cli config resetstat
 *  [ ] nGrinder: vuser=200, Duration=5min, Ramp-up 없이 즉시 최대 동접
 *  [ ] Thread.sleep(150) 서비스 코드에서 제거 확인
 *  [ ] X-Cache 헤더 사전 확인:
 *        curl -s -D - -o /dev/null http://13.124.17.19:8080/api/tutors/1/profile | grep -i x-cache
 * ===============================================================================
 */
@RunWith(GrinderRunner)
class TutorProfileCacheTest {

    // ─── 설정값 ────────────────────────────────────────────────────────────────

    /** 서버 주소 */
    static final String BASE_URL = "http://3.36.100.66:8080"

    /**
     * true  : tutorId=1 고정 (단일 핫키 최악의 경우)
     * false : 편중 분포 (HOT 80%, TAIL 20%)
     */
    static final boolean SINGLE_HOT_KEY_MODE = false

    // ─── [Issue 1 수정] 실제 DB ID 분포에 맞춘 HOT / TAIL 구간 ───────────────
    //
    //  data-perf.sql 실제 구조:
    //    tutor_id 1~17   : COMPLETED (소량 고정)
    //    tutor_id 18~1000: 존재하지 않음 (이 범위를 요청하면 404)
    //    tutor_id 1001~2000: COMPLETED 1000명 (대량 데이터)
    //
    //  HOT: 1~10 (최상위 10명, 실제 존재)
    //  TAIL: 1001~2000 (대량 풀, 실제 존재)
    //
    //  필요 시 아래 두 값을 DB 조회 결과로 보정하세요:
    //    SELECT MIN(tutor_id), MAX(tutor_id) FROM tutor_account
    //    WHERE profile_status = 'COMPLETED' AND tutor_id >= 1001;

    /** HOT 구간: 실제 존재하는 인기 튜터 ID 배열 */
    static final long[] HOT_IDS = [1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L] as long[]

    /** TAIL 구간: 대량 튜터 ID 범위 (1001~2000) */
    static final long TAIL_MIN = 1001L
    static final long TAIL_MAX = 2000L

    /**
     * 응답 지연 경보 임계값(ms).
     * Thread.sleep(150) 제거 후 30~50ms 권장.
     * 아직 제거 전이면 200ms 이상으로 설정.
     */
    static final int SLOW_THRESHOLD_MS = 50

    /**
     * 프로필 조회 포함 여부.
     * ★ 리뷰 단독 Stampede 측정 시 false 로 설정하여 프로필 호출 부하/로그 오염 방지
     */
    static final boolean INCLUDE_PROFILE = true

    /**
     * 리뷰 조회 포함 여부.
     * ★ 리뷰 Stampede 측정 시 true 로 전환 (단독 측정 시 INCLUDE_PROFILE=false 병행 권장)
     */
    static final boolean INCLUDE_REVIEWS = false

    /** 리뷰 페이지 크기 (기본값 5) */
    static final int REVIEW_PAGE_SIZE = 5

    // ─── nGrinder 내부 객체 ────────────────────────────────────────────────────

    static GTest       profileTest
    static GTest       reviewTest
    static HTTPRequest profileRequest
    static HTTPRequest reviewRequest

    // ─── 집계 카운터 ───────────────────────────────────────────────────────────

    /** 프로세스 단위 HIT/MISS 카운터 (멀티 프로세스 합산 아님) */
    static final java.util.concurrent.atomic.AtomicLong HIT_COUNT  = new java.util.concurrent.atomic.AtomicLong()
    static final java.util.concurrent.atomic.AtomicLong MISS_COUNT = new java.util.concurrent.atomic.AtomicLong()

    // ─── [Issue 2] X-Cache null 경고 1회 한정 ─────────────────────────────────
    /** X-Cache 헤더가 없다는 경고를 1회만 출력하기 위한 플래그 */
    static final java.util.concurrent.atomic.AtomicBoolean NO_XCACHE_WARNED = new java.util.concurrent.atomic.AtomicBoolean(false)

    // ─── [Issue 3] ThreadLocal 익명 클래스 (Groovy 클로저 → 익명 클래스) ──────
    /**
     * nGrinder 의 Groovy 버전에 따라 ThreadLocal.withInitial({ new Random() }) 의
     * 클로저→Supplier 자동 변환이 실패할 수 있다.
     * 익명 클래스로 작성하면 모든 Groovy/JVM 환경에서 안전하다.
     */
    private static final ThreadLocal<java.util.Random> TL_RANDOM =
        new ThreadLocal<java.util.Random>() {
            @Override
            protected java.util.Random initialValue() {
                return new java.util.Random()
            }
        }

    // ─── 프로세스 초기화 (워커 프로세스당 1회) ────────────────────────────────

    @BeforeProcess
    static void beforeProcess() {
        HTTPPluginControl.getConnectionDefaults().timeout = 5000

        profileTest = new GTest(1, "GET /api/tutors/{id}/profile [Cache]")
        reviewTest  = new GTest(2, "GET /api/tutors/{id}/reviews [Cache]")

        profileRequest = new HTTPRequest()
        reviewRequest  = new HTTPRequest()
        profileTest.record(profileRequest)   // GTest-1 ← profileRequest 전용
        reviewTest.record(reviewRequest)     // GTest-2 ← reviewRequest 전용

        grinder.logger.warn("=== TutorProfileCacheTest START ===")
        grinder.logger.warn("MODE     : " + (SINGLE_HOT_KEY_MODE ? "단일 핫키(tutorId=1 고정)" : "편중 분포 (HOT 80%, TAIL 20%)"))
        grinder.logger.warn("HOT_IDS  : 1~10 (실제 존재 확인됨)")
        grinder.logger.warn("TAIL     : " + TAIL_MIN + "~" + TAIL_MAX + " (data-perf.sql 기준)")
        grinder.logger.warn("PROFILE  : " + (INCLUDE_PROFILE ? "포함" : "제외 (리뷰 Stampede 순수 측정)"))
        grinder.logger.warn("REVIEWS  : " + (INCLUDE_REVIEWS ? "포함" : "제외 (프로필 Stampede 순수 측정)"))
        grinder.logger.warn("SLOW_TH  : >" + SLOW_THRESHOLD_MS + "ms → WARN 기록")
        grinder.logger.warn("━━━ 실행 전 확인 ━━━")
        grinder.logger.warn("[1] X-Cache 헤더 확인:")
        grinder.logger.warn("    curl -s -D - -o /dev/null " + BASE_URL + "/api/tutors/1/profile | grep -i x-cache")
        grinder.logger.warn("[2] 실제 ID 범위 확인 (DB):")
        grinder.logger.warn("    SELECT MIN(tutor_id), MAX(tutor_id), COUNT(*) FROM tutor_account WHERE profile_status = 'COMPLETED';")
        grinder.logger.warn("[3] sync=true 에서 대기 후 응답 → HIT/MISS 헤더 어느 쪽? 서버 로그 기준으로 판정하세요.")
    }

    // ─── 스레드 초기화 ──────────────────────────────────────────────────────────

    @BeforeThread
    void beforeThread() {
        grinder.statistics.delayReports = true
    }

    // ─── 헬퍼: tutorId 선택 ────────────────────────────────────────────────────

    /**
     * [Issue 1 수정] 실제 존재하는 ID 범위에서만 선택
     *
     * HOT (80%): HOT_IDS 배열에서 랜덤 (tutorId 1~10, 실제 COMPLETED 확인)
     * TAIL(20%): TAIL_MIN~TAIL_MAX 범위에서 랜덤 (tutorId 1001~2000)
     *
     * ★ 이전 코드의 TAIL 구간(11~1000)은 대부분 404 → 에러율/응답시간 왜곡
     *    → 1001~2000 으로 수정
     */
    private long pickTutorId() {
        if (SINGLE_HOT_KEY_MODE) return 1L
        def rng = TL_RANDOM.get()
        if (rng.nextInt(100) < 80) {
            // HOT: 실제 존재하는 ID 배열에서 선택
            return HOT_IDS[rng.nextInt(HOT_IDS.length)]
        }
        // TAIL: 1001~2000 범위에서 랜덤
        return TAIL_MIN + rng.nextInt((int)(TAIL_MAX - TAIL_MIN + 1))
    }

    /** tutorId 가 HOT 티어인지 여부 (HOT_IDS 배열 기준) */
    private static String tier(long tutorId) {
        for (long id : HOT_IDS) {
            if (id == tutorId) return "HOT"
        }
        return "TAIL"
    }

    // ─── 테스트 본체 ────────────────────────────────────────────────────────────

    @Test
    void testTutorProfileCache() {
        long tutorId   = pickTutorId()
        String tierTag = tier(tutorId)
        NVPair[] headers = [new NVPair("Accept", "application/json")] as NVPair[]

        // ── 1) 프로필 조회 ── profileRequest(GTest-1) 로 집계 ─────────────────
        if (INCLUDE_PROFILE) {
            String profileUrl = BASE_URL + "/api/tutors/" + tutorId + "/profile"
        long profileStart = System.currentTimeMillis()
        try {
            def profileRes      = profileRequest.GET(profileUrl, null, headers)
            long profileElapsed = System.currentTimeMillis() - profileStart

            if (profileRes.statusCode == 200) {
                String xCache = profileRes.getHeader("X-Cache")

                if (xCache == null) {
                    // [Issue 2 수정] X-Cache 헤더가 없으면 HIT/MISS 집계가 아예 안 됨
                    // → 처음 1회만 경고 출력 (AtomicBoolean 으로 중복 방지)
                    if (NO_XCACHE_WARNED.compareAndSet(false, true)) {
                        grinder.logger.warn(
                            "[NO X-CACHE HEADER] tutorId=" + tutorId +
                            " | 서버가 X-Cache 헤더를 내려주지 않으면 HIT/MISS 집계가 불가합니다." +
                            " | curl -s -D - -o /dev/null " + BASE_URL + "/api/tutors/1/profile | grep -i x-cache"
                        )
                    }
                } else if ("HIT".equalsIgnoreCase(xCache.trim())) {
                    HIT_COUNT.incrementAndGet()
                } else {
                    MISS_COUNT.incrementAndGet()
                    // [Issue 1 수정] HOT(1~10) 의 중복 MISS = Stampede 판정 대상
                    // TAIL(1001~2000) 은 평범한 콜드 미스 → Stampede 아님
                    String now = new java.text.SimpleDateFormat("HH:mm:ss.SSS").format(new Date())
                    grinder.logger.warn(
                        "[CACHE MISS:" + tierTag + "] " + now +
                        " | tutorId=" + tutorId +
                        " | elapsed=" + profileElapsed + "ms" +
                        " | thread=" + grinder.threadNumber
                    )
                }

                if (profileElapsed > SLOW_THRESHOLD_MS) {
                    String now = new java.text.SimpleDateFormat("HH:mm:ss.SSS").format(new Date())
                    grinder.logger.warn(
                        "[SLOW:profile:" + tierTag + "] " + now +
                        " | tutorId=" + tutorId +
                        " | elapsed=" + profileElapsed + "ms" +
                        " | X-Cache=" + xCache +
                        " | thread=" + grinder.threadNumber
                    )
                }

                // [STATS] warn 레벨, 프로세스 단위임을 명시
                // ※ total % 1000 == 0 은 멀티 스레드 동시 통과 가능 → 참고용
                long total = HIT_COUNT.get() + MISS_COUNT.get()
                if (total > 0 && total % 1000 == 0) {
                    long hitPct = HIT_COUNT.get() * 100 / total
                    grinder.logger.warn(
                        "[STATS:process] total=" + total +
                        " | HIT=" + HIT_COUNT.get() + "(" + hitPct + "%)" +
                        " | MISS=" + MISS_COUNT.get() +
                        " | ※ 프로세스 단위 (멀티 프로세스 합산 아님)"
                    )
                }

            } else {
                grinder.logger.error(
                    "[FAIL:profile:" + tierTag + "] tutorId=" + tutorId +
                    " | status=" + profileRes.statusCode +
                    " | elapsed=" + profileElapsed + "ms"
                )
            }
            grinder.statistics.forLastTest.setLong("userLong1", profileElapsed)

        } catch (Exception e) {
            grinder.logger.error("[ERROR:profile:" + tierTag + "] tutorId=" + tutorId + " | " + e.message)
        }
        }

        // ── 2) 리뷰 조회 ── reviewRequest(GTest-2) 로 집계 ───────────────────
        // INCLUDE_REVIEWS=false 기본값: 프로필 Stampede 순수 측정
        // tutorReviews 캐시 구현 완료 후 true 로 전환하세요
        if (INCLUDE_REVIEWS) {
            String reviewUrl = BASE_URL + "/api/tutors/" + tutorId +
                               "/reviews?page=0&size=" + REVIEW_PAGE_SIZE
            long reviewStart = System.currentTimeMillis()
            try {
                def reviewRes      = reviewRequest.GET(reviewUrl, null, headers)
                long reviewElapsed = System.currentTimeMillis() - reviewStart

                if (reviewRes.statusCode == 200) {
                    String xCacheReview = reviewRes.getHeader("X-Cache")

                    if (xCacheReview != null && !"HIT".equalsIgnoreCase(xCacheReview.trim())) {
                        String now = new java.text.SimpleDateFormat("HH:mm:ss.SSS").format(new Date())
                        grinder.logger.warn(
                            "[CACHE MISS:review:" + tierTag + "] " + now +
                            " | tutorId=" + tutorId +
                            " | elapsed=" + reviewElapsed + "ms"
                        )
                    }

                    if (reviewElapsed > SLOW_THRESHOLD_MS) {
                        String now = new java.text.SimpleDateFormat("HH:mm:ss.SSS").format(new Date())
                        grinder.logger.warn(
                            "[SLOW:review:" + tierTag + "] " + now +
                            " | tutorId=" + tutorId +
                            " | elapsed=" + reviewElapsed + "ms" +
                            " | X-Cache=" + xCacheReview
                        )
                    }
                } else {
                    grinder.logger.error(
                        "[FAIL:review:" + tierTag + "] tutorId=" + tutorId +
                        " | status=" + reviewRes.statusCode +
                        " | elapsed=" + reviewElapsed + "ms"
                    )
                }
            } catch (Exception e) {
                grinder.logger.error("[ERROR:review:" + tierTag + "] tutorId=" + tutorId + " | " + e.message)
            }
        }
    }
}
