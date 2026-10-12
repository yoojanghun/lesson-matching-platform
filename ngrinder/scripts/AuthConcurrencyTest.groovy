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
 * nGrinder 부하 테스트 시나리오 8:
 * "회원가입/인증 동시성 테스트 — 중복 체크 Race / 비밀번호 해싱 CPU 부하"
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 시나리오 설명                                                            │
 * │  • 프로모션 시즌 순간 트래픽 스파이크 시뮬레이션                         │
 * │  • 동일 아이디/이메일로 동시 가입 시도 (중복 체크 Race)                 │
 * │  • Bcrypt 해싱 CPU 부하 측정 (CPU-Intensive 작업)                      │
 * │  • 대량 로그인 시 JWT 발급 성능                                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * 회원가입/인증의 치명적 동시성 버그:
 *  1. 아이디/이메일 중복 체크 Race Condition
 *     → Select(중복 조회)와 INSERT 사이 시간 갭
 *     → 동일 아이디로 2개 계정 생성 (Duplicate Key Error)
 *  2. Bcrypt 해싱 CPU 스파이크
 *     → 대량 가입 시 CPU 100% 도달 → 전체 API 응답 멈춤
 *  3. Refresh Token Redis 저장 병목
 *     → Redis Connection Pool 고갈
 *  4. 동시 로그인 시 JWT 발급 부하
 *     → 이벤트/프로모션 시 수천 명 동시 로그인
 *
 * 측정 포인트:
 *  1. TPS (Sign-ups Per Second)          — 회원가입 처리량
 *  2. 중복 계정 방지                     — 동일 아이디 1개만 존재
 *  3. CPU 사용률                         — Bcrypt 해싱으로 인한 CPU 부하
 *  4. Login TPS                          — 로그인 처리량
 *  5. Redis Connection Pool 상태         — 토큰 저장 병목
 *
 * nGrinder 권장 설정:
 *  ┌──────────────┬────────────┬──────────────┬────────────────────────────┐
 *  │ 단계          │ vUser      │ Duration     │ 목적                        │
 *  ├──────────────┼────────────┼──────────────┼────────────────────────────┤
 *  │ Warmup       │ 10         │ 1min         │ JIT 컴파일                  │
 *  │ 기본 부하     │ 50         │ 3min         │ 정상 TPS, CPU 사용률        │
 *  │ 중간 부하     │ 100        │ 3min         │ 중복 체크 Race 검증         │
 *  │ 스파이크       │ 300        │ 2min         │ CPU Throttling, 중복 계정   │
 *  └──────────────┴────────────┴──────────────┴────────────────────────────┘
 *
 * 사전 준비:
 *  1. DB 초기화 (기존 테스트 계정 삭제)
 *  2. Redis 초기화
 *  3. 서버 CPU/메모리 모니터링 준비
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RunWith(GrinderRunner)
class AuthConcurrencyTest {

    // ─────────────────────────────────────────────────────────────────────
    // ★ 테스트 환경 설정
    // ─────────────────────────────────────────────────────────────────────

    static final String BASE_URL = "http://host.docker.internal:8080"

    /**
     * 테스트 모드
     * - SIGNUP_RACE: 동일 아이디로 동시 가입 (중복 체크 Race)
     * - SIGNUP_SPIKE: 대량 가입 (Bcrypt CPU 부하)
     * - LOGIN_SPIKE: 대량 로그인 (JWT 발급 부하)
     * - DUPLICATE_CHECK: 중복 체크 API 부하
     */
    static final String TEST_MODE = "SIGNUP_SPIKE"  // ← 테스트 모드 선택

    /**
     * SIGNUP_RACE 모드: 동일 아이디로 동시 가입 시도
     * → 중복 체크 Race Condition 재현
     */
    static final String DUPLICATE_USERNAME = "race_test_user"
    static final String DUPLICATE_EMAIL = "race_test@test.com"

    /**
     * SIGNUP_RACE 집중도 (%)
     * 80 = 80%의 요청이 동일 아이디로 시도
     */
    static final int RACE_CONCENTRATION = 80

    // ─────────────────────────────────────────────────────────────────────
    // 회원가입 데이터 풀
    // ─────────────────────────────────────────────────────────────────────

