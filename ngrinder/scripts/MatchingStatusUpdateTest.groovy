import static net.grinder.script.Grinder.grinder
import net.grinder.script.GTest
import net.grinder.scriptengine.groovy.junit.GrinderRunner
import net.grinder.scriptengine.groovy.junit.annotation.BeforeThread
import net.grinder.scriptengine.groovy.junit.annotation.BeforeProcess
import org.junit.Test
import org.junit.runner.RunWith

import net.grinder.plugin.http.HTTPRequest
import HTTPClient.NVPair
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * nGrinder 부하 테스트 시나리오 2:
 * "수락/거절 동시 처리 테스트 — 상태 변경 원자성 / TPS / 알림 병목 검증"
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 시나리오 설명                                                            │
 * │  Phase 1 (BeforeThread): 학생들이 미리 PENDING 매칭을 N개 생성함         │
 * │  Phase 2 (@Test):        튜터 VUser가 PENDING 목록을 동시에              │
 * │                          ACCEPT / REJECT API 호출 → 상태 원자성 검증     │
 * │                          SMS 알림 발행 병목(RabbitMQ) 동시 측정           │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * 측정 포인트:
 *  1. PATCH TPS          — 튜터의 수락/거절 처리량
 *  2. 상태 전이 정합성   — PENDING → ACCEPTED/REJECTED 중복 전이 없는지 확인
 *  3. SMS 알림 지연      — RabbitMQ Consumer 처리 지연 (로그 타임스탬프 비교)
 *  4. Thread Pool 고갈   — 500 에러 급증 시 확인
 *  5. DB Lock 대기       — 비관적 락 사용 시 Latency 급증 여부
 *
 * nGrinder 권장 설정:
 *  ┌──────────────┬────────────┬──────────────┬────────────────────────────┐
 *  │ 단계          │ vUser      │ Duration     │ 목적                        │
 *  ├──────────────┼────────────┼──────────────┼────────────────────────────┤
 *  │ 기본         │ 10 튜터    │ 3min         │ 정상 TPS / 알림 지연 측정   │
 *  │ 중간 부하    │ 30 튜터    │ 3min         │ Lock 경합 / 알림 큐 지연    │
 *  │ 스파이크      │ 100 튜터   │ 2min         │ Thread Pool / DLX 발생 여부 │
 *  └──────────────┴────────────┴──────────────┴────────────────────────────┘
 *
 * 사전 준비:
 *  1. PENDING 상태의 매칭이 충분히 존재해야 함
 *     → MatchingConcurrencyTest.groovy 를 먼저 실행하거나
 *     → data.sql 에 대량 INSERT 후 테스트
 *  2. TUTOR 계정 풀: tutor1@test.com ~ tutorN@test.com
 *  3. 각 튜터는 자신의 matchingId 목록만 처리 가능
 *     → 테스트 전 DB 조회로 tutor별 PENDING matchingId 목록을 확인할 것
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RunWith(GrinderRunner)
class MatchingStatusUpdateTest {

    // ─────────────────────────────────────────────────────────────────────
    // ★ 테스트 환경 설정 (실제 환경에 맞게 수정 필요)
    // ─────────────────────────────────────────────────────────────────────

    static final String BASE_URL    = "http://13.125.58.106:8080"
    static final long TOKEN_TTL_MS  = 15 * 60 * 1000L  // 15분

    /**
     * ACCEPT / REJECT 비율 설정 (퍼센트)
     * 예: ACCEPT_RATIO = 70 → 70% 수락, 30% 거절
     */
    static final int ACCEPT_RATIO = 70

    // ─────────────────────────────────────────────────────────────────────
    // 튜터 계정 풀 (TUTOR 역할 JWT 사용)
    // 각 튜터 계정에 PENDING 매칭이 있어야 함
    // ─────────────────────────────────────────────────────────────────────
    static final List<Map> TUTOR_USERS = [
        [username: "tutor1@test.com",  password: "testaccount123"],
        [username: "tutor2@test.com",  password: "testaccount123"],
        [username: "tutor3@test.com",  password: "testaccount123"],
        [username: "tutor4@test.com",  password: "testaccount123"],
        [username: "tutor5@test.com",  password: "testaccount123"],
        [username: "tutor6@test.com",  password: "testaccount123"],
        [username: "tutor7@test.com",  password: "testaccount123"],
        [username: "tutor8@test.com",  password: "testaccount123"],
        [username: "tutor9@test.com",  password: "testaccount123"],
        [username: "tutor10@test.com", password: "testaccount123"],
    ]

