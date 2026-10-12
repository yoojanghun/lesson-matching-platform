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
 * nGrinder 부하 테스트 시나리오 4:
 * "결제 동시성 테스트 — 상태 전이 원자성 / 중복 결제 / 입금 확인 Race Condition"
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 시나리오 설명                                                            │
 * │  • Phase 1: 여러 학생이 동시에 동일 매칭의 예약에 대해 결제 요청 생성    │
 * │  • Phase 2: 학생들이 동시에 이체 완료 신고                               │
 * │  • Phase 3: 튜터들이 동시에 입금 확인/취소 처리 (상태 전이 Race)         │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * 결제 시스템의 치명적 동시성 버그:
 *  1. 중복 결제 생성 (동일 예약에 2개 이상의 결제)
 *  2. 중복 상태 전이 (PENDING → CLAIMED를 2회 이상 호출)
 *  3. 입금 확인 Race Condition (튜터가 동시에 확인/취소 버튼 클릭)
 *  4. 예약-결제 연관 정합성 (결제 취소 시 예약 릴리즈 누락)
 *  5. orderId 중복 생성 (UUID 충돌 또는 동시 생성)
 *
 * 측정 포인트:
 *  1. TPS (Transactions Per Second)       — 결제 생성/이체신고/입금확인 처리량
 *  2. 중복 결제 방지                       — 동일 예약에 1개 결제만 존재
 *  3. 상태 전이 원자성                     — PENDING → CLAIMED → DONE 순서 보장
 *  4. orderId UNIQUE 제약                 — DB 레벨 중복 방지
 *  5. DB Lock Wait Timeout                — 비관적 락 경합 시간
 *
 * nGrinder 권장 설정:
 *  ┌──────────────┬────────────┬──────────────┬────────────────────────────┐
 *  │ 단계          │ vUser      │ Duration     │ 목적                        │
 *  ├──────────────┼────────────┼──────────────┼────────────────────────────┤
 *  │ Warmup       │ 10         │ 1min         │ JIT 컴파일, DB 워밍업       │
 *  │ 기본 부하     │ 30         │ 3min         │ 정상 TPS 측정               │
 *  │ 중간 부하     │ 100        │ 3min         │ 상태 전이 충돌 검증         │
 *  │ 스파이크       │ 300        │ 2min         │ Race Condition 재현         │
 *  └──────────────┴────────────┴──────────────┴────────────────────────────┘
 *
 * 사전 준비:
 *  1. ACCEPTED 매칭 + CONFIRMED 예약이 충분히 존재
 *  2. STUDENT 계정 + TUTOR 계정 준비
 *  3. 튜터 계좌 정보 사전 등록 (bank_account 컬럼)
 *  4. 테스트용 매칭-예약 ID 매핑 준비
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RunWith(GrinderRunner)
class PaymentConcurrencyTest {

    // ─────────────────────────────────────────────────────────────────────
    // ★ 테스트 환경 설정
    // ─────────────────────────────────────────────────────────────────────

    static final String BASE_URL    = "http://host.docker.internal:8080"
    static final long TOKEN_TTL_MS  = 15 * 60 * 1000L  // 15분

    /**
     * 동시성 테스트 모드
     * - PREPARE_PHASE: 결제 생성 동시성 (학생들이 동시에 결제 요청)
     * - CLAIM_PHASE: 이체 완료 신고 동시성 (학생들이 동시에 이체 신고)
     * - CONFIRM_PHASE: 입금 확인 동시성 (튜터들이 동시에 확인/취소)
     */
    static final String TEST_PHASE = "PREPARE_PHASE"  // ← 테스트 단계 선택

