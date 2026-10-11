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
 * nGrinder 부하 테스트: POST /api/ai/recommend-tutors
 *
 * 테스트 시나리오:
 *   1. BeforeThread : 각 가상 유저(Thread)가 스레드번호 기반 계정으로 로그인 → JWT 발급
 *   2. @Test        : 다양한 학생 요구사항으로 AI 강사 추천 API 반복 호출
 *
 * nGrinder 설정 권장값 (상황에 맞게 조정):
 *   - 기본    : vUser=10,  duration=3min
 *   - 중간    : vUser=30,  duration=5min
 *   - 스파이크: vUser=100, duration=2min
 */
@RunWith(GrinderRunner)
class RecommendTutorTest {

    // ─────────────────────────────────────────────
    // 설정 상수
    // ─────────────────────────────────────────────

    /** nGrinder Agent가 Docker 내부에서 실행될 경우 host.docker.internal 사용 */
    static final String BASE_URL = "http://host.docker.internal:8080"

    /** 액세스 토큰 유효 시간 (30분) - 서버 설정과 맞출 것 */
    static final long TOKEN_TTL_MS = 30 * 60 * 1000L

    /** GTest: nGrinder 통계 수집 단위 */
    static GTest loginTest
    static GTest recommendTest

    /** 공유 HTTPRequest 객체 */
    static HTTPRequest request

    // ─────────────────────────────────────────────
    // 테스트 계정 풀 (실제 DB에 등록된 계정으로 교체)
    // ─────────────────────────────────────────────

    static final List<Map> TEST_USERS = [
            [username: "student1@test.com", password: "testaccount123"],
            [username: "student2@test.com", password: "testaccount123"],
            [username: "student3@test.com", password: "testaccount123"],
            [username: "student4@test.com", password: "testaccount123"],
            [username: "student5@test.com", password: "testaccount123"],
    ]

    // ─────────────────────────────────────────────
    // 다양한 학생 요구사항 시나리오 (LLM 다양성 테스트)
    // ─────────────────────────────────────────────

    static final List<String> REQUIREMENTS = [
            "피아노를 처음 배우는 성인입니다. 클래식 위주로 가르쳐주시고 서울 강남 지역에서 레슨 가능한 선생님을 찾고 있습니다.",
            "기타 중급자로 재즈와 블루스를 배우고 싶습니다. 홍대 근처에서 주말 레슨 원합니다.",
            "보컬 레슨을 받고 싶은 대학생입니다. 팝, R&B 장르에 관심 있고 온라인 레슨도 괜찮습니다.",
            "드럼을 독학으로 배운 초보입니다. 기초부터 체계적으로 배우고 싶어요. 경기 지역 선생님 선호합니다.",
            "바이올린을 자녀(초등학생)에게 가르쳐 줄 선생님 찾습니다. 입시 경험 있는 분 선호합니다.",
            "우쿨렐레나 기타를 취미로 배우고 싶어요. 부담 없이 즐겁게 배울 수 있는 선생님 원합니다.",
            "색소폰을 처음 배우는 50대 직장인입니다. 주말 오전 레슨 가능한 분 계신가요?",
            "합창단에서 활동 중인데 성악 발성을 더 체계적으로 배우고 싶습니다.",
            "첼로를 배우고 싶은 고등학생입니다. 음대 입시 준비를 도와주실 수 있는 선생님이 필요합니다.",
            "플루트 중급자인데 오케스트라 합주를 위한 앙상블 기법을 배우고 싶습니다. 인천 지역 가능한 분 찾습니다.",
    ]

    // ─────────────────────────────────────────────
    // 스레드 로컬 상태
    // ─────────────────────────────────────────────

    /** 각 가상 유저(Thread)의 JWT 액세스 토큰 */
    String accessToken = null

    /** 토큰 발급 시각 (ms) — 만료 임박 시 자동 재발급 */
    long tokenIssuedAt = 0L

    // ─────────────────────────────────────────────
    // 공통 헤더 헬퍼
    // ─────────────────────────────────────────────

    static NVPair[] jsonHeader() {
        return [new NVPair("Content-Type", "application/json")] as NVPair[]
    }

    NVPair[] authHeader() {
        return [
                new NVPair("Content-Type", "application/json"),
                new NVPair("Authorization", "Bearer ${accessToken}")
        ] as NVPair[]
    }

    // ─────────────────────────────────────────────
    // 프로세스 초기화 (1회)
    // ─────────────────────────────────────────────

    @BeforeProcess
    static void beforeProcess() {
        loginTest     = new GTest(1, "POST /api/auth/login")
        recommendTest = new GTest(2, "POST /api/ai/recommend-tutors")
        request       = new HTTPRequest()

        grinder.logger.info("=== [BeforeProcess] nGrinder AI 추천 부하 테스트 시작 ===")
    }

