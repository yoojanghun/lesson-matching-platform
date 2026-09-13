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

@RunWith(GrinderRunner)
class RecommendTutorTest {

    static final String BASE_URL = "http://host.docker.internal:8080"

    static GTest loginTest
    static GTest recommendTest

    static HTTPRequest request

    String accessToken = null

    static final List<Map> TEST_USERS = [
            [username: "student1@test.com", password: "password123"],
            [username: "student2@test.com", password: "password123"],
            [username: "student3@test.com", password: "password123"],
            [username: "student4@test.com", password: "password123"],
            [username: "student5@test.com", password: "password123"],
    ]

    static final List<String> REQUIREMENTS = [
            "피아노를 처음 배우는 성인입니다. 클래식 위주로 가르쳐주시고 서울 강남 지역에서 레슨 가능한 선생님을 찾고 있습니다.",
            "기타 중급자로 재즈와 블루스를 배우고 싶습니다. 홍대 근처에서 주말 레슨 원합니다.",
            "보컬 레슨을 받고 싶은 대학생입니다. 팝, R&B 장르에 관심 있고 온라인 레슨도 괜찮습니다.",
            "드럼을 독학으로 배운 초보입니다. 기초부터 체계적으로 배우고 싶어요. 경기 지역 선생님 선호합니다.",
            "바이올린을 자녀(초등학생)에게 가르쳐 줄 선생님 찾습니다. 입시 경험 있는 분 선호합니다.",
            "우쿨렐레나 기타를 취미로 배우고 싶어요. 부담 없이 즐겁게 배울 수 있는 선생님 원합니다.",
            "색소폰을 처음 배우는 50대 직장인입니다. 주말 오전 레슨 가능한 분 계신가요?",
            "합창단에서 활동 중인데 성악 발성을 더 체계적으로 배우고 싶습니다.",
    ]

    @BeforeProcess
    static void beforeProcess() {
        loginTest = new GTest(1, "POST /api/auth/login")
        recommendTest = new GTest(2, "POST /api/ai/recommend-tutors")

        request = new HTTPRequest()
    }

    @BeforeThread
    void beforeThread() {
        login()
    }

    void login() {
        int userIndex = (grinder.threadNumber % TEST_USERS.size())
        Map user = TEST_USERS[userIndex]

        loginTest.record(request)

        NVPair[] headers = [new NVPair("Content-Type", "application/json")]
        String bodyJson = JsonOutput.toJson([
                username: user.username,
                password: user.password
        ])

        try {
            def response = request.POST("${BASE_URL}/api/auth/login", bodyJson.getBytes("UTF-8"), headers)
            if (response.statusCode == 200) {
                def slurper = new JsonSlurper()
                def data = slurper.parseText(response.text)
                accessToken = data?.accessToken
                grinder.logger.info("로그인 성공 | user: ${user.username}")
            } else {
                grinder.logger.warn("로그인 실패 | status: ${response.statusCode}")
                accessToken = null
            }
        } catch (Exception e) {
            grinder.logger.error("로그인 예외 발생: ${e.message}")
            accessToken = null
        }
    }

    @Test
    void testRecommendTutors() {
        if (accessToken == null) {
            login()
            if (accessToken == null) return
        }

        recommendTest.record(request)

        NVPair[] headers = [
                new NVPair("Content-Type", "application/json"),
                new NVPair("Authorization", "Bearer ${accessToken}")
        ]

        String requirement = REQUIREMENTS[new Random().nextInt(REQUIREMENTS.size())]
        String bodyJson = JsonOutput.toJson([
                studentRequirement: requirement
        ])

        try {
            def response = request.POST("${BASE_URL}/api/ai/recommend-tutors", bodyJson.getBytes("UTF-8"), headers)

            if (response.statusCode == 200) {
                grinder.logger.info("추천 성공 | HTTP 200")
            } else if (response.statusCode == 401) {
                accessToken = null
            } else {
                fail("API 오류: status ${response.statusCode}")
            }
        } catch (Exception e) {
            grinder.logger.error("추천 API 호출 예외: ${e.message}")
        }
    }
}