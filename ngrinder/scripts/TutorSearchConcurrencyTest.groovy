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
 * nGrinder 부하 테스트 시나리오 6:
 * "튜터 검색 부하 테스트 — Elasticsearch + Redis Cache Stampede 검증"
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 시나리오 설명                                                            │
 * │  • Read-Heavy 트래픽: 조회가 쓰기의 10~100배 (실제 플랫폼 트래픽 패턴) │
 * │  • 인기 키워드 집중 검색: 수백 명이 동시에 동일 키워드 검색             │
 * │  • Cache Stampede: 캐시 만료 시점에 ES로 동시 쿼리 폭주               │
 * │  • Deep Pagination: 깊은 페이지 조회 시 ES 메모리 부하                 │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * Read-Heavy 플랫폼의 핵심 병목:
 *  1. Redis Cache Miss → ES 동시 쿼리 (Stampede)
 *  2. Elasticsearch Bool Query 복잡도 증가 → CPU/Memory 병목
 *  3. Deep Pagination (from: 1000+) → Lucene Segment 지연
 *  4. 인기 튜터 프로필 조회 폭주 → DB/Redis 부하
 *  5. Elasticsearch Thread Pool Rejection
 *
 * 측정 포인트:
 *  1. TPS (Queries Per Second)           — Elasticsearch 처리량
 *  2. Cache Hit Ratio                    — Redis 캐시 효율
 *  3. Cache Stampede 발생 여부           — 동시 Cache Miss 시 ES 부하
 *  4. ES Latency (P50/P95/P99)          — 복잡한 검색 쿼리 응답 시간
 *  5. Deep Pagination 성능               — from: 1000+ 쿼리 지연
 *  6. ES JVM Heap / Thread Pool Status  — ES 클러스터 리소스
 *
 * nGrinder 권장 설정:
 *  ┌──────────────┬────────────┬──────────────┬────────────────────────────┐
 *  │ 단계          │ vUser      │ Duration     │ 목적                        │
 *  ├──────────────┼────────────┼──────────────┼────────────────────────────┤
 *  │ Warmup       │ 10         │ 1min         │ ES/Redis 워밍업             │
 *  │ 기본 부하     │ 50         │ 5min         │ 정상 TPS, 캐시 히트율       │
 *  │ 인기 키워드   │ 200        │ 3min         │ Cache Stampede 재현         │
 *  │ 스파이크       │ 500        │ 2min         │ ES Thread Pool Rejection    │
 *  └──────────────┴────────────┴──────────────┴────────────────────────────┘
 *
 * 사전 준비:
 *  1. Elasticsearch에 튜터 데이터 동기화
 *     → POST /api/tutors/sync/all
 *  2. Redis 캐시 TTL 설정 확인 (예: 5분)
 *  3. 인기 검색 키워드 목록 준비
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RunWith(GrinderRunner)
class TutorSearchConcurrencyTest {

    // ─────────────────────────────────────────────────────────────────────
    // ★ 테스트 환경 설정
    // ─────────────────────────────────────────────────────────────────────

    static final String BASE_URL = "http://host.docker.internal:8080"

    /**
     * 인기 검색 키워드 (Cache Stampede 유발용)
     * → 80% 트래픽을 상위 5개 키워드에 집중
     */
    static final List<String> POPULAR_KEYWORDS = [
        "서울 피아노",          // 가장 인기 있는 키워드 #1
        "입시 클래식",          // 인기 #2
        "강남 기타",            // 인기 #3
        "온라인 보컬",          // 인기 #4
        "주말 바이올린"         // 인기 #5
    ]

    /**
     * 다양한 검색 키워드 (트래픽 분산용)
     */
    static final List<String> DIVERSE_KEYWORDS = [
        "드럼 레슨", "첼로 초보", "색소폰 재즈", "플루트 클래식",
        "우쿨렐레 취미", "작곡 이론", "화성학", "음대 입시",
        "실용음악", "힙합 보컬", "뮤지컬 노래", "트롯 레슨",
        "베이스 기타", "일렉 기타", "통기타", "클래식 기타",
        "키보드 레슨", "신디사이저", "전자 드럼", "타악기"
    ]