    /**
     * 각 튜터의 PENDING matchingId 목록
     * ★ 테스트 전 아래 SQL로 조회 후 실제 ID로 교체:
     *   SELECT m.matching_id, ta.tutor_id
     *   FROM matching m
     *   JOIN tutor_account ta ON m.tutor_id = ta.tutor_id
     *   WHERE m.status = 'PENDING'
     *   ORDER BY ta.tutor_id, m.matching_id;
     */
    static final Map<String, List<Long>> TUTOR_PENDING_MATCHING_IDS = [
        "tutor1@test.com":  (1L..50L)   as List,
        "tutor2@test.com":  (51L..100L) as List,
        "tutor3@test.com":  (101L..150L) as List,
        "tutor4@test.com":  (151L..200L) as List,
        "tutor5@test.com":  (201L..250L) as List,
        "tutor6@test.com":  (251L..300L) as List,
        "tutor7@test.com":  (301L..350L) as List,
        "tutor8@test.com":  (351L..400L) as List,
        "tutor9@test.com":  (401L..450L) as List,
        "tutor10@test.com": (451L..500L) as List,
    ]

    // ─────────────────────────────────────────────────────────────────────
    // GTest 통계 수집 단위
    // ─────────────────────────────────────────────────────────────────────
    static GTest loginTest
    static GTest acceptTest
    static GTest rejectTest
    static GTest readMatchingListTest
    static HTTPRequest request

    // ─────────────────────────────────────────────────────────────────────
    // 스레드 로컬 상태
    // ─────────────────────────────────────────────────────────────────────
    String accessToken = null
    long   tokenIssuedAt = 0L
    String currentUsername = null

    // 통계 카운터
    int acceptSuccessCount  = 0
    int rejectSuccessCount  = 0
    int conflictCount       = 0  // 409: 이미 처리된 매칭 재처리 시도
    int errorCount          = 0
    int notFoundCount       = 0  // 404: 매칭 없음

    // PENDING matchingId 순환 인덱스 (스레드별)
    int matchingIdxCursor = 0

    // ─────────────────────────────────────────────────────────────────────
    // 헤더 헬퍼
    // ─────────────────────────────────────────────────────────────────────

    static NVPair[] jsonHeader() {
        return [new NVPair("Content-Type", "application/json")] as NVPair[]
    }

    NVPair[] authHeader() {
        return [
            new NVPair("Content-Type", "application/json"),
            new NVPair("Authorization", "Bearer ${accessToken}")
        ] as NVPair[]
    }

    // ─────────────────────────────────────────────────────────────────────
    // 프로세스 초기화 (전체 1회)
    // ─────────────────────────────────────────────────────────────────────

    @BeforeProcess
    static void beforeProcess() {
        loginTest            = new GTest(1, "POST /api/auth/login [tutor]")
        acceptTest           = new GTest(2, "PATCH /api/matchings/{id}/status [ACCEPTED]")
        rejectTest           = new GTest(3, "PATCH /api/matchings/{id}/status [REJECTED]")
        readMatchingListTest = new GTest(4, "GET /api/matchings/tutor/my [목록 조회]")
        request              = new HTTPRequest()

        grinder.logger.info("=" * 60)
        grinder.logger.info("=== [BeforeProcess] 수락/거절 동시 처리 + 알림 병목 테스트 시작 ===")
        grinder.logger.info("=== 수락 비율: ${ACCEPT_RATIO}% / 거절 비율: ${100 - ACCEPT_RATIO}% ===")
        grinder.logger.info("=" * 60)
    }

    // ─────────────────────────────────────────────────────────────────────
    // 스레드 초기화 (VUser당 1회) — 튜터 계정으로 로그인
    // ─────────────────────────────────────────────────────────────────────

    @BeforeThread
    void beforeThread() {
        int idx          = (int)(grinder.threadNumber % TUTOR_USERS.size())
        currentUsername  = TUTOR_USERS[idx].username
        grinder.logger.info("▶ Thread #${grinder.threadNumber} 초기화 — 튜터 로그인: ${currentUsername}")
        login()
    }

    // ─────────────────────────────────────────────────────────────────────
    // 로그인 (TUTOR 역할 JWT 발급)
    // ─────────────────────────────────────────────────────────────────────