    static final List<String> NAMES = [
        "김민준", "이서윤", "박지호", "최수아", "정예준",
        "강하은", "조윤서", "윤지우", "장서연", "임도윤",
        "한예은", "오시우", "신지안", "권민서", "황서준"
    ]

    static final List<String> PHONE_PREFIXES = ["010-1234", "010-2345", "010-3456", "010-4567"]

    static final String DEFAULT_PASSWORD = "testPassword123!"

    // ─────────────────────────────────────────────────────────────────────
    // GTest 통계 수집 단위
    // ─────────────────────────────────────────────────────────────────────
    static GTest checkIdTest
    static GTest checkEmailTest
    static GTest signUpStudentTest
    static GTest signUpTutorTest
    static GTest loginTest
    static HTTPRequest request

    // ─────────────────────────────────────────────────────────────────────
    // 통계 카운터
    // ─────────────────────────────────────────────────────────────────────
    int signUpSuccessCount    = 0  // 201 Created
    int duplicateErrorCount   = 0  // 409/400: 중복 계정
    int loginSuccessCount     = 0  // 200 OK
    int checkIdSuccessCount   = 0  // 중복 체크 성공
    int errorCount            = 0  // 500: DB/Redis 에러
    int cpuWarningCount       = 0  // 응답 > 2000ms (CPU 부하 의심)

    // ─────────────────────────────────────────────────────────────────────
    // 헤더 헬퍼
    // ─────────────────────────────────────────────────────────────────────

    static NVPair[] jsonHeader() {
        return [new NVPair("Content-Type", "application/json")] as NVPair[]
    }

    // ─────────────────────────────────────────────────────────────────────
    // 프로세스 초기화 (전체 1회)
    // ─────────────────────────────────────────────────────────────────────

    @BeforeProcess
    static void beforeProcess() {
        checkIdTest       = new GTest(1, "GET /api/sign-up/check-id")
        checkEmailTest    = new GTest(2, "GET /api/sign-up/check-email")
        signUpStudentTest = new GTest(3, "POST /api/sign-up/student [회원가입]")
        signUpTutorTest   = new GTest(4, "POST /api/sign-up/tutor [회원가입]")
        loginTest         = new GTest(5, "POST /api/auth/login [로그인]")
        request           = new HTTPRequest()

        grinder.logger.info("=" * 80)
        grinder.logger.info("=== [BeforeProcess] 회원가입/인증 동시성 테스트 시작 ===")
        grinder.logger.info("=== 테스트 모드: ${TEST_MODE} ===")
        if (TEST_MODE == "SIGNUP_RACE") {
            grinder.logger.info("=== Race 타겟: ${DUPLICATE_USERNAME} / ${DUPLICATE_EMAIL} ===")
            grinder.logger.info("=== Race 집중도: ${RACE_CONCENTRATION}% ===")
        }
        grinder.logger.info("=" * 80)
    }

    @BeforeThread
    void beforeThread() {
        grinder.logger.info("▶ Thread #${grinder.threadNumber} 초기화")
    }

    // ─────────────────────────────────────────────────────────────────────
    // 랜덤 계정 생성
    // ─────────────────────────────────────────────────────────────────────

    Map generateRandomAccount() {
        long timestamp = System.currentTimeMillis()
        int threadNum = grinder.threadNumber
        int random = new Random().nextInt(10000)

        String username = "user_${threadNum}_${timestamp}_${random}"
        String email = "user${threadNum}_${timestamp}_${random}@test.com"
        String name = NAMES[new Random().nextInt(NAMES.size())]
        String phone = PHONE_PREFIXES[new Random().nextInt(PHONE_PREFIXES.size())] + "-" + 
                       String.format("%04d", new Random().nextInt(10000))

        return [
            username: username,
            email: email,
            password: DEFAULT_PASSWORD,
            name: name,
            phone: phone
        ]
    }

    // ─────────────────────────────────────────────────────────────────────
    // ★ 테스트 1: 아이디 중복 체크 (부하 테스트)
    // GET /api/sign-up/check-id?userId={userId}
    // ─────────────────────────────────────────────────────────────────────