    /**
     * 검색 조건 (필터)
     * locationIds: data-perf.sql 기준 실제 location_id 값 (Long 리스트로 쿼리 파라미터 전송)
     * sort: Spring Pageable 형식 — TutorDocument 필드명 기준
     *   totalScore,desc / averageRating,desc / minPrice,asc / reviewCount,desc / createdAt,desc
     */
    // location_id는 DB의 실제 PK값으로 교체 (현재 data-perf.sql 미포함 → 필터 비활성화)
    static final List<Long> LOCATION_ID_OPTIONS = [1L, 2L, 3L, 4L, 5L]   // ★ 실제 DB location PK로 교체
    static final List<Long> CATEGORY_ID_OPTIONS = [1L, 2L, 3L, 4L, 5L, 6L, 7L]  // category: 1=PIANO~7=COMPOSITION
    static final List<String> SORT_OPTIONS = ["totalScore,desc", "averageRating,desc", "minPrice,asc", "reviewCount,desc"]
    static final List<Map> PRICE_RANGES = [
        [min: 30000, max: 70000],
        [min: 50000, max: 100000],
        [min: null,  max: null]   // 가격 필터 없음
    ]

    /**
     * 인기 키워드 집중도 (%)
     * 80 = 80%의 요청이 인기 키워드, 20%는 다양한 키워드
     */
    static final int POPULAR_KEYWORD_RATIO = 80

    /**
     * Deep Pagination 테스트 활성화
     * true = 일부 요청을 깊은 페이지로 보내서 성능 측정
     * ES max_result_window=10000 기준: size=8이면 page 최대 1250까지 가능
     * 단, 실제 튜터 데이터 수보다 큰 페이지는 빈 결과 반환 → 부하 의미 없음
     * ★ data-perf.sql 기준 튜터 17명 → 최대 page=2 (17/8=2.12)
     *    Deep Pagination은 ES 데이터가 500개+ 이상일 때 의미 있음
     */
    static final boolean ENABLE_DEEP_PAGINATION = true
    // 11,017명 튜터 기준 / size(8) = 최대 1,377페이지 존재
    // ES max_result_window=10,000 기준: page 1250(1250*8=10,000)이 임계치
    // Deep Pagination 테스트: page 1000 ~ 1350 (from: 8,000 ~ 10,800) 구간 집중 부하
    static final int MAX_MEANINGFUL_PAGE = 1350

    // ─────────────────────────────────────────────────────────────────────
    // GTest 통계 수집 단위
    // ─────────────────────────────────────────────────────────────────────
    static GTest searchTest
    static GTest profileTest
    static GTest reviewListTest
    static HTTPRequest request

    // ─────────────────────────────────────────────────────────────────────
    // 통계 카운터
    // ─────────────────────────────────────────────────────────────────────
    int searchSuccessCount   = 0
    int profileSuccessCount  = 0
    int cacheHitCount        = 0  // X-Cache: HIT 헤더로 확인
    int cacheMissCount       = 0  // X-Cache: MISS
    int slowQueryCount       = 0  // 응답 > 1000ms
    int errorCount           = 0
    int deepPageCount        = 0  // Deep Pagination 요청 수

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
        searchTest     = new GTest(1, "GET /api/tutors/search [Elasticsearch]")
        profileTest    = new GTest(2, "GET /api/tutors/{id}/profile [Redis 캐싱]")
        reviewListTest = new GTest(3, "GET /api/tutors/{id}/reviews")
        request        = new HTTPRequest()

