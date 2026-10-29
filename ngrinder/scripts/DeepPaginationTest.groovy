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
 *  DeepPaginationTest.groovy
 *
 *  [목적]
 *  - 11,017건의 Elasticsearch 튜터 데이터셋을 대상으로 Deep Pagination 성능 측정
 *  - 에러 응답이 Deep 지연시간을 왜곡하지 않도록 GTest 3개로 엄격히 분리
 *
 *  [GTest 3단계 분리]
 *  1. Test 1 (Shallow: 70%): page 0 ~ 10 (from: 0 ~ 80)
 *     → 기준선(Baseline), 캐시 및 인덱스 기반 ~10ms 초고속 응답
 *  2. Test 2 (Deep Valid: 25%): page 1000 ~ 1249 (from: 8,000 ~ 9,992)
 *     → 10,000건 이내 유효 대량 오프셋 스캔, Lucene Segment 정렬/머지 지연 측정
 *  3. Test 3 (Over Window: 5%): page 1250 ~ 1350 (from: 10,000 ~ 10,800)
 *     → ES max_result_window(10,000) 한계 초과 에러(Result window too large) 확인용
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RunWith(GrinderRunner)
class DeepPaginationTest {

    static final String BASE_URL = "http://13.124.17.19:8080"
    static final int PAGE_SIZE = 8
    static final int SLOW_THRESHOLD_MS = 50   // 11,000건 규모에 맞춘 현실적인 지연 감지 기준 (50ms)

    // 통계 분리: 앞쪽 vs 깊은 유효 페이지 vs 한도 초과
    static GTest shallowSearchTest
    static GTest deepSearchTest
    static GTest overWindowTest

    static HTTPRequest shallowRequest
    static HTTPRequest deepRequest
    static HTTPRequest overWindowRequest

    @BeforeProcess
    static void beforeProcess() {
        HTTPPluginControl.getConnectionDefaults().timeout = 5000

        shallowSearchTest = new GTest(1, "GET /api/tutors/search [1. Shallow: 0~10]")
        deepSearchTest = new GTest(2, "GET /api/tutors/search [2. Deep Valid: 1000~1249]")
        overWindowTest = new GTest(3, "GET /api/tutors/search [3. Over Window: 1250+]")

        shallowRequest = new HTTPRequest()
        deepRequest = new HTTPRequest()
        overWindowRequest = new HTTPRequest()

        shallowSearchTest.record(shallowRequest)
        deepSearchTest.record(deepRequest)
        overWindowTest.record(overWindowRequest)
    }

    @BeforeThread
    void beforeThread() {
        grinder.statistics.delayReports = true
    }

    @Test
    void testSearchPagination() {
        NVPair[] headers = [new NVPair("Accept", "application/json")] as NVPair[]
        int roll = new Random().nextInt(100)

        int page
        HTTPRequest targetRequest
        String testLabel

        if (roll < 70) {
            // 1. Shallow Page (70%): 정상 앞쪽 페이지 탐색
            page = new Random().nextInt(11)
            targetRequest = shallowRequest
            testLabel = "Shallow"
        } else if (roll < 95) {
            // 2. Deep Valid (25%): 10,000건 직전까지의 유효 깊은 페이지 (오프셋 지연 측정)
            page = 1000 + new Random().nextInt(250)   // 1000 ~ 1249 (from: 8000 ~ 9992)
            targetRequest = deepRequest
            testLabel = "Deep"
        } else {
            // 3. Over Window (5%): 한도 초과 에러 동작 검증용 (부하 영향 최소화)
            page = 1250 + new Random().nextInt(101)   // 1250 ~ 1350 (from: 10000 ~ 10800)
            targetRequest = overWindowRequest
            testLabel = "OverWindow"
        }

        // 전체 11,017건 대상을 정렬 스캔
        String url = "${BASE_URL}/api/tutors/search?sort=createdAt,desc&page=${page}&size=${PAGE_SIZE}"
        long start = System.currentTimeMillis()

        try {
            def res = targetRequest.GET(url, null, headers)
            long elapsed = System.currentTimeMillis() - start

            if (res.statusCode == 200) {
                if (testLabel == "Deep" && elapsed > SLOW_THRESHOLD_MS) {
                    grinder.logger.warn("⚡ [Deep Page 지연] page=${page} (from=${page * PAGE_SIZE}) | elapsed=${elapsed}ms")
                }
            } else {
                // OverWindow 구간의 에러(4xx 또는 5xx)를 구분하여 기록
                if (testLabel == "OverWindow") {
                    grinder.logger.warn("💥 [한도 초과 에러 정상 포착] status=${res.statusCode} | page=${page} | elapsed=${elapsed}ms")
                } else {
                    grinder.logger.error("⚠ 예상치 못한 에러 [${testLabel}] | status=${res.statusCode} | page=${page}")
                }
            }

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start
            grinder.logger.error("❌ 예외 발생 [${testLabel}] | page=${page} | elapsed=${elapsed}ms | msg=${e.message}")
        }
    }
}
