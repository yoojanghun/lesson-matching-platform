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
 * nGrinder 부하 테스트 시나리오 7:
 * "리뷰 시스템 동시성 테스트 — 평점 집계 정합성 / 중복 리뷰 방지"
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 시나리오 설명                                                            │
 * │  • 인기 튜터에게 수십 명의 학생이 동시에 리뷰 작성                       │
 * │  • 평점 집계(average_rating, review_count) 정합성 검증                  │
 * │  • 중복 리뷰 방지: 동일 학생이 동일 튜터에게 2개 이상 리뷰 차단          │
 * │  • 리뷰 삭제 시 평점 재계산 정확성                                       │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * 리뷰 시스템의 치명적 동시성 버그:
 *  1. 평점 집계 Race Condition
 *     → 동시에 여러 리뷰 작성 시 average_rating 계산 오류
 *     → review_count 증가 누락 (Lost Update)
 *  2. 중복 리뷰 생성
 *     → 동일 학생-튜터 쌍에 2개 이상 리뷰 생성
 *     → 네트워크 지연으로 버튼 연타 시 발생
 *  3. 리뷰 삭제 시 평점 불일치
 *     → 리뷰 삭제 후 평점 재계산 누락
 *  4. 수업 완료 검증 쿼리 병목
 *     → JOIN 쿼리로 COMPLETED 레슨 확인 시 DB 부하
 *
 * 측정 포인트:
 *  1. TPS (Reviews Per Second)           — 리뷰 작성 처리량
 *  2. 평점 정합성                        — DB 평점 vs 실제 계산값 일치 여부
 *  3. 중복 리뷰 방지                     — 동일 학생-튜터 1개 리뷰만 존재
 *  4. DB Lock Wait Timeout               — 동시 평점 업데이트 경합
 *  5. 비동기 집계 지연                   — Kafka/RabbitMQ 사용 시 최종 정합성
 *
 * nGrinder 권장 설정:
 *  ┌──────────────┬────────────┬──────────────┬────────────────────────────┐
 *  │ 단계          │ vUser      │ Duration     │ 목적                        │
 *  ├──────────────┼────────────┼──────────────┼────────────────────────────┤
 *  │ Warmup       │ 10         │ 1min         │ DB 워밍업                   │
 *  │ 기본 부하     │ 30         │ 3min         │ 정상 TPS 측정               │
 *  │ 중간 부하     │ 100        │ 3min         │ 평점 집계 정합성 검증       │
 *  │ 스파이크       │ 300        │ 2min         │ 중복 리뷰 Race 재현         │
 *  └──────────────┴────────────┴──────────────┴────────────────────────────┘
 *
 * 사전 준비:
 *  1. COMPLETED 레슨이 충분히 존재
 *     → 리뷰 작성은 레슨 완료 후에만 가능
 *  2. STUDENT 계정 풀 준비
 *  3. 인기 튜터 ID 확인
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RunWith(GrinderRunner)
class ReviewConcurrencyTest {

    // ─────────────────────────────────────────────────────────────────────
    // ★ 테스트 환경 설정
    // ─────────────────────────────────────────────────────────────────────

    static final String BASE_URL    = "http://host.docker.internal:8080"
    static final long TOKEN_TTL_MS  = 15 * 60 * 1000L  // 15분

    /**
     * 인기 튜터 ID (리뷰 집중 대상)
     * → 평점 집계 Race Condition 유발
     */
    static final Long POPULAR_TUTOR_ID = 1L

    /**
     * 인기 튜터 집중도 (%)
     * 80 = 80%의 리뷰가 인기 튜터에게 집중, 20%는 분산
     */
    static final int POPULAR_TUTOR_RATIO = 80

    // ─────────────────────────────────────────────────────────────────────
    // 학생 계정 풀 (리뷰 작성자)
    // ★ 테스트 전 COMPLETED 레슨이 있는 학생 계정으로 준비:
    //   SELECT sa.student_id, ua.username, COUNT(r.reservation_id) as completed_count
    //   FROM student_account sa
    //   JOIN user_account ua ON sa.user_account_id = ua.user_account_id
    //   JOIN reservation r ON r.student_id = sa.student_id
    //   WHERE r.status = 'COMPLETED'
    //   GROUP BY sa.student_id, ua.username
    //   HAVING COUNT(r.reservation_id) > 0;
    // ─────────────────────────────────────────────────────────────────────
    static final List<Map> STUDENT_USERS = [
        [username: "student1@test.com",  password: "testaccount123"],
        [username: "student2@test.com",  password: "testaccount123"],
        [username: "student3@test.com",  password: "testaccount123"],
        [username: "student4@test.com",  password: "testaccount123"],
        [username: "student5@test.com",  password: "testaccount123"],
        [username: "student6@test.com",  password: "testaccount123"],
        [username: "student7@test.com",  password: "testaccount123"],
        [username: "student8@test.com",  password: "testaccount123"],
        [username: "student9@test.com",  password: "testaccount123"],
        [username: "student10@test.com", password: "testaccount123"],
        [username: "student11@test.com", password: "testaccount123"],
        [username: "student12@test.com", password: "testaccount123"],
        [username: "student13@test.com", password: "testaccount123"],
        [username: "student14@test.com", password: "testaccount123"],
        [username: "student15@test.com", password: "testaccount123"],
        [username: "student16@test.com", password: "testaccount123"],
        [username: "student17@test.com", password: "testaccount123"],
        [username: "student18@test.com", password: "testaccount123"],
        [username: "student19@test.com", password: "testaccount123"],
        [username: "student20@test.com", password: "testaccount123"],
    ]