        grinder.logger.info("=" * 80)
        grinder.logger.info("=== [BeforeProcess] 튜터 검색 부하 + Cache Stampede 테스트 시작 ===")
        grinder.logger.info("=== 인기 키워드 집중도: ${POPULAR_KEYWORD_RATIO}% ===")
        grinder.logger.info("=== Deep Pagination: ${ENABLE_DEEP_PAGINATION} ===")
        grinder.logger.info("=" * 80)
    }

    @BeforeThread
    void beforeThread() {
        grinder.logger.info("▶ Thread #${grinder.threadNumber} 초기화")
    }

    // ─────────────────────────────────────────────────────────────────────
    // 키워드 선택 (인기 vs 다양성)
    // ─────────────────────────────────────────────────────────────────────

    String selectKeyword() {
        int rand = new Random().nextInt(100)
        if (rand < POPULAR_KEYWORD_RATIO) {
            // 인기 키워드 (Cache Stampede 유발)
            return POPULAR_KEYWORDS[new Random().nextInt(POPULAR_KEYWORDS.size())]
        } else {
            // 다양한 키워드 (캐시 분산)
            return DIVERSE_KEYWORDS[new Random().nextInt(DIVERSE_KEYWORDS.size())]
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ★ 메인 테스트 1: Elasticsearch 검색 (Cache Stampede 검증)
    // GET /api/tutors/search?keyword={keyword}&location={loc}&sort={sort}
    // ─────────────────────────────────────────────────────────────────────

    /**
     * 검증 포인트:
     *  - 200 OK: 검색 성공
     *  - X-Cache 헤더: HIT (캐시) vs MISS (ES 쿼리)
     *  - 응답 시간: P95 < 500ms 목표
     *  - Cache Stampede: 인기 키워드 캐시 만료 시 ES 동시 쿼리 폭주
     *  - Deep Pagination: from > 1000 시 지연
     *
     * Cache Stampede 시나리오:
     *  1. 인기 키워드의 캐시 TTL 만료 (예: 5분)
     *  2. 200명이 동시에 같은 키워드 검색
     *  3. Redis Cache Miss → 200개 요청 모두 ES로 쿼리
     *  4. ES Thread Pool 고갈 또는 CPU 100%
     */
    @Test
    void testElasticsearchSearch() {
        searchTest.record(request)

        // 키워드 선택
        String keyword = selectKeyword()
        boolean isPopular = POPULAR_KEYWORDS.contains(keyword)

        // ─── 필터 조건 랜덤 생성 (TutorSearchCondition 필드명 기준) ───
        String sort = SORT_OPTIONS[new Random().nextInt(SORT_OPTIONS.size())]
        Map priceRange = PRICE_RANGES[new Random().nextInt(PRICE_RANGES.size())]

        // locationIds, categoryIds: @ModelAttribute로 List<Long> 바인딩됨
        // → ?locationIds=1&locationIds=2 형식으로 전송 (★ location 문자열이 아님!)
        // data-perf.sql에 location 데이터가 없으면 필터 비활성화 → 파라미터 생략
        Long locationId = LOCATION_ID_OPTIONS[new Random().nextInt(LOCATION_ID_OPTIONS.size())]
        Long categoryId = CATEGORY_ID_OPTIONS[new Random().nextInt(CATEGORY_ID_OPTIONS.size())]

        // Deep Pagination 적용 (전체 요청 중 약 20%를 깊은 페이지로 전송)
        int page = 0
        if (ENABLE_DEEP_PAGINATION && new Random().nextInt(100) < 20) {
            // 20% 확률로 Deep Page: page 1000 ~ 1350 (from: 8,000 ~ 10,800)
            // 1,250페이지(from: 10,000)를 넘어가면 ES max_result_window 한계 에러 발생
            page = 1000 + new Random().nextInt(351)
            deepPageCount++
        } else {
            // 80% 확률로 일반 사용자 탐색 (0 ~ 50페이지)
            page = new Random().nextInt(51)
        }

        int size = 8

        // URL 조립 — Spring Pageable sort 형식: sort=field,direction
        StringBuilder urlBuilder = new StringBuilder("${BASE_URL}/api/tutors/search?")
        if (keyword) urlBuilder.append("keyword=${URLEncoder.encode(keyword, 'UTF-8')}&")
        urlBuilder.append("categoryIds=${categoryId}&")   // Long ID 필터 (실제 바인딩됨)
        urlBuilder.append("locationIds=${locationId}&")  // ★ location 데이터 있으면 활성화
        if (priceRange.min != null) urlBuilder.append("minPrice=${priceRange.min}&")
        if (priceRange.max != null) urlBuilder.append("maxPrice=${priceRange.max}&")
        urlBuilder.append("sort=${sort}&page=${page}&size=${size}")

        String url = urlBuilder.toString()

        long start = System.currentTimeMillis()

        try {
            def res     = request.GET(url, null, jsonHeader())
            long elapsed = System.currentTimeMillis() - start

            // ─── Cache Hit/Miss 확인 (서버가 X-Cache 헤더 제공 시) ───
            String cacheStatus = res.getHeader("X-Cache") ?: "UNKNOWN"
            boolean isCacheHit = cacheStatus.contains("HIT")
            boolean isCacheMiss = cacheStatus.contains("MISS")

            if (isCacheHit) cacheHitCount++
            if (isCacheMiss) cacheMissCount++

            // ─── Slow Query 감지 ───
            boolean isSlow = elapsed > 1000
            if (isSlow) slowQueryCount++

            String pageLabel = page > 10 ? "[🎯 Deep Page=${page}]" : "[페이지=${page}]"
            String keywordLabel = isPopular ? "[🔥 인기 키워드]" : "[분산 키워드]"
            String cacheLabel = isCacheHit ? "[✅ CACHE HIT]" : (isCacheMiss ? "[❌ CACHE MISS → ES]" : "")

            switch (res.statusCode) {
                case 200:
                    searchSuccessCount++
                    def json = new JsonSlurper().parseText(res.text)
                    int totalElements = json?.totalElements ?: 0
                    int returned = json?.content?.size() ?: 0

                    grinder.logger.info(
                        "✅ 검색 성공 ${keywordLabel} ${cacheLabel} ${pageLabel} | " +
                        "keyword=${keyword} | total=${totalElements} | returned=${returned} | " +
                        "elapsed=${elapsed}ms | thread=${grinder.threadNumber}" +
                        (isSlow ? " ⚠ 응답 지연! (Slow Query)" : "") +
                        (page > 10 ? " [Deep Pagination 성능 측정]" : "")
                    )
                    break

                case 204:
                    // 검색 결과 없음 (정상 케이스)
                    grinder.logger.info(
                        "ℹ️ 검색 결과 없음(204) ${keywordLabel} | keyword=${keyword} | elapsed=${elapsed}ms"
                    )
                    break

                case 500:
                    // ES 장애, Thread Pool Rejection 등
                    errorCount++
                    grinder.logger.error(
                        "❌ 서버 에러(500) ${keywordLabel} [ES 장애 의심!] | " +
                        "keyword=${keyword} | elapsed=${elapsed}ms | body=${res.text?.take(200)}"
                    )
                    break

                case 503:
                    // ES Thread Pool Rejection (ES가 과부하 상태)
                    errorCount++
                    grinder.logger.error(
                        "❌ 서비스 과부하(503) [ES Thread Pool Rejection!] | " +
                        "keyword=${keyword} | elapsed=${elapsed}ms"
                    )
                    break

                default:
                    grinder.logger.error("⚠ 예상치 못한 응답 | status=${res.statusCode}")
            }

            // ─── 통계 요약 로그 (100회마다 출력) ───
            // 문제: grinder.logger.info는 nGrinder 기본 로그 레벨(WARN)에서 출력되지 않음
            // 해결: logger.warn 으로 변경하여 기본 설정에서도 반드시 로그 파일에 기록되도록 함
            int total = searchSuccessCount + errorCount
            if (total > 0 && total % 100 == 0) {
                double hitRatio = cacheHitCount + cacheMissCount > 0 ?
                    (cacheHitCount * 100.0 / (cacheHitCount + cacheMissCount)) : 0.0
                grinder.logger.warn(
                    "📊 [검색 집계] thread=${grinder.threadNumber} | " +
                    "성공=${searchSuccessCount} | 에러=${errorCount} | " +
                    "캐시 히트율=${String.format('%.1f', hitRatio)}% | " +
                    "Slow Query=${slowQueryCount} | Deep Page=${deepPageCount}"
                )
            }

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start
            errorCount++
            grinder.logger.error(
                "❌ 예외 발생 | keyword=${keyword} | elapsed=${elapsed}ms | msg=${e.message}"
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ★ 서브 테스트 1: 인기 튜터 프로필 조회 (Redis 캐싱)
    // GET /api/tutors/{tutorId}/profile
    // ─────────────────────────────────────────────────────────────────────

    /**
     * 검증 포인트:
     *  - Redis 캐시 적용 여부 확인
     *  - 인기 튜터 프로필 폭주 시 DB 부하
     *  - Cache Stampede (캐시 만료 시 DB 동시 쿼리)
     */
    @Test
    void testPopularTutorProfile() {
        profileTest.record(request)

        // 인기 튜터 ID (상위 10명에 트래픽 집중)
        // ★ 실제 DB의 인기 튜터 ID로 교체
        List<Long> popularTutorIds = [1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L]
        Long tutorId = popularTutorIds[new Random().nextInt(popularTutorIds.size())]

        String url = "${BASE_URL}/api/tutors/${tutorId}/profile"
        long start = System.currentTimeMillis()

        try {
            def res     = request.GET(url, null, jsonHeader())
            long elapsed = System.currentTimeMillis() - start

            String cacheStatus = res.getHeader("X-Cache") ?: "UNKNOWN"
            boolean isCacheHit = cacheStatus.contains("HIT")

            if (res.statusCode == 200) {
                profileSuccessCount++
                if (isCacheHit) cacheHitCount++

                // nGrinder는 WARN 이상만 0.log에 남기므로 warn으로 기록
                // 캐시 미스(DB 조회)이거나 500ms 이상 지연되면 경고 표시
                if (!isCacheHit || elapsed > 300) {
                    grinder.logger.warn(
                        "⚡ [캐시 미스/지연] tutorId=${tutorId} | cache=${cacheStatus} | elapsed=${elapsed}ms"
                    )
                } else if (profileSuccessCount % 50 == 0) {
                    grinder.logger.warn(
                        "✅ [프로필 조회] thread=${grinder.threadNumber} | tutorId=${tutorId} | cache=${cacheStatus} | elapsed=${elapsed}ms"
                    )
                }
            } else {
                errorCount++
                grinder.logger.error("⚠ 프로필 조회 실패 | status=${res.statusCode}")
            }

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start
            errorCount++
            grinder.logger.error("❌ 예외 발생 | elapsed=${elapsed}ms | msg=${e.message}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ★ 서브 테스트 2: 튜터 리뷰 목록 조회 (읽기 부하)
    // GET /api/tutors/{tutorId}/reviews
    // ─────────────────────────────────────────────────────────────────────

    @Test
    void testTutorReviewList() {
        reviewListTest.record(request)

        List<Long> tutorIds = [1L, 2L, 3L, 4L, 5L]
        Long tutorId = tutorIds[new Random().nextInt(tutorIds.size())]

        String url = "${BASE_URL}/api/tutors/${tutorId}/reviews?page=0&size=5"
        long start = System.currentTimeMillis()

        try {
            def res     = request.GET(url, null, jsonHeader())
            long elapsed = System.currentTimeMillis() - start

            if (res.statusCode == 200) {
                def json = new JsonSlurper().parseText(res.text)
                int returned = json?.content?.size() ?: 0
                grinder.logger.info(
                    "📋 리뷰 목록 조회 | tutorId=${tutorId} | returned=${returned}건 | " +
                    "elapsed=${elapsed}ms"
                )
            }
        } catch (Exception e) {
            grinder.logger.error("❌ 리뷰 목록 예외 | msg=${e.message}")
        }
    }
}