    // ─────────────────────────────────────────────────────────────────────
    // 학생 계정 풀 (matchingId + reservationIds 매핑)
    // ★ 테스트 전 아래 SQL로 조회:
    //   SELECT m.matching_id, sa.student_id, ua.username,
    //          GROUP_CONCAT(r.reservation_id) as reservation_ids
    //   FROM matching m
    //   JOIN student_account sa ON m.student_id = sa.student_id
    //   JOIN user_account ua ON sa.user_account_id = ua.user_account_id
    //   JOIN reservation r ON r.matching_id = m.matching_id
    //   WHERE m.status = 'ACCEPTED'
    //   AND r.status = 'CONFIRMED'
    //   AND r.payment_id IS NULL
    //   GROUP BY m.matching_id, sa.student_id, ua.username;
    // ─────────────────────────────────────────────────────────────────────
    static final List<Map> STUDENT_USERS = [
        [username: "student1@test.com",  password: "testaccount123", matchingId: 1L, reservationIds: [1L, 2L]],
        [username: "student2@test.com",  password: "testaccount123", matchingId: 2L, reservationIds: [3L, 4L]],
        [username: "student3@test.com",  password: "testaccount123", matchingId: 3L, reservationIds: [5L, 6L]],
        [username: "student4@test.com",  password: "testaccount123", matchingId: 4L, reservationIds: [7L, 8L]],
        [username: "student5@test.com",  password: "testaccount123", matchingId: 5L, reservationIds: [9L, 10L]],
        [username: "student6@test.com",  password: "testaccount123", matchingId: 6L, reservationIds: [11L, 12L]],
        [username: "student7@test.com",  password: "testaccount123", matchingId: 7L, reservationIds: [13L, 14L]],
        [username: "student8@test.com",  password: "testaccount123", matchingId: 8L, reservationIds: [15L, 16L]],
        [username: "student9@test.com",  password: "testaccount123", matchingId: 9L, reservationIds: [17L, 18L]],
        [username: "student10@test.com", password: "testaccount123", matchingId: 10L, reservationIds: [19L, 20L]],
    ]

    // ─────────────────────────────────────────────────────────────────────
    // 튜터 계정 풀 (orderId 목록 매핑 — CLAIM_PHASE 이후 사용)
    // ★ 테스트 전 결제 생성 후 orderId 수집 필요:
    //   SELECT p.order_id, ta.tutor_id, ua.username
    //   FROM payment p
    //   JOIN matching m ON p.matching_id = m.matching_id
    //   JOIN tutor_account ta ON m.tutor_id = ta.tutor_id
    //   JOIN user_account ua ON ta.user_account_id = ua.user_account_id
    //   WHERE p.payment_status = 'TRANSFER_CLAIMED'
    //   ORDER BY ta.tutor_id;
    // ─────────────────────────────────────────────────────────────────────
    static final List<Map> TUTOR_USERS = [
        [username: "tutor1@test.com", password: "testaccount123", orderIds: ["ORDER_001", "ORDER_002"]],
        [username: "tutor2@test.com", password: "testaccount123", orderIds: ["ORDER_003", "ORDER_004"]],
        [username: "tutor3@test.com", password: "testaccount123", orderIds: ["ORDER_005", "ORDER_006"]],
        [username: "tutor4@test.com", password: "testaccount123", orderIds: ["ORDER_007", "ORDER_008"]],
        [username: "tutor5@test.com", password: "testaccount123", orderIds: ["ORDER_009", "ORDER_010"]],
    ]

    /**
     * CLAIM_PHASE용: 이미 생성된 결제의 orderId 목록
     * PREPARE_PHASE 실행 후 DB에서 조회하여 입력
     */
    static final List<String> CREATED_ORDER_IDS = [
        "ORDER_001", "ORDER_002", "ORDER_003", "ORDER_004", "ORDER_005",
        "ORDER_006", "ORDER_007", "ORDER_008", "ORDER_009", "ORDER_010"
    ]

    /**
     * 튜터 입금 확인/취소 비율 (CONFIRM_PHASE용)
     * 예: CONFIRM_RATIO = 80 → 80% 확인, 20% 취소
     */
    static final int CONFIRM_RATIO = 80

    // ─────────────────────────────────────────────────────────────────────
    // GTest 통계 수집 단위
    // ─────────────────────────────────────────────────────────────────────
    static GTest loginTest
    static GTest preparePaymentTest
    static GTest claimTransferTest
    static GTest confirmPaymentTest
    static GTest cancelPaymentTest
    static HTTPRequest request