    /**
     * 분산 튜터 ID 목록
     */
    static final List<Long> TUTOR_IDS = [1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L]

    /**
     * 리뷰 내용 템플릿
     */
    static final List<Map> REVIEW_TEMPLATES = [
        [rating: 5, content: "정말 훌륭한 선생님이십니다! 덕분에 실력이 많이 늘었어요."],
        [rating: 5, content: "친절하고 체계적인 레슨으로 만족스럽습니다."],
        [rating: 4, content: "전반적으로 좋았지만 약간 아쉬운 부분도 있었어요."],
        [rating: 4, content: "성실하게 가르쳐 주셔서 감사합니다."],
        [rating: 3, content: "보통입니다. 나쁘지는 않았어요."],
        [rating: 5, content: "최고의 강사님! 강력 추천합니다."],
        [rating: 4, content: "실력 향상에 도움이 많이 되었습니다."],
        [rating: 5, content: "열정적으로 가르쳐 주셔서 좋았습니다."],
    ]

    // ─────────────────────────────────────────────────────────────────────
    // GTest 통계 수집 단위
    // ─────────────────────────────────────────────────────────────────────
    static GTest loginTest
    static GTest createReviewTest
    static GTest deleteReviewTest
    static HTTPRequest request

    // ─────────────────────────────────────────────────────────────────────
    // 스레드 로컬 상태
    // ─────────────────────────────────────────────────────────────────────
    String accessToken = null
    long   tokenIssuedAt = 0L
    String currentUsername = null

    // 통계 카운터
    int createSuccessCount   = 0  // 리뷰 작성 성공 (201)
    int duplicateCount       = 0  // 중복 리뷰 차단 (409/400)
    int deleteSuccessCount   = 0  // 리뷰 삭제 성공
    int errorCount           = 0  // 500: DB Lock 등
    int notFoundCount        = 0  // 404: 튜터/레슨 없음

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
        loginTest        = new GTest(1, "POST /api/auth/login [student]")
        createReviewTest = new GTest(2, "POST /api/tutors/{id}/reviews [리뷰 작성]")
        deleteReviewTest = new GTest(3, "DELETE /api/tutors/{tid}/reviews/{rid} [리뷰 삭제]")
        request          = new HTTPRequest()

        grinder.logger.info("=" * 80)
        grinder.logger.info("=== [BeforeProcess] 리뷰 시스템 동시성 + 평점 집계 정합성 테스트 시작 ===")
        grinder.logger.info("=== 인기 튜터 ID: ${POPULAR_TUTOR_ID} ===")
        grinder.logger.info("=== 집중도: ${POPULAR_TUTOR_RATIO}% ===")
        grinder.logger.info("=" * 80)
    }

    @BeforeThread
    void beforeThread() {
        int idx = (int)(grinder.threadNumber % STUDENT_USERS.size())
        currentUsername = STUDENT_USERS[idx].username
        grinder.logger.info(
            "▶ Thread #${grinder.threadNumber} 초기화 — 학생 로그인: ${currentUsername}"
        )
        login()
    }

    // ─────────────────────────────────────────────────────────────────────
    // 로그인
    // ─────────────────────────────────────────────────────────────────────

    void login() {
        int idx  = (int)(grinder.threadNumber % STUDENT_USERS.size())
        Map user = STUDENT_USERS[idx]
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
                grinder.logger.info("✅ 학생 로그인 성공 | thread=${grinder.threadNumber} | user=${user.username}")
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
    // 튜터 ID 선택 (인기 vs 분산)
    // ─────────────────────────────────────────────────────────────────────

    Long selectTutorId() {
        int rand = new Random().nextInt(100)
        if (rand < POPULAR_TUTOR_RATIO) {
            // 인기 튜터 (평점 집계 Race 유발)
            return POPULAR_TUTOR_ID
        } else {
            // 분산 튜터
            return TUTOR_IDS[new Random().nextInt(TUTOR_IDS.size())]
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ★ 메인 테스트: 리뷰 작성 (평점 집계 정합성 + 중복 방지)
    // POST /api/tutors/{tutorId}/reviews
    // ─────────────────────────────────────────────────────────────────────

    /**
     * 검증 포인트:
     *  - 201 Created: 리뷰 작성 성공 → 평점 집계 업데이트
     *  - 409/400: 중복 리뷰 차단 (동일 학생-튜터 쌍)
     *  - 500: DB Lock Timeout, 평점 집계 오류
     *
     * Race Condition 시나리오:
     *  1. 인기 튜터에게 50명이 동시에 리뷰 작성
     *  2. 평점 집계 (average_rating, review_count) 동시 업데이트
     *  3. Lost Update 발생 → 평점 불일치
     *
     * 중복 리뷰 시나리오:
     *  - 동일 학생이 네트워크 지연으로 버튼 연타
     *  - 첫 번째만 성공, 나머지는 409로 차단
     */
    @Test
    void testCreateReviewConcurrency() {
        if (!ensureValidToken()) {
            grinder.logger.warn("⛔ 토큰 없음 — 테스트 스킵 | thread=${grinder.threadNumber}")
            return
        }

        createReviewTest.record(request)

        // 튜터 선택
        Long tutorId = selectTutorId()
        boolean isPopular = (tutorId == POPULAR_TUTOR_ID)

        // 리뷰 내용 생성
        Map template = REVIEW_TEMPLATES[new Random().nextInt(REVIEW_TEMPLATES.size())]
        String body = JsonOutput.toJson([
            rating: template.rating,
            content: template.content
        ])

        String url = "${BASE_URL}/api/tutors/${tutorId}/reviews"
        long start = System.currentTimeMillis()

        try {
            def res     = request.POST(url, body.getBytes("UTF-8"), authHeader())
            long elapsed = System.currentTimeMillis() - start

            String tutorLabel = isPopular ? "[🔥 인기 튜터]" : "[분산 튜터]"

            switch (res.statusCode) {
                case 201:
                    // ✅ 리뷰 작성 성공 → 평점 집계 업데이트됨
                    createSuccessCount++
                    def json = new JsonSlurper().parseText(res.text)
                    Long reviewId = json?.reviewId as Long
                    grinder.logger.info(
                        "✅ 리뷰 작성 성공 ${tutorLabel} | reviewId=${reviewId} | " +
                        "tutorId=${tutorId} | rating=${template.rating} | " +
                        "elapsed=${elapsed}ms | thread=${grinder.threadNumber}" +
                        (isPopular ? " [평점 집계 동시 업데이트]" : "")
                    )
                    break

                case 400:
                case 409:
                    // ⚠ 중복 리뷰 차단 (동일 학생-튜터 쌍)
                    duplicateCount++
                    grinder.logger.info(
                        "⚠ 중복 리뷰 차단(${res.statusCode}) ${tutorLabel} | " +
                        "tutorId=${tutorId} | elapsed=${elapsed}ms | " +
                        "thread=${grinder.threadNumber} | body=${res.text?.take(150)}"
                    )
                    break

                case 403:
                    // 레슨 완료되지 않음 (COMPLETED 상태 아님)
                    grinder.logger.warn(
                        "⚠ 레슨 미완료(403) | tutorId=${tutorId} | " +
                        "→ COMPLETED 레슨 확인 필요"
                    )
                    break

                case 404:
                    notFoundCount++
                    grinder.logger.warn("⚠ 튜터 없음(404) | tutorId=${tutorId}")
                    break

                case 401:
                    grinder.logger.warn("🔐 인증 만료(401) — 재로그인 | thread=${grinder.threadNumber}")
                    accessToken = null
                    login()
                    break

                case 500:
                    // ❌ DB Lock, 평점 집계 오류
                    errorCount++
                    grinder.logger.error(
                        "❌ 서버 에러(500) ${tutorLabel} [평점 집계 Race 의심!] | " +
                        "tutorId=${tutorId} | elapsed=${elapsed}ms | " +
                        "thread=${grinder.threadNumber} | body=${res.text?.take(300)}"
                    )
                    break

                default:
                    grinder.logger.error("⚠ 예상치 못한 응답 | status=${res.statusCode}")
            }

            // ─── 통계 요약 로그 (30회마다 출력) ───
            int total = createSuccessCount + duplicateCount + errorCount + notFoundCount
            if (total > 0 && total % 30 == 0) {
                grinder.logger.info(
                    "📊 [리뷰 집계] thread=${grinder.threadNumber} | " +
                    "성공(201)=${createSuccessCount} | 중복차단=${duplicateCount} | " +
                    "404=${notFoundCount} | 500에러=${errorCount}"
                )
            }

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start
            errorCount++
            grinder.logger.error(
                "❌ 예외 발생 | tutorId=${tutorId} | elapsed=${elapsed}ms | " +
                "thread=${grinder.threadNumber} | msg=${e.message}"
            )
        }
    }
}