    // ─────────────────────────────────────────────
    // 스레드 초기화 (가상 유저당 1회)
    // ─────────────────────────────────────────────

    @BeforeThread
    void beforeThread() {
        grinder.logger.info("Thread #${grinder.threadNumber} 초기화 — 로그인 시도")
        login()
    }

    // ─────────────────────────────────────────────
    // 로그인 로직
    // ─────────────────────────────────────────────

    /**
     * 스레드 번호를 기반으로 계정 풀에서 순환 선택하여 로그인.
     * 성공 시 accessToken 및 tokenIssuedAt 갱신.
     */
    void login() {
        int idx  = (int)(grinder.threadNumber % TEST_USERS.size())
        Map user = TEST_USERS[idx]

        loginTest.record(request)

        String body = JsonOutput.toJson([
                username: user.username,
                password: user.password
        ])

        try {
            def res = request.POST("${BASE_URL}/api/auth/login", body.getBytes("UTF-8"), jsonHeader())

            if (res.statusCode == 200) {
                def json      = new JsonSlurper().parseText(res.text)
                accessToken   = json?.accessToken as String
                tokenIssuedAt = System.currentTimeMillis()
                grinder.logger.info("✅ 로그인 성공 | thread=${grinder.threadNumber} user=${user.username}")
            } else {
                grinder.logger.warn("⚠️  로그인 실패 | status=${res.statusCode} user=${user.username}")
                accessToken = null
            }
        } catch (Exception e) {
            grinder.logger.error("❌ 로그인 예외 | thread=${grinder.threadNumber} msg=${e.message}")
            accessToken = null
        }
    }

    /**
     * 토큰이 없거나 TTL의 90% 경과 시 재로그인.
     * @return 유효한 토큰 보유 여부
     */
    boolean ensureValidToken() {
        boolean expired = (System.currentTimeMillis() - tokenIssuedAt) > (TOKEN_TTL_MS * 0.9)
        if (accessToken == null || expired) {
            grinder.logger.info("🔄 토큰 재발급 시도 | thread=${grinder.threadNumber}")
            login()
        }
        return accessToken != null
    }

    // ─────────────────────────────────────────────
    // 메인 테스트: AI 강사 추천 API
    // ─────────────────────────────────────────────

    /**
     * POST /api/ai/recommend-tutors
     *
     * 1) 토큰 유효성 확인 (필요 시 자동 재로그인)
     * 2) 랜덤 요구사항으로 AI 추천 API 호출
     * 3) 응답 상태 코드별 세분화 로깅 및 검증
     */
    @Test
    void testRecommendTutors() {
        // 토큰이 없으면 스킵 (연속 로그인 실패 시 서버 과부하 방지)
        if (!ensureValidToken()) {
            grinder.logger.warn("⛔ 토큰 없음 — 테스트 스킵 | thread=${grinder.threadNumber}")
            return
        }

        recommendTest.record(request)

        // 매 호출마다 다른 요구사항 선택 (LLM 캐싱 우회 / 실사용 패턴 모사)
        String requirement = REQUIREMENTS[new Random().nextInt(REQUIREMENTS.size())]
        String body = JsonOutput.toJson([studentRequirement: requirement])

        long start = System.currentTimeMillis()

        try {
            def res     = request.POST("${BASE_URL}/api/ai/recommend-tutors", body.getBytes("UTF-8"), authHeader())
            long elapsed = System.currentTimeMillis() - start

            switch (res.statusCode) {
                case 200:
                    // 응답 바디 파싱하여 추천 결과 건수 확인
                    def results = new JsonSlurper().parseText(res.text)
                    int count   = results instanceof List ? results.size() : 0
                    grinder.logger.info("✅ 추천 성공 | count=${count}명 elapsed=${elapsed}ms thread=${grinder.threadNumber}")
                    break

                case 204:
                    // 매칭 강사 없음 (정상 케이스)
                    grinder.logger.info("ℹ️  추천 결과 없음 (204) | elapsed=${elapsed}ms")
                    break

                case 401:
                    // 토큰 만료 → 즉시 재발급 후 다음 사이클에서 재시도
                    grinder.logger.warn("🔐 인증 만료(401) — 재로그인 | thread=${grinder.threadNumber}")
                    accessToken = null
                    login()
                    break

                case 429:
                    // 외부 AI API Rate Limit 초과
                    grinder.logger.warn("🚦 Rate Limit(429) — AI API 요청 한도 초과 | elapsed=${elapsed}ms")
                    break

                case 500:
                    grinder.logger.error("❌ 서버 에러(500) | elapsed=${elapsed}ms")
                    break

                default:
                    grinder.logger.error("⚠️  예상치 못한 응답 | status=${res.statusCode} elapsed=${elapsed}ms")
            }

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start
            grinder.logger.error("❌ 예외 발생 | elapsed=${elapsed}ms msg=${e.message}")
        }
    }
}