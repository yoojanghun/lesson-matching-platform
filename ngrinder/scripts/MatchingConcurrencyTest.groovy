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
 * nGrinder 부하 테스트 시나리오 1:
 * "단일 인기 튜터 몰림 테스트 — Race Condition / 동시성 정합성 검증"
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 시나리오 설명                                                            │
 * │  • 1명의 Popular 튜터(POPULAR_TUTOR_ID)에게 N명의 학생 VUser가          │
 * │    동시에 매칭 요청(POST /api/matchings/tutors/{tutorId})을 보냄        │
 * │  • 각 학생은 1명의 튜터에게 1개의 PENDING 요청만 가능해야 함            │
 * │    (existsActiveMatching 중복 방지 로직 검증)                           │
 * │  • Race Condition 발생 시: 동일 학생-튜터 쌍이 DB에 중복 INSERT됨       │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * 측정 포인트:
 *  1. TPS (Transactions Per Second)      — 요청 처리량
 *  2. Mean Latency / 95th Percentile     — 응답 지연
 *  3. 201 Created vs 409/400/500 비율    — 중복 방지 로직 정상 작동 여부
 *  4. DB Deadlock / Lock Wait Timeout    — MySQL Error Log 확인
 *
 * nGrinder 권장 설정:
 *  ┌──────────────┬────────────┬──────────────┬────────────────────────────┐
 *  │ 단계          │ vUser      │ Duration     │ 목적                        │
 *  ├──────────────┼────────────┼──────────────┼────────────────────────────┤
 *  │ Warmup       │ 10         │ 1min         │ JIT 컴파일, 커넥션 풀 열기  │
 *  │ 기본 부하     │ 50         │ 3min         │ 정상 TPS 측정               │
 *  │ 집중 부하     │ 200        │ 3min         │ 병목 / Lock 경합 확인       │
 *  │ 스파이크       │ 500        │ 2min         │ Race Condition 재현         │
 *  └──────────────┴────────────┴──────────────┴────────────────────────────┘
 *
 * 사전 준비:
 *  1. data.sql에 아래 계정이 존재해야 함:
 *     - TUTOR 계정: tutor_popular@test.com (ID = POPULAR_TUTOR_ID)
 *     - STUDENT 계정: student1@test.com ~ student{N}@test.com (최소 vUser 수만큼)
 *  2. 각 학생은 STUDENT 역할의 JWT를 사용 (로그인 후 토큰 자동 획득)
 *  3. 테스트 전 matching 테이블에 해당 학생-튜터 간 기존 데이터 없을 것
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RunWith(GrinderRunner)
class MatchingConcurrencyTest {

    // ─────────────────────────────────────────────────────────────────────
    // ★ 테스트 환경 설정 (실제 환경에 맞게 수정 필요)
    // ─────────────────────────────────────────────────────────────────────

    /** nGrinder Agent → Spring Boot 앱 주소 */
    static final String BASE_URL = "http://13.125.58.106:8080"

    /**
     * 부하를 몰아줄 인기 튜터의 DB ID (PK)
     * → 실행 전 DB에서 SELECT tutor_id FROM tutor_account LIMIT 1; 로 확인
     */
    static final Long POPULAR_TUTOR_ID = 1L

    /** JWT 액세스 토큰 TTL (ms) — application.yml access-token-expiration 과 맞출 것 */
    static final long TOKEN_TTL_MS = 15 * 60 * 1000L  // 15분

    // ─────────────────────────────────────────────────────────────────────
    // 학생 계정 풀 (vUser 수 이상 준비할 것)
    // → data.sql 또는 테스트 DB에 실제로 존재하는 계정이어야 함
    // ─────────────────────────────────────────────────────────────────────
    static final List<Map> STUDENT_USERS = (1..50).collect { i ->
        [username: "student${i}@test.com", password: "testaccount123"]
    }

    // ─────────────────────────────────────────────────────────────────────
    // 요청 메시지 풀 (매 요청마다 다양한 메시지 전달)
    // ─────────────────────────────────────────────────────────────────────
    static final List<String> REQUEST_MESSAGES = [
        "피아노 레슨을 받고 싶습니다. 초보자인데 잘 부탁드립니다.",
        "기타 중급자로 재즈를 배우고 싶습니다.",
        "클래식 피아노 입시 준비를 도와주세요.",
        "취미로 바이올린을 배우고 싶은 직장인입니다.",
        "드럼 기초부터 체계적으로 배우고 싶습니다.",
        "보컬 레슨 문의드립니다. 팝 장르 위주로 부탁드려요.",
        "색소폰을 처음 배워보려 합니다.",
        "첼로 레슨 부탁드립니다. 음대 입시 목표입니다.",
        "플루트를 배우고 싶은 고등학생입니다.",
        "우쿨렐레를 취미로 배우고 싶습니다.",
    ]

    // ─────────────────────────────────────────────────────────────────────
    // GTest 통계 수집 단위
    // ─────────────────────────────────────────────────────────────────────
    static GTest loginTest
    static GTest matchingRequestTest
    static HTTPRequest request

    // ─────────────────────────────────────────────────────────────────────
    // 스레드 로컬 상태
    // ─────────────────────────────────────────────────────────────────────
    String accessToken = null
    long tokenIssuedAt = 0L
    int successCount   = 0  // 201 Created 횟수
    int conflictCount  = 0  // 409/400 횟수 (중복 방지)
    int errorCount     = 0  // 500 등 오류 횟수

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
        loginTest          = new GTest(1, "POST /api/auth/login")
        matchingRequestTest = new GTest(2, "POST /api/matchings/tutors/{tutorId} [동시 요청]")
        request            = new HTTPRequest()