    // ─────────────────────────────────────────────────────────────────────
    // 스레드 로컬 상태
    // ─────────────────────────────────────────────────────────────────────
    String accessToken = null
    long   tokenIssuedAt = 0L
    Long   currentMatchingId = null
    List<Long> currentReservationIds = []
    String currentUsername = null
    List<String> currentOrderIds = []

    // 통계 카운터
    int prepareSuccessCount  = 0  // 결제 생성 성공 (201)
    int claimSuccessCount    = 0  // 이체 신고 성공 (200)
    int confirmSuccessCount  = 0  // 입금 확인 성공 (200)
    int cancelSuccessCount   = 0  // 결제 취소 성공 (200)
    int conflictCount        = 0  // 409/400: 중복/상태 오류
    int errorCount           = 0  // 500: DB Lock 등
    int notFoundCount        = 0  // 404: 결제/매칭 없음

    int orderIdCursor = 0  // orderId 순환 인덱스

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
        loginTest           = new GTest(1, "POST /api/auth/login")
        preparePaymentTest  = new GTest(2, "POST /api/payments/prepare [결제 생성]")
        claimTransferTest   = new GTest(3, "POST /api/payments/claim-transfer [이체 신고]")
        confirmPaymentTest  = new GTest(4, "POST /api/payments/confirm [입금 확인]")
        cancelPaymentTest   = new GTest(5, "POST /api/payments/cancel [결제 취소]")
        request             = new HTTPRequest()

