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
 *  MainPageCacheStampedeTest.groovy  --  테스트 A
 *
 *  [목적]
 *  메인 홈페이지의 인기 튜터(trendingTutors) / 루키 튜터(rookieTutors) 캐시에
 *  대해 세 가지 방식을 동일 조건(vuser 200, 5분, 테스트 TTL 15초)에서 순차 실행하고
 *  Stampede 발생 여부 + 방어 효과를 정량적으로 비교한다.
 *
 *  [★ 주의: TTL 설정]
 *  - 현재 운영 기본값: trending 30분, rookie 20분
 *  - Stampede 테스트를 수행할 때는 RedisConfig에서 두 캐시의 TTL을
 *    "15초(테스트용 값)"로 임시 변경한 후 빌드/배포하여 테스트하세요.
 *  - 선갱신(스케줄러) 방식 테스트 시에는 스케줄러 주기를 5초~10초로 설정해야
 *    TTL 15초가 만료되기 전에 선갱신되는 효과를 검증할 수 있습니다.
 *
 *  [측정 지표 및 판정 기준]
 *  1. 주기당 CACHE MISS 건수 & concurrent 최댓값 (★ 서버 로그 기준):
 *       tail -f app.log | grep -E "CACHE MISS:trending|CACHE MISS:rookie"
 *       -> concurrent 최댓값은 nGrinder 리포트가 아닌 서버 로그(concurrent=)로 확인!
 *
 *  2. ★ t=0 (flushdb 직후) 콜드 스타트 미스 분리:
 *       테스트 시작 직후(t=0)에는 캐시가 비어 있어 전원이 동시에 MISS를 찍습니다.
 *       이것은 Stampede가 아닌 콜드 스타트이므로, "주기당 MISS 최대"와 "concurrent 최댓값"은
 *       반드시 2번째 만료 주기(t=15초 이후)부터 측정된 수치로 판정하세요!
 *
 *  3. nGrinder 리포트 지표:
 *       - Mean Test Time (평균 응답시간)
 *       - SLOW 로그 건수 (SLOW_THRESHOLD_MS 초과 시 WARN 기록)
 *       - TPS 및 응답시간 그래프의 15초 주기 스파이크(톱니바퀴) 여부
 * ===============================================================================
 */
@RunWith(GrinderRunner)
class MainPageCacheStampedeTest {

    // ─── 설정값 ────────────────────────────────────────────────────────────────

    static final String BASE_URL = "http://3.36.100.66:8080"
    static final int SLOW_THRESHOLD_MS = 50

    // ─── nGrinder 내부 객체 ────────────────────────────────────────────────────

    static GTest       homeTest
    static HTTPRequest request

    @BeforeProcess
    static void beforeProcess() {
        // 첫 콜드 스타트 DB 집계 쿼리 및 네트워크 지연을 감안하여 타임아웃 10초로 설정
        HTTPPluginControl.getConnectionDefaults().timeout = 10000

        homeTest = new GTest(1, "GET /api/main/home [Dual Cache Stampede]")
        request  = new HTTPRequest()
        homeTest.record(request)

        grinder.logger.warn("=== MainPageCacheStampedeTest START ===")
        grinder.logger.warn("TARGET : " + BASE_URL + "/api/main/home")
        grinder.logger.warn("SLOW_TH: >" + SLOW_THRESHOLD_MS + "ms → WARN 기록")
        grinder.logger.warn("※ concurrent 최댓값 및 주기당 MISS는 서버 로그(grep 'CACHE MISS') 기준으로 판정하세요.")
        grinder.logger.warn("※ t=0 콜드 스타트 미스는 제외하고 2번째 주기 이후의 값으로 기록하세요.")
    }

    @BeforeThread
    void beforeThread() {
        grinder.statistics.delayReports = true
    }

    @Test
    void testMainHomeCacheStampede() {
        String homeUrl = BASE_URL + "/api/main/home"
        NVPair[] headers = [new NVPair("Accept", "application/json")] as NVPair[]

        long start = System.currentTimeMillis()
        try {
            def res = request.GET(homeUrl, null, headers)
            long elapsed = System.currentTimeMillis() - start

            if (res.statusCode == 200) {
                if (elapsed > SLOW_THRESHOLD_MS) {
                    String now = new java.text.SimpleDateFormat("HH:mm:ss.SSS").format(new Date())
                    grinder.logger.warn(
                        "[SLOW:home] " + now +
                        " | elapsed=" + elapsed + "ms" +
                        " | thread=" + grinder.threadNumber
                    )
                }
            } else {
                grinder.logger.error(
                    "[FAIL:home] status=" + res.statusCode +
                    " | elapsed=" + elapsed + "ms"
                )
            }
            grinder.statistics.forLastTest.setLong("userLong1", elapsed)

        } catch (Exception e) {
            // nGrinder 종료 시그널(Thread has been shut down)은 에러로 남기지 않음
            if (!e.message?.contains("shut down") && !e.message?.contains("interrupted")) {
                grinder.logger.error("[ERROR:home] " + e.message)
            }
        }
    }
}