        grinder.logger.info("=" * 60)
        grinder.logger.info("=== [BeforeProcess] 단일 튜터 몰림 동시성 테스트 시작 ===")
        grinder.logger.info("=== 타겟 튜터 ID: ${POPULAR_TUTOR_ID} ===")
        grinder.logger.info("=" * 60)
    }

    // ─────────────────────────────────────────────────────────────────────
    // 스레드 초기화 (VUser당 1회) — 학생 계정으로 로그인
    // ─────────────────────────────────────────────────────────────────────

    @BeforeThread
    void beforeThread() {
        grinder.logger.info("▶ Thread #${grinder.threadNumber} 초기화 — 학생 로그인 시도")
        login()
    }

    // ─────────────────────────────────────────────────────────────────────
    // 로그인 (STUDENT 역할 JWT 발급) — 스레드 초기화 시 1회 호출
    // ─────────────────────────────────────────────────────────────────────

    void login() {
        int idx  = (int)(grinder.threadNumber % STUDENT_USERS.size())
        Map user = STUDENT_USERS[idx]

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
                grinder.logger.info("✅ 로그인 성공 | thread=${grinder.threadNumber} | user=${user.username}")
            } else {
                grinder.logger.error("❌ 로그인 실패 | status=${res.statusCode} | body=${res.text} | user=${user.username}")
                accessToken = null
            }
        } catch (Exception e) {
            grinder.logger.error("❌ 로그인 예외 | thread=${grinder.threadNumber} | msg=${e.message}")
            accessToken = null
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ★ 메인 테스트: 동시 매칭 요청 (Race Condition 검증)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * POST /api/matchings/tutors/{tutorId}
     *
     * 검증 포인트:
     *  - 201 Created  : 매칭 생성 성공
     *  - 409 Conflict : 이미 활성 매칭 존재 (existsActiveMatching 정상 작동)
     *  - 400 Bad Request: 잘못된 요청
     *  - 500 Internal : DB Lock 충돌, Deadlock 등 — 즉시 조사 필요!
     *
     * Race Condition 발생 징후:
     *  - 동일 학생-튜터 쌍으로 201이 2회 이상 발생
     *  - 500 에러가 다수 발생 (Duplicate Key Exception)
     */
    @Test
    void testConcurrentMatchingRequest() {
        if (accessToken == null) {
            // 토큰이 없으면 재로그인 시도
            login()
            if (accessToken == null) {
                grinder.logger.warn("⛔ 토큰 획득 실패 — 테스트 스킵 | thread=${grinder.threadNumber}")
                return
            }
        }

        matchingRequestTest.record(request)

        // 매 호출마다 랜덤 메시지 선택 (캐시 우회 및 실사용 패턴 모사)
        String msg  = REQUEST_MESSAGES[new Random().nextInt(REQUEST_MESSAGES.size())]
        String body = JsonOutput.toJson([requestMsg: msg])
        String url  = "${BASE_URL}/api/matchings/tutors/${POPULAR_TUTOR_ID}"

        long start = System.currentTimeMillis()

        try {
            def res     = request.POST(url, body.getBytes("UTF-8"), authHeader())
            long elapsed = System.currentTimeMillis() - start

            switch (res.statusCode) {
                case 201:
                    // ✅ 정상 생성 — matchingId 파싱하여 로깅
                    successCount++
                    Long matchingId = res.text?.trim()?.toLong()
                    grinder.logger.info(
                        "✅ 매칭 생성 성공 | matchingId=${matchingId} | elapsed=${elapsed}ms | thread=${grinder.threadNumber}"
                    )
                    break

                case 409:
                case 400:
                    // ⚠ 중복 방지 로직 정상 작동 (이미 활성 매칭 존재)
                    conflictCount++
                    grinder.logger.info(
                        "⚠ 중복 방지 ${res.statusCode} | elapsed=${elapsed}ms | thread=${grinder.threadNumber} | body=${res.text}"
                    )
                    break

                case 401:
                    // 토큰 만료 → 재로그인
                    grinder.logger.warn("🔐 인증 만료(401) — 재로그인 | thread=${grinder.threadNumber}")
                    accessToken = null
                    login()
                    break

                case 500:
                    // ❌ 서버 오류 — DB Deadlock, Duplicate Key 등 동시성 버그 징후
                    errorCount++
                    grinder.logger.error(
                        "❌ 서버 에러(500) [Race Condition 의심!] | elapsed=${elapsed}ms | thread=${grinder.threadNumber} | body=${res.text?.take(200)}"
                    )
                    break

                default:
                    grinder.logger.error(
                        "⚠ 예상치 못한 응답 | status=${res.statusCode} | elapsed=${elapsed}ms | thread=${grinder.threadNumber}"
                    )
            }

            // ─── 동시성 정합성 요약 로그 (50회 호출마다 출력) ───
            int total = successCount + conflictCount + errorCount
            if (total > 0 && total % 50 == 0) {
                grinder.logger.info(
                    "📊 [동시성 집계] thread=${grinder.threadNumber} | " +
                    "성공(201)=${successCount} | 중복방지(409/400)=${conflictCount} | 오류(500)=${errorCount}"
                )
            }

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start
            errorCount++
            grinder.logger.error(
                "❌ 예외 발생 | elapsed=${elapsed}ms | thread=${grinder.threadNumber} | msg=${e.message}"
            )
        }
    }
}