    void login() {
        int idx  = (int)(grinder.threadNumber % TUTOR_USERS.size())
        Map user = TUTOR_USERS[idx]
        currentUsername = user.username

        loginTest.record(request)

        String body = JsonOutput.toJson([
            username: user.username,
            password: user.password
        ])

        try {
            def res = request.POST("${BASE_URL}/api/auth/login", body.getBytes("UTF-8"), jsonHeader())

            if (res.statusCode == 200) {
                def json    = new JsonSlurper().parseText(res.text)
                accessToken = json?.accessToken as String
                tokenIssuedAt = System.currentTimeMillis()
                grinder.logger.info("✅ 튜터 로그인 성공 | thread=${grinder.threadNumber} | user=${user.username}")
            } else {
                grinder.logger.warn("⚠ 로그인 실패 | status=${res.statusCode} | user=${user.username}")
                accessToken = null
            }
        } catch (Exception e) {
            grinder.logger.error("❌ 로그인 예외 | thread=${grinder.threadNumber} | msg=${e.message}")
            accessToken = null
        }
    }

    boolean ensureValidToken() {
        boolean expired = (System.currentTimeMillis() - tokenIssuedAt) > (TOKEN_TTL_MS * 0.9)
        if (accessToken == null || expired) {
            login()
        }
        return accessToken != null
    }

    // ─────────────────────────────────────────────────────────────────────
    // 현재 스레드(튜터)의 다음 처리할 matchingId 순환 선택
    // ─────────────────────────────────────────────────────────────────────

    Long getNextMatchingId() {
        List<Long> ids = TUTOR_PENDING_MATCHING_IDS[currentUsername] ?: []
        if (ids.isEmpty()) return null
        Long id = ids[matchingIdxCursor % ids.size()]
        matchingIdxCursor++
        return id
    }

    // ─────────────────────────────────────────────────────────────────────
    // ★ 메인 테스트 1: 매칭 수락/거절 동시 처리
    // PATCH /api/matchings/{matchingId}/status
    // ─────────────────────────────────────────────────────────────────────

