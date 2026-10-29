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
 * ═══════════════════════════════════════════════════════════════════════════
 *  CacheStampedeTest.groovy
 *
 *  [목적]
 *  - 단일 인기 튜터(tutorId=1) 프로필 조회에 모든 트래픽을 집중
 *  - TTL(15초) 만료 시점에 동시 다발적인 Cache Miss로 인한 DB 동시 쿼리(Stampede) 재현
 *
 *  [측정 방법]
 *  1. Spring Boot 서버 로그 실시간 확인:
 *     tail -f app.log | grep "CACHE MISS"
 *     → 15초 주기 만료 시점에 같은 tutorId=1로 [CACHE MISS] 로그가 수~수십 개 동시 출력되는지 확인
 *  2. nGrinder 로그에서 만료 시점의 응답 시간 스파이크 확인 (SLOW_THRESHOLD_MS 초과 시 WARN 기록)
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RunWith(GrinderRunner)
class CacheStampedeTest {

    static final String BASE_URL = "http://13.124.17.19:8080"
    static final Long TARGET_TUTOR_ID = 1L
    static final int SLOW_THRESHOLD_MS = 50   // 캐시 히트 평균(약 13ms)보다 큰 값 (Stampede 순간 감지)

    static GTest profileTest
    static HTTPRequest request

    @BeforeProcess
    static void beforeProcess() {
        HTTPPluginControl.getConnectionDefaults().timeout = 3000
        profileTest = new GTest(1, "GET /api/tutors/{id}/profile [Stampede]")
        request = new HTTPRequest()
        profileTest.record(request)
    }

    @BeforeThread
    void beforeThread() {
        grinder.statistics.delayReports = true
    }

    @Test
    void testCacheStampede() {
        String url = "${BASE_URL}/api/tutors/${TARGET_TUTOR_ID}/profile"
        NVPair[] headers = [new NVPair("Accept", "application/json")] as NVPair[]
        long start = System.currentTimeMillis()

        try {
            def res = request.GET(url, null, headers)
            long elapsed = System.currentTimeMillis() - start

            if (res.statusCode != 200) {
                grinder.logger.error("요청 실패: status=${res.statusCode} | elapsed=${elapsed}ms")
            } else if (elapsed > SLOW_THRESHOLD_MS) {
                // 표준 SimpleDateFormat으로 시간 포맷팅 (Groovy 런타임 호환성 보장)
                String now = new java.text.SimpleDateFormat("HH:mm:ss.SSS").format(new Date())
                grinder.logger.warn("⚡ 지연 | ${now} | elapsed=${elapsed}ms | thread=${grinder.threadNumber}")
            }
        } catch (Exception e) {
            grinder.logger.error("예외 발생: ${e.message}")
        }
    }
}