        grinder.logger.info("=" * 80)
        grinder.logger.info("=== [BeforeProcess] 결제 동시성 테스트 시작 ===")
        grinder.logger.info("=== 테스트 단계: ${TEST_PHASE} ===")
        grinder.logger.info("=" * 80)
    }

    // ─────────────────────────────────────────────────────────────────────
    // 스레드 초기화 (VUser당 1회)
    // ─────────────────────────────────────────────────────────────────────

    @BeforeThread
    void beforeThread() {
        if (TEST_PHASE == "CONFIRM_PHASE") {
            // 튜터 계정 로그인 (입금 확인/취소 테스트)
            int idx = (int)(grinder.threadNumber % TUTOR_USERS.size())
            currentUsername = TUTOR_USERS[idx].username
            currentOrderIds = TUTOR_USERS[idx].orderIds
            grinder.logger.info(
                "▶ Thread #${grinder.threadNumber} 초기화 [TUTOR] — ${currentUsername} | orderIds=${currentOrderIds}"
            )
        } else {
            // 학생 계정 로그인 (결제 생성/이체 신고 테스트)
            int idx = (int)(grinder.threadNumber % STUDENT_USERS.size())
            currentUsername = STUDENT_USERS[idx].username
            currentMatchingId = STUDENT_USERS[idx].matchingId
            currentReservationIds = STUDENT_USERS[idx].reservationIds
            grinder.logger.info(
                "▶ Thread #${grinder.threadNumber} 초기화 [STUDENT] — ${currentUsername} | " +
                "matchingId=${currentMatchingId} | reservationIds=${currentReservationIds}"
            )
        }
        login()
    }

    // ─────────────────────────────────────────────────────────────────────
    // 로그인
    // ─────────────────────────────────────────────────────────────────────

    void login() {
        Map user
        if (TEST_PHASE == "CONFIRM_PHASE") {
            int idx = (int)(grinder.threadNumber % TUTOR_USERS.size())
            user = TUTOR_USERS[idx]
        } else {
            int idx = (int)(grinder.threadNumber % STUDENT_USERS.size())
            user = STUDENT_USERS[idx]
        }

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
                grinder.logger.info("✅ 로그인 성공 | thread=${grinder.threadNumber} | user=${user.username}")
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
    // orderId 순환 선택
    // ─────────────────────────────────────────────────────────────────────

    String getNextOrderId() {
        if (TEST_PHASE == "CLAIM_PHASE") {
            // CLAIM_PHASE: 공용 orderId 풀 사용
            if (CREATED_ORDER_IDS.isEmpty()) return null
            String orderId = CREATED_ORDER_IDS[orderIdCursor % CREATED_ORDER_IDS.size()]
            orderIdCursor++
            return orderId
        } else if (TEST_PHASE == "CONFIRM_PHASE") {
            // CONFIRM_PHASE: 튜터별 orderId 사용
            if (currentOrderIds.isEmpty()) return null
            String orderId = currentOrderIds[orderIdCursor % currentOrderIds.size()]
            orderIdCursor++
            return orderId
        }
        return null
    }

    // ─────────────────────────────────────────────────────────────────────
    // ★ 테스트 1: 결제 생성 동시성 (PREPARE_PHASE)
    // POST /api/payments/prepare
    // ─────────────────────────────────────────────────────────────────────

    /**
     * 검증 포인트:
     *  - 201 Created: 결제 생성 성공
     *  - 409/400: 동일 예약에 이미 결제 존재 (중복 방지)
     *  - 500: orderId UNIQUE 제약 위반, DB Deadlock
     *
     * Race Condition 시나리오:
     *  - 여러 학생이 동일 예약에 대해 동시에 결제 생성 시도
     *  - orderId 생성 로직에서 UUID 충돌 또는 동시 생성
     */
    @Test
    void testPreparePaymentConcurrency() {
        if (TEST_PHASE != "PREPARE_PHASE") return
        if (!ensureValidToken() || currentMatchingId == null) {
            grinder.logger.warn("⛔ 토큰 또는 matchingId 없음 — 테스트 스킵")
            return
        }

        preparePaymentTest.record(request)

        String body = JsonOutput.toJson([
            matchingId: currentMatchingId,
            reservationId: currentReservationIds
        ])

        String url = "${BASE_URL}/api/payments/prepare"
        long start = System.currentTimeMillis()

        try {
            def res     = request.POST(url, body.getBytes("UTF-8"), authHeader())
            long elapsed = System.currentTimeMillis() - start

            switch (res.statusCode) {
                case 201:
                    // ✅ 결제 생성 성공
                    prepareSuccessCount++
                    def json = new JsonSlurper().parseText(res.text)
                    String orderId = json?.orderId as String
                    grinder.logger.info(
                        "✅ 결제 생성 성공 | orderId=${orderId} | " +
                        "matchingId=${currentMatchingId} | elapsed=${elapsed}ms | " +
                        "thread=${grinder.threadNumber}"
                    )
                    break

                case 400:
                case 409:
                    // ⚠ 중복 방지 (동일 예약에 이미 결제 존재)
                    conflictCount++
                    grinder.logger.info(
                        "⚠ 중복/검증 실패(${res.statusCode}) | matchingId=${currentMatchingId} | " +
                        "elapsed=${elapsed}ms | thread=${grinder.threadNumber} | body=${res.text?.take(150)}"
                    )
                    break

                case 404:
                    notFoundCount++
                    grinder.logger.warn("⚠ 매칭/예약 없음(404) | matchingId=${currentMatchingId}")
                    break

                case 401:
                    grinder.logger.warn("🔐 인증 만료(401) — 재로그인")
                    accessToken = null
                    login()
                    break

                case 500:
                    // ❌ DB Lock, orderId 중복 등
                    errorCount++
                    grinder.logger.error(
                        "❌ 서버 에러(500) [동시성 버그 의심!] | matchingId=${currentMatchingId} | " +
                        "elapsed=${elapsed}ms | body=${res.text?.take(300)}"
                    )
                    break

                default:
                    grinder.logger.error("⚠ 예상치 못한 응답 | status=${res.statusCode}")
            }

            // 통계 출력
            int total = prepareSuccessCount + conflictCount + errorCount + notFoundCount
            if (total > 0 && total % 20 == 0) {
                grinder.logger.info(
                    "📊 [결제생성 집계] thread=${grinder.threadNumber} | " +
                    "성공=${prepareSuccessCount} | 중복=${conflictCount} | " +
                    "404=${notFoundCount} | 500=${errorCount}"
                )
            }

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start
            errorCount++
            grinder.logger.error("❌ 예외 발생 | elapsed=${elapsed}ms | msg=${e.message}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ★ 테스트 2: 이체 완료 신고 동시성 (CLAIM_PHASE)
    // POST /api/payments/claim-transfer
    // ─────────────────────────────────────────────────────────────────────

    /**
     * 검증 포인트:
     *  - 200 OK: 이체 신고 성공 (PENDING_TRANSFER → TRANSFER_CLAIMED)
     *  - 409/400: 이미 신고된 결제 재시도 (상태 전이 중복 방지)
     *  - 500: DB Lock Timeout, 상태 전이 Race Condition
     *
     * Race Condition 시나리오:
     *  - 학생이 이체 신고 버튼을 빠르게 2회 클릭 (더블 클릭)
     *  - 동일 orderId에 대해 2개 스레드가 동시에 상태 변경 시도
     */
    @Test
    void testClaimTransferConcurrency() {
        if (TEST_PHASE != "CLAIM_PHASE") return
        if (!ensureValidToken()) {
            grinder.logger.warn("⛔ 토큰 없음 — 테스트 스킵")
            return
        }

        String orderId = getNextOrderId()
        if (orderId == null) {
            grinder.logger.warn("⛔ orderId 없음 — CREATED_ORDER_IDS 설정 확인")
            return
        }

        claimTransferTest.record(request)

        String body = JsonOutput.toJson([orderId: orderId])
        String url = "${BASE_URL}/api/payments/claim-transfer"
        long start = System.currentTimeMillis()

        try {
            def res     = request.POST(url, body.getBytes("UTF-8"), authHeader())
            long elapsed = System.currentTimeMillis() - start

            switch (res.statusCode) {
                case 200:
                    // ✅ 이체 신고 성공
                    claimSuccessCount++
                    grinder.logger.info(
                        "✅ 이체 신고 성공 | orderId=${orderId} | elapsed=${elapsed}ms | " +
                        "thread=${grinder.threadNumber}"
                    )
                    break

                case 400:
                case 409:
                    // ⚠ 중복 신고 또는 상태 오류
                    conflictCount++
                    grinder.logger.info(
                        "⚠ 중복/상태 오류(${res.statusCode}) | orderId=${orderId} | " +
                        "elapsed=${elapsed}ms | body=${res.text?.take(150)}"
                    )
                    break

                case 404:
                    notFoundCount++
                    grinder.logger.warn("⚠ 결제 없음(404) | orderId=${orderId}")
                    break

                case 401:
                    grinder.logger.warn("🔐 인증 만료(401) — 재로그인")
                    accessToken = null
                    login()
                    break

                case 500:
                    errorCount++
                    grinder.logger.error(
                        "❌ 서버 에러(500) [상태 전이 Race 의심!] | orderId=${orderId} | " +
                        "elapsed=${elapsed}ms | body=${res.text?.take(300)}"
                    )
                    break

                default:
                    grinder.logger.error("⚠ 예상치 못한 응답 | status=${res.statusCode}")
            }

            int total = claimSuccessCount + conflictCount + errorCount + notFoundCount
            if (total > 0 && total % 20 == 0) {
                grinder.logger.info(
                    "📊 [이체신고 집계] thread=${grinder.threadNumber} | " +
                    "성공=${claimSuccessCount} | 중복=${conflictCount} | " +
                    "404=${notFoundCount} | 500=${errorCount}"
                )
            }

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start
            errorCount++
            grinder.logger.error("❌ 예외 발생 | orderId=${orderId} | elapsed=${elapsed}ms | msg=${e.message}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ★ 테스트 3: 입금 확인/취소 동시성 (CONFIRM_PHASE)
    // POST /api/payments/confirm (입금 확인)
    // POST /api/payments/cancel (결제 취소)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * 검증 포인트:
     *  - 200 OK: 확인/취소 성공
     *  - 409/400: 상태 오류 (동시에 확인+취소 시도 시 한 쪽은 실패)
     *  - 500: DB Lock, 예약 릴리즈 실패
     *
     * Race Condition 시나리오:
     *  - 튜터가 동시에 "입금 확인" + "결제 취소" 버튼 클릭 (UI 더블 클릭)
     *  - 첫 번째만 성공, 두 번째는 상태 오류로 차단되어야 함
     */
    @Test
    void testConfirmCancelConcurrency() {
        if (TEST_PHASE != "CONFIRM_PHASE") return
        if (!ensureValidToken()) {
            grinder.logger.warn("⛔ 토큰 없음 — 테스트 스킵")
            return
        }

        String orderId = getNextOrderId()
        if (orderId == null) {
            grinder.logger.warn("⛔ orderId 없음 — TUTOR_USERS orderIds 설정 확인")
            return
        }

        // CONFIRM_RATIO 기반으로 확인/취소 결정
        boolean isConfirm = (new Random().nextInt(100) < CONFIRM_RATIO)

        if (isConfirm) {
            confirmPaymentTest.record(request)
        } else {
            cancelPaymentTest.record(request)
        }

        String endpoint = isConfirm ? "/api/payments/confirm" : "/api/payments/cancel"
        String body = JsonOutput.toJson([
            orderId: orderId,
            cancelReason: isConfirm ? null : "금액 불일치"
        ])

        String url = "${BASE_URL}${endpoint}"
        long start = System.currentTimeMillis()

        try {
            def res     = request.POST(url, body.getBytes("UTF-8"), authHeader())
            long elapsed = System.currentTimeMillis() - start

            switch (res.statusCode) {
                case 200:
                    // ✅ 확인/취소 성공
                    if (isConfirm) {
                        confirmSuccessCount++
                        grinder.logger.info(
                            "✅ 입금 확인 성공 | orderId=${orderId} | elapsed=${elapsed}ms | " +
                            "thread=${grinder.threadNumber}"
                        )
                    } else {
                        cancelSuccessCount++
                        grinder.logger.info(
                            "✅ 결제 취소 성공 | orderId=${orderId} | elapsed=${elapsed}ms | " +
                            "thread=${grinder.threadNumber}"
                        )
                    }
                    break

                case 400:
                case 409:
                    // ⚠ 상태 오류 (이미 처리됨)
                    conflictCount++
                    grinder.logger.info(
                        "⚠ 상태 오류(${res.statusCode}) ${isConfirm ? '확인' : '취소'} | " +
                        "orderId=${orderId} | elapsed=${elapsed}ms | body=${res.text?.take(150)}"
                    )
                    break

                case 404:
                    notFoundCount++
                    grinder.logger.warn("⚠ 결제 없음(404) | orderId=${orderId}")
                    break

                case 401:
                    grinder.logger.warn("🔐 인증 만료(401) — 재로그인")
                    accessToken = null
                    login()
                    break

                case 500:
                    errorCount++
                    grinder.logger.error(
                        "❌ 서버 에러(500) [예약 릴리즈 실패 의심!] | orderId=${orderId} | " +
                        "elapsed=${elapsed}ms | body=${res.text?.take(300)}"
                    )
                    break

                default:
                    grinder.logger.error("⚠ 예상치 못한 응답 | status=${res.statusCode}")
            }

            int total = confirmSuccessCount + cancelSuccessCount + conflictCount + errorCount + notFoundCount
            if (total > 0 && total % 20 == 0) {
                grinder.logger.info(
                    "📊 [입금처리 집계] thread=${grinder.threadNumber} | " +
                    "확인=${confirmSuccessCount} | 취소=${cancelSuccessCount} | " +
                    "상태오류=${conflictCount} | 404=${notFoundCount} | 500=${errorCount}"
                )
            }

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start
            errorCount++
            grinder.logger.error("❌ 예외 발생 | orderId=${orderId} | elapsed=${elapsed}ms | msg=${e.message}")
        }
    }
}