    @Test
    void testCheckIdConcurrency() {
        if (TEST_MODE != "DUPLICATE_CHECK") return

        checkIdTest.record(request)

        // 인기 아이디 vs 랜덤 아이디
        String userId
        int rand = new Random().nextInt(100)
        if (rand < 50) {
            // 50%는 인기 아이디 (중복 체크 집중)
            userId = "popular_user_" + (new Random().nextInt(10))
        } else {
            userId = "user_" + System.currentTimeMillis() + "_" + new Random().nextInt(10000)
        }

        String url = "${BASE_URL}/api/sign-up/check-id?userId=${URLEncoder.encode(userId, 'UTF-8')}"
        long start = System.currentTimeMillis()

        try {
            def res     = request.GET(url, jsonHeader())
            long elapsed = System.currentTimeMillis() - start

            if (res.statusCode == 200) {
                checkIdSuccessCount++
                boolean isDuplicate = new JsonSlurper().parseText(res.text) as boolean
                grinder.logger.info(
                    "✅ 중복 체크 | userId=${userId} | duplicate=${isDuplicate} | " +
                    "elapsed=${elapsed}ms"
                )
            } else {
                errorCount++
                grinder.logger.error("⚠ 중복 체크 실패 | status=${res.statusCode}")
            }
        } catch (Exception e) {
            errorCount++
            grinder.logger.error("❌ 예외 발생 | msg=${e.message}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ★ 테스트 2: 학생 회원가입 (중복 체크 Race + Bcrypt CPU 부하)
    // POST /api/sign-up/student
    // ─────────────────────────────────────────────────────────────────────

    /**
     * 검증 포인트:
     *  - 201 Created: 회원가입 성공
     *  - 409/400: 중복 계정 차단 (아이디/이메일)
     *  - 500: DB Deadlock, Redis Connection Pool 고갈
     *  - 응답 시간 > 2000ms: Bcrypt CPU 부하로 인한 지연
     *
     * Race Condition 시나리오 (SIGNUP_RACE 모드):
     *  1. 100명이 동시에 동일 아이디로 가입 시도
     *  2. Select(중복 조회) 시점에는 모두 "중복 없음"
     *  3. 동시 INSERT → Duplicate Key Error 또는 2개 계정 생성
     *
     * CPU 부하 시나리오 (SIGNUP_SPIKE 모드):
     *  1. 300명이 동시에 회원가입
     *  2. Bcrypt 해싱 (각 요청당 ~200ms CPU 사용)
     *  3. CPU 100% 도달 → 전체 API 응답 멈춤
     */
    @Test
    void testStudentSignUpConcurrency() {
        if (TEST_MODE != "SIGNUP_RACE" && TEST_MODE != "SIGNUP_SPIKE") return

        signUpStudentTest.record(request)

        // 계정 생성 (SIGNUP_RACE 모드 시 동일 계정 사용)
        Map account
        boolean isRaceAttempt = false

        if (TEST_MODE == "SIGNUP_RACE") {
            int rand = new Random().nextInt(100)
            if (rand < RACE_CONCENTRATION) {
                // Race Condition 유발 (동일 계정)
                account = [
                    username: DUPLICATE_USERNAME,
                    email: DUPLICATE_EMAIL,
                    password: DEFAULT_PASSWORD,
                    name: "테스트유저",
                    phone: "010-1234-5678"
                ]
                isRaceAttempt = true
            } else {
                account = generateRandomAccount()
            }
        } else {
            account = generateRandomAccount()
        }

        String body = JsonOutput.toJson([
            userId: account.username,
            username: account.name,
            email: account.email,
            password: account.password,
            phoneNumber: account.phone
        ])

        String url = "${BASE_URL}/api/sign-up/student"
        long start = System.currentTimeMillis()

        try {
            def res     = request.POST(url, body.getBytes("UTF-8"), jsonHeader())
            long elapsed = System.currentTimeMillis() - start

            // ─── Bcrypt CPU 부하 감지 ───
            boolean isSlow = elapsed > 2000
            if (isSlow) {
                cpuWarningCount++
            }

            String raceLabel = isRaceAttempt ? "[🎯 Race 시도]" : ""

            switch (res.statusCode) {
                case 201:
                    // ✅ 회원가입 성공
                    signUpSuccessCount++
                    grinder.logger.info(
                        "✅ 회원가입 성공 ${raceLabel} | username=${account.username} | " +
                        "elapsed=${elapsed}ms | thread=${grinder.threadNumber}" +
                        (isSlow ? " ⚠ CPU 부하 의심! (Bcrypt 해싱 지연)" : "")
                    )
                    break

                case 400:
                case 409:
                    // ⚠ 중복 계정 차단 (정상 동작)
                    duplicateErrorCount++
                    if (isRaceAttempt) {
                        grinder.logger.info(
                            "⚠ 중복 차단(${res.statusCode}) ${raceLabel} [정상] | " +
                            "username=${account.username} | elapsed=${elapsed}ms | " +
                            "body=${res.text?.take(100)}"
                        )
                    } else {
                        grinder.logger.warn(
                            "⚠ 예상치 못한 중복(${res.statusCode}) | username=${account.username}"
                        )
                    }
                    break

                case 500:
                    // ❌ DB Deadlock, Redis Connection Pool 고갈 등
                    errorCount++
                    grinder.logger.error(
                        "❌ 서버 에러(500) ${raceLabel} [동시성 버그 의심!] | " +
                        "username=${account.username} | elapsed=${elapsed}ms | " +
                        "body=${res.text?.take(300)}"
                    )
                    break

                default:
                    grinder.logger.error("⚠ 예상치 못한 응답 | status=${res.statusCode}")
            }

            // ─── 통계 요약 로그 (30회마다 출력) ───
            int total = signUpSuccessCount + duplicateErrorCount + errorCount
            if (total > 0 && total % 30 == 0) {
                grinder.logger.info(
                    "📊 [회원가입 집계] thread=${grinder.threadNumber} | " +
                    "성공(201)=${signUpSuccessCount} | 중복차단=${duplicateErrorCount} | " +
                    "500에러=${errorCount} | CPU경고=${cpuWarningCount}"
                )
            }

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start
            errorCount++
            grinder.logger.error(
                "❌ 예외 발생 | username=${account.username} | elapsed=${elapsed}ms | msg=${e.message}"
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ★ 테스트 3: 대량 로그인 (JWT 발급 부하)
    // POST /api/auth/login
    // ─────────────────────────────────────────────────────────────────────

    /**
     * 검증 포인트:
     *  - 200 OK: 로그인 성공, JWT 발급
     *  - Redis Refresh Token 저장 부하
     *  - Bcrypt 비교 연산 CPU 부하
     *
     * 이벤트/프로모션 시나리오:
     *  - 1000명이 동시에 로그인 시도
     *  - JWT 발급 + Redis 저장 병목
     */
    @Test
    void testLoginSpike() {
        if (TEST_MODE != "LOGIN_SPIKE") return

        loginTest.record(request)

        // 기존 계정으로 로그인 (사전에 생성된 계정 사용)
        // ★ 테스트 전 준비: student1@test.com ~ student100@test.com
        int accountNum = (grinder.threadNumber % 100) + 1
        String username = "student${accountNum}@test.com"
        String password = "testaccount123"

        String body = JsonOutput.toJson([
            username: username,
            password: password
        ])

        String url = "${BASE_URL}/api/auth/login"
        long start = System.currentTimeMillis()

        try {
            def res     = request.POST(url, body.getBytes("UTF-8"), jsonHeader())
            long elapsed = System.currentTimeMillis() - start

            boolean isSlow = elapsed > 1000
            if (isSlow) cpuWarningCount++

            if (res.statusCode == 200) {
                loginSuccessCount++
                def json = new JsonSlurper().parseText(res.text)
                String token = json?.accessToken as String
                grinder.logger.info(
                    "✅ 로그인 성공 | username=${username} | elapsed=${elapsed}ms | " +
                    "thread=${grinder.threadNumber}" +
                    (isSlow ? " ⚠ Redis/Bcrypt 병목 의심" : "")
                )
            } else if (res.statusCode == 401) {
                grinder.logger.warn("⚠ 인증 실패(401) | username=${username}")
            } else {
                errorCount++
                grinder.logger.error("❌ 로그인 에러 | status=${res.statusCode}")
            }

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start
            errorCount++
            grinder.logger.error("❌ 예외 발생 | elapsed=${elapsed}ms | msg=${e.message}")
        }
    }
}