    /**
     * 검증 포인트:
     *  - 200 OK: 상태 변경 성공 → SMS 알림 비동기 발행됨 (RabbitMQ로 전달)
     *  - 409: 이미 처리된 매칭에 재시도 (중복 처리 방지 로직 확인)
     *  - 500: DB Lock Timeout, Deadlock → 비관적/낙관적 락 검토 필요
     *
     * 동시성 검증 핵심:
     *  동일 matchingId에 대해 2개의 스레드가 동시에 ACCEPTED를 보낼 때
     *  하나만 성공하고 나머지는 거부되어야 함 (상태 멱등성)
     *
     * SMS 알림 병목 검증:
     *  200 성공 직후 RabbitMQ 로그에서 Consumer 처리 지연 타임스탬프 확인
     *  → 정상: 200ms 이내 처리
     *  → 경고: 1000ms 이상 → Thread Pool 고갈 또는 큐 백프레셔 발생
     */
    @Test
    void testConcurrentStatusUpdate() {
        if (!ensureValidToken()) {
            grinder.logger.warn("⛔ 토큰 없음 — 테스트 스킵 | thread=${grinder.threadNumber}")
            return
        }

        Long matchingId = getNextMatchingId()
        if (matchingId == null) {
            grinder.logger.warn("⛔ matchingId 없음 — TUTOR_PENDING_MATCHING_IDS 설정 확인 필요")
            return
        }

        // ACCEPT_RATIO 기반으로 수락/거절 결정
        boolean isAccept = (new Random().nextInt(100) < ACCEPT_RATIO)
        String status    = isAccept ? "ACCEPTED" : "REJECTED"

        // GTest 통계 분리 수집 (수락 / 거절 별도 TPS 확인)
        if (isAccept) {
            acceptTest.record(request)
        } else {
            rejectTest.record(request)
        }

        String body = JsonOutput.toJson([status: status])
        String url  = "${BASE_URL}/api/matchings/${matchingId}/status"

        long start = System.currentTimeMillis()

        try {
            def res     = request.POST(url, body.getBytes("UTF-8"), authHeader())
            long elapsed = System.currentTimeMillis() - start

            switch (res.statusCode) {
                case 200:
                    // ✅ 상태 변경 성공
                    if (isAccept) {
                        acceptSuccessCount++
                        // ★ ACCEPTED 성공 = RabbitMQ SMS 알림 발행됨
                        // → 서버 로그에서 "[SmsProducer] 발행" 타임스탬프와 비교하여 지연 측정
                        grinder.logger.info(
                            "✅ ACCEPTED 성공 | matchingId=${matchingId} | elapsed=${elapsed}ms | " +
                            "thread=${grinder.threadNumber} [SMS 알림 비동기 발행됨]"
                        )
                    } else {
                        rejectSuccessCount++
                        grinder.logger.info(
                            "✅ REJECTED 성공 | matchingId=${matchingId} | elapsed=${elapsed}ms | " +
                            "thread=${grinder.threadNumber}"
                        )
                    }
                    break

                case 404:
                    // 매칭 없음 (matchingId 설정 오류 또는 이미 삭제됨)
                    notFoundCount++
                    grinder.logger.warn(
                        "⚠ 매칭 없음(404) | matchingId=${matchingId} | elapsed=${elapsed}ms | " +
                        "→ TUTOR_PENDING_MATCHING_IDS 값 확인 필요"
                    )
                    break

                case 400:
                case 409:
                    // 이미 처리된 매칭 또는 잘못된 상태 전이
                    conflictCount++
                    grinder.logger.info(
                        "⚠ 상태 전이 거부(${res.statusCode}) | matchingId=${matchingId} | elapsed=${elapsed}ms | " +
                        "→ 정상: 이미 처리된 매칭에 재시도 | body=${res.text?.take(100)}"
                    )
                    break

                case 401:
                    grinder.logger.warn("🔐 인증 만료(401) — 재로그인 | thread=${grinder.threadNumber}")
                    accessToken = null
                    login()
                    break

                case 500:
                    // ❌ DB Lock 충돌, Deadlock, 트랜잭션 충돌 등 동시성 버그 징후
                    errorCount++
                    grinder.logger.error(
                        "❌ 서버 에러(500) [동시성/Lock 버그 의심!] | matchingId=${matchingId} | " +
                        "status=${status} | elapsed=${elapsed}ms | body=${res.text?.take(200)}"
                    )
                    break

                default:
                    grinder.logger.error(
                        "⚠ 예상치 못한 응답 | status=${res.statusCode} | elapsed=${elapsed}ms | " +
                        "matchingId=${matchingId}"
                    )
            }

            // ─── 통계 요약 로그 (20회마다 출력) ───
            int total = acceptSuccessCount + rejectSuccessCount + conflictCount + errorCount + notFoundCount
            if (total > 0 && total % 20 == 0) {
                grinder.logger.info(
                    "📊 [상태변경 집계] thread=${grinder.threadNumber} | " +
                    "수락=${acceptSuccessCount} | 거절=${rejectSuccessCount} | " +
                    "상태전이거부=${conflictCount} | 404=${notFoundCount} | 500에러=${errorCount}"
                )
            }

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start
            errorCount++
            grinder.logger.error(
                "❌ 예외 발생 | matchingId=${matchingId} | elapsed=${elapsed}ms | " +
                "thread=${grinder.threadNumber} | msg=${e.message}"
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ★ 서브 테스트: 튜터 매칭 목록 조회 (읽기 부하 병행 측정)
    // GET /api/matchings/tutor/my
    //
    // 목적: 수락/거절 처리와 동시에 튜터가 자신의 매칭 목록을 조회할 때
    //       읽기 성능 저하(Lock 경합 영향)가 발생하는지 측정
    // ─────────────────────────────────────────────────────────────────────

    @Test
    void testTutorMatchingListRead() {
        if (!ensureValidToken()) return

        readMatchingListTest.record(request)

        String url = "${BASE_URL}/api/matchings/tutor/my?page=0&size=10&sort=createdAt,desc"

        long start = System.currentTimeMillis()

        try {
            def res     = request.GET(url, null, authHeader())
            long elapsed = System.currentTimeMillis() - start

            if (res.statusCode == 200) {
                def json     = new JsonSlurper().parseText(res.text)
                int total    = json?.totalElements ?: 0
                int returned = json?.content?.size() ?: 0
                grinder.logger.info(
                    "📋 매칭 목록 조회 성공 | total=${total} | returned=${returned} | " +
                    "elapsed=${elapsed}ms | thread=${grinder.threadNumber}" +
                    (elapsed > 500 ? " [⚠ 응답 지연! Lock 경합 의심]" : "")
                )
            } else {
                grinder.logger.warn(
                    "⚠ 목록 조회 실패 | status=${res.statusCode} | elapsed=${elapsed}ms"
                )
            }
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start
            grinder.logger.error("❌ 목록 조회 예외 | elapsed=${elapsed}ms | msg=${e.message}")
        }
    }
}
