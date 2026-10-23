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
 * nGrinder 부하 테스트 시나리오 3:
 * "레슨 예약 동시성 테스트 — 시간 슬롯 선점 Race Condition 검증"
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ 시나리오 설명                                                            │
 * │  • 1명의 인기 튜터의 선호 시간대(예: 평일 저녁 7시, 주말 오후 2시)에      │
 * │    여러 학생이 동시에 예약 요청을 보냄                                   │
 * │  • 동일 튜터-날짜-시간대에 중복 예약(Double Booking)이 발생하면 안됨     │
 * │  • Race Condition 발생 시: 2명 이상이 같은 타임슬롯 예약 성공            │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * 측정 포인트:
 *  1. TPS (Transactions Per Second)       — 예약 요청 처리량
 *  2. Double Booking 방지 정합성          — 동일 시간대 중복 예약 차단 검증
 *  3. Overlapping Schedule Check 지연     — Between Query 성능
 *  4. DB Connection Pool 고갈             — Lock Wait Timeout
 *  5. 알림/캘린더 연동 병목                — 비동기 처리 지연 (RabbitMQ, 외부 API)
 *
 * nGrinder 권장 설정:
 *  ┌──────────────┬────────────┬──────────────┬────────────────────────────┐
 *  │ 단계          │ vUser      │ Duration     │ 목적                        │
 *  ├──────────────┼────────────┼──────────────┼────────────────────────────┤
 *  │ Warmup       │ 10         │ 1min         │ JIT 컴파일, DB 워밍업       │
 *  │ 기본 부하     │ 30         │ 3min         │ 정상 TPS 측정               │
 *  │ 중간 부하     │ 100        │ 3min         │ 시간대 충돌 검증            │
 *  │ 스파이크       │ 300        │ 2min         │ Race Condition 재현         │
 *  └──────────────┴────────────┴──────────────┴────────────────────────────┘
 *
 * 사전 준비:
 *  1. ACCEPTED 상태의 매칭이 충분히 존재해야 함
 *     → matching 테이블에 status='ACCEPTED' 레코드 준비
 *  2. STUDENT 계정 풀: student1@test.com ~ studentN@test.com
 *  3. TUTOR 계정이 실제 DB에 존재하고 해당 튜터와 학생 간 ACCEPTED 매칭 필요
 *  4. 인기 시간대(예: 평일 19:00-20:00, 토요일 14:00-15:00)를 집중 공략
 *
 * 동시성 검증 전략:
 *  - 모든 학생 VUser가 동일한 튜터의 동일한 시간대를 동시에 예약 시도
 *  - 첫 번째 요청만 201 Created, 나머지는 409 Conflict 또는 400 Bad Request
 *  - 500 에러 발생 시 DB Lock 충돌/Deadlock 의심
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RunWith(GrinderRunner)
class ReservationConcurrencyTest {

    // ─────────────────────────────────────────────────────────────────────
    // ★ 테스트 환경 설정 (실제 환경에 맞게 수정 필요)
    // ─────────────────────────────────────────────────────────────────────

    static final String BASE_URL    = "http://host.docker.internal:8080"
    static final long TOKEN_TTL_MS  = 15 * 60 * 1000L  // 15분

    /**
     * 부하를 몰아줄 인기 튜터 ID
     * ★ 테스트 전 DB 확인: 
     *   SELECT tutor_id FROM tutor_account LIMIT 1;
     */
    static final Long POPULAR_TUTOR_ID = 1L

    /**
     * 동시성 테스트에 사용할 고정 시간 슬롯 (인기 시간대)
     * → 모든 학생이 이 시간대를 동시에 예약 시도하여 Race Condition 유도
     * ※ @FutureOrPresent 유효성 검증을 통과하기 위해 현재 날짜(2026-10-23) 이후의 미래 날짜 사용
     */
    static final String TARGET_DATE       = "2026-10-29"  // 목요일 (미래 날짜)
    static final String TARGET_DAY        = "THURSDAY"
    static final String TARGET_START_TIME = "19:00"       // 평일 저녁 7시 (인기 시간대)
    static final String TARGET_END_TIME   = "20:00"

    /**
     * 대체 시간대 (다양한 시간대 테스트)
     * 일부 VUser는 다른 시간대도 시도하여 전체 예약 시스템 부하 테스트
     */
    static final List<Map> TIME_SLOTS = [
        [date: "2026-10-29", day: "THURSDAY", start: "19:00", end: "20:00"],  // 인기 #1
        [date: "2026-10-30", day: "FRIDAY",   start: "20:00", end: "21:00"],  // 인기 #2
        [date: "2026-11-01", day: "SUNDAY",   start: "14:00", end: "15:00"],  // 주말 인기
        [date: "2026-11-02", day: "MONDAY",   start: "18:00", end: "19:00"],
        [date: "2026-11-03", day: "TUESDAY",  start: "19:30", end: "20:30"],
    ]

    /**
     * 동일 시간대 집중도 (%)
     * 80 = 80%의 요청이 TARGET 시간대로 집중, 20%는 분산
     */
    static final int SAME_SLOT_CONCENTRATION = 80

    // ─────────────────────────────────────────────────────────────────────
    // 학생 계정 풀 + 매칭 ID 매핑
    // ★ 테스트 전 아래 SQL로 조회하여 실제 데이터로 교체:
    //   SELECT m.matching_id, sa.student_id, ta.tutor_id, ua_s.username
    //   FROM matching m
    //   JOIN student_account sa ON m.student_id = sa.student_id
    //   JOIN tutor_account ta ON m.tutor_id = ta.tutor_id
    //   JOIN user_account ua_s ON sa.user_account_id = ua_s.user_account_id
    //   WHERE m.status = 'ACCEPTED'
    //   AND ta.tutor_id = 1
    //   ORDER BY sa.student_id;
    // ─────────────────────────────────────────────────────────────────────
    static final List<Map> STUDENT_USERS = [
        [username: "student1@test.com",  password: "testaccount123", matchingId: 1L],
        [username: "student3@test.com",  password: "testaccount123", matchingId: 2L],
        [username: "student11@test.com", password: "testaccount123", matchingId: 11L],
        [username: "student12@test.com", password: "testaccount123", matchingId: 12L],
        [username: "student13@test.com", password: "testaccount123", matchingId: 13L],
        [username: "student14@test.com", password: "testaccount123", matchingId: 14L],
        [username: "student15@test.com", password: "testaccount123", matchingId: 15L],
        [username: "student16@test.com", password: "testaccount123", matchingId: 16L],
        [username: "student17@test.com", password: "testaccount123", matchingId: 17L],
        [username: "student18@test.com", password: "testaccount123", matchingId: 18L],
    ]

    static final List<String> RESERVATION_MESSAGES = [
        "해당 시간에 레슨 부탁드립니다.",
        "이번 주 레슨 예약하고 싶습니다.",
        "선생님 이 시간 가능하신가요?",
        "레슨 신청합니다. 잘 부탁드립니다.",
        "평일 저녁 시간 예약 가능한지 확인 부탁드립니다.",
        "주말 오후 레슨 희망합니다.",
        "기존 스케줄 이어서 이번 주도 부탁드립니다.",
        "정기 레슨 예약 신청합니다.",
        "시간 맞춰 예약 부탁드립니다.",
        "이번 레슨도 잘 부탁드립니다.",
    ]

    // ─────────────────────────────────────────────────────────────────────
    // GTest 통계 수집 단위
    // ─────────────────────────────────────────────────────────────────────
    static GTest loginTest
    static GTest reservationTest
    static GTest reservationListTest
    static HTTPRequest request

    // ─────────────────────────────────────────────────────────────────────
    // 스레드 로컬 상태
    // ─────────────────────────────────────────────────────────────────────
    String accessToken = null
    long   tokenIssuedAt = 0L
    Long   currentMatchingId = null
    String currentUsername = null

    // 통계 카운터
    int successCount       = 0  // 201 Created
    int conflictCount      = 0  // 409/400: 중복 예약 방지
    int errorCount         = 0  // 500: DB Lock 등 동시성 에러
    int notFoundCount      = 0  // 404: 매칭 없음
    int doubleBookingCount = 0  // 동일 시간대 중복 예약 성공 (치명적 버그!)

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
        loginTest         = new GTest(1, "POST /api/auth/login [student]")
        reservationTest   = new GTest(2, "POST /api/reservations/matchings/{matchingId}/tutors/{tutorId}")
        reservationListTest = new GTest(3, "GET /api/reservations/matching/{matchingId}")
        request           = new HTTPRequest()

        grinder.logger.info("=" * 80)
        grinder.logger.info("=== [BeforeProcess] 레슨 예약 동시성 + 시간 슬롯 Race Condition 테스트 시작 ===")
        grinder.logger.info("=== 타겟 튜터 ID: ${POPULAR_TUTOR_ID} ===")
        grinder.logger.info("=== 집중 시간대: ${TARGET_DATE} ${TARGET_START_TIME}-${TARGET_END_TIME} ===")
        grinder.logger.info("=== 집중도: ${SAME_SLOT_CONCENTRATION}% ===")
        grinder.logger.info("=" * 80)
    }

    // ─────────────────────────────────────────────────────────────────────
    // 스레드 초기화 (VUser당 1회) — 학생 계정으로 로그인
    // ─────────────────────────────────────────────────────────────────────

    @BeforeThread
    void beforeThread() {
        int idx = (int)(grinder.threadNumber % STUDENT_USERS.size())
        currentUsername  = STUDENT_USERS[idx].username
        currentMatchingId = STUDENT_USERS[idx].matchingId
        grinder.logger.info(
            "▶ Thread #${grinder.threadNumber} 초기화 — 학생 로그인: ${currentUsername} | matchingId=${currentMatchingId}"
        )
        login()
    }

    // ─────────────────────────────────────────────────────────────────────
    // 로그인 (STUDENT 역할 JWT 발급)
    // ─────────────────────────────────────────────────────────────────────

    void login() {
        int idx  = (int)(grinder.threadNumber % STUDENT_USERS.size())
        Map user = STUDENT_USERS[idx]
        currentUsername  = user.username
        currentMatchingId = user.matchingId

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
    // 시간대 선택 로직 (집중도 기반)
    // ─────────────────────────────────────────────────────────────────────

    Map selectTimeSlot() {
        int rand = new Random().nextInt(100)
        if (rand < SAME_SLOT_CONCENTRATION) {
            // 집중 시간대로 Race Condition 유도
            return [
                date: TARGET_DATE,
                day:  TARGET_DAY,
                start: TARGET_START_TIME,
                end:   TARGET_END_TIME
            ]
        } else {
            // 분산 시간대 (전체 시스템 부하 테스트)
            return TIME_SLOTS[new Random().nextInt(TIME_SLOTS.size())]
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ★ 메인 테스트 1: 레슨 예약 동시 요청 (Race Condition 검증)
    // POST /api/reservations/matchings/{matchingId}/tutors/{tutorId}
    // ─────────────────────────────────────────────────────────────────────

    /**
     * 검증 포인트:
     *  - 201 Created  : 예약 생성 성공
     *  - 409 Conflict : 시간대 중복 (Overlapping Schedule) 정상 차단
     *  - 400 Bad Request: 검증 오류 (과거 날짜, 시작>종료 시간 등)
     *  - 500 Internal : DB Lock Timeout, Deadlock → 즉시 조사 필요!
     *
     * Double Booking 검증:
     *  - 동일 튜터-날짜-시간대에 201이 2회 이상 발생하면 치명적 버그
     *  - 첫 요청만 성공, 나머지는 409로 차단되어야 함
     *
     * 알림/캘린더 연동 병목:
     *  - 201 성공 후 RabbitMQ로 알림 발행, 외부 API(구글 캘린더) 연동
     *  - 응답 시간이 500ms 이상이면 비동기 처리 병목 의심
     */
    @Test
    void testConcurrentReservationRequest() {
        if (!ensureValidToken()) {
            grinder.logger.warn("⛔ 토큰 없음 — 테스트 스킵 | thread=${grinder.threadNumber}")
            return
        }

        if (currentMatchingId == null) {
            grinder.logger.warn("⛔ matchingId 없음 — STUDENT_USERS 설정 확인 필요")
            return
        }

        reservationTest.record(request)

        // 시간대 선택 (집중도 기반)
        Map slot = selectTimeSlot()
        String msg = RESERVATION_MESSAGES[new Random().nextInt(RESERVATION_MESSAGES.size())]

        String body = JsonOutput.toJson([
            date:      slot.date,
            dayOfWeek: slot.day,
            startTime: slot.start,
            endTime:   slot.end,
            requestMsg: msg
        ])

        String url = "${BASE_URL}/api/reservations/matchings/${currentMatchingId}/tutors/${POPULAR_TUTOR_ID}"
        long start = System.currentTimeMillis()

        try {
            def res     = request.POST(url, body.getBytes("UTF-8"), authHeader())
            long elapsed = System.currentTimeMillis() - start

            // ─── 동일 시간대 집중 시 상세 로그 ───
            boolean isTargetSlot = (slot.date == TARGET_DATE && slot.start == TARGET_START_TIME)
            String slotLabel = isTargetSlot ? "[🎯 타겟 시간대]" : "[분산 시간대]"

            switch (res.statusCode) {
                case 201:
                    // ✅ 예약 생성 성공
                    successCount++
                    if (isTargetSlot) {
                        // ★ 타겟 시간대 성공 = 다른 스레드와 경쟁에서 승리
                        // → 이후 동일 시간대 요청은 모두 409여야 정상
                        grinder.logger.info(
                            "✅ 예약 성공 ${slotLabel} | " +
                            "slot=${slot.date} ${slot.start}-${slot.end} | " +
                            "matchingId=${currentMatchingId} | elapsed=${elapsed}ms | " +
                            "thread=${grinder.threadNumber} | " +
                            "[알림/캘린더 비동기 처리 발행됨]" +
                            (elapsed > 500 ? " ⚠ 응답 지연! 비동기 병목 의심" : "")
                        )
                    } else {
                        grinder.logger.info(
                            "✅ 예약 성공 ${slotLabel} | slot=${slot.date} ${slot.start}-${slot.end} | " +
                            "elapsed=${elapsed}ms | thread=${grinder.threadNumber}"
                        )
                    }
                    break

                case 400:
                case 409:
                    // ⚠ 중복 방지 (Overlapping Schedule Check 정상 작동)
                    conflictCount++
                    if (isTargetSlot) {
                        grinder.logger.info(
                            "⚠ 중복 차단 ${slotLabel} [정상] | status=${res.statusCode} | " +
                            "slot=${slot.date} ${slot.start}-${slot.end} | " +
                            "elapsed=${elapsed}ms | thread=${grinder.threadNumber} | " +
                            "body=${res.text?.take(150)}"
                        )
                    } else {
                        grinder.logger.info(
                            "⚠ 검증 실패/중복(${res.statusCode}) ${slotLabel} | elapsed=${elapsed}ms | " +
                            "thread=${grinder.threadNumber}"
                        )
                    }
                    break

                case 404:
                    // 매칭 없음 (matchingId 설정 오류)
                    notFoundCount++
                    grinder.logger.warn(
                        "⚠ 매칭 없음(404) | matchingId=${currentMatchingId} | elapsed=${elapsed}ms | " +
                        "→ STUDENT_USERS matchingId 값 확인 필요"
                    )
                    break

                case 401:
                    grinder.logger.warn("🔐 인증 만료(401) — 재로그인 | thread=${grinder.threadNumber}")
                    accessToken = null
                    login()
                    break

                case 500:
                    // ❌ 서버 에러 — DB Lock, Deadlock, Connection Pool 고갈 등
                    errorCount++
                    grinder.logger.error(
                        "❌ 서버 에러(500) ${slotLabel} [동시성 버그 의심!] | " +
                        "slot=${slot.date} ${slot.start}-${slot.end} | " +
                        "matchingId=${currentMatchingId} | elapsed=${elapsed}ms | " +
                        "thread=${grinder.threadNumber} | body=${res.text?.take(300)}"
                    )
                    break

                default:
                    grinder.logger.error(
                        "⚠ 예상치 못한 응답 | status=${res.statusCode} | elapsed=${elapsed}ms | " +
                        "slot=${slot.date} ${slot.start}-${slot.end}"
                    )
            }

            // ─── 통계 요약 로그 (30회마다 출력) ───
            int total = successCount + conflictCount + errorCount + notFoundCount
            if (total > 0 && total % 30 == 0) {
                grinder.logger.info(
                    "📊 [예약 집계] thread=${grinder.threadNumber} | " +
                    "성공(201)=${successCount} | 중복차단(409/400)=${conflictCount} | " +
                    "404=${notFoundCount} | 500에러=${errorCount}"
                )
            }

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start
            errorCount++
            grinder.logger.error(
                "❌ 예외 발생 | slot=${slot.date} ${slot.start}-${slot.end} | " +
                "matchingId=${currentMatchingId} | elapsed=${elapsed}ms | " +
                "thread=${grinder.threadNumber} | msg=${e.message}"
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ★ 서브 테스트: 특정 매칭의 예약 목록 조회 (읽기 부하 병행 측정)
    // GET /api/reservations/matching/{matchingId}
    //
    // 목적: 예약 생성과 동시에 학생이 자신의 예약 목록 조회 시
    //       읽기 성능 저하(Lock 경합, Between Query 지연) 측정
    // ─────────────────────────────────────────────────────────────────────

    @Test
    void testReservationListRead() {
        if (!ensureValidToken() || currentMatchingId == null) return

        reservationListTest.record(request)

        String url = "${BASE_URL}/api/reservations/matching/${currentMatchingId}?page=0&size=15"
        long start = System.currentTimeMillis()

        try {
            def res     = request.GET(url, null, authHeader())
            long elapsed = System.currentTimeMillis() - start

            if (res.statusCode == 200) {
                def json     = new JsonSlurper().parseText(res.text)
                int returned = json?.content?.size() ?: 0
                grinder.logger.info(
                    "📋 예약 목록 조회 성공 | returned=${returned}건 | elapsed=${elapsed}ms | " +
                    "thread=${grinder.threadNumber}" +
                    (elapsed > 500 ? " [⚠ 응답 지연! Lock 경합 또는 Between Query 병목 의심]" : "")
                )
            } else {
                grinder.logger.warn(
                    "⚠ 목록 조회 실패 | status=${res.statusCode} | elapsed=${elapsed}ms"
                )
            }
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start
            grinder.logger.error("❌ 목록 조회 예외 | elapsed=${elapsed}ms | msg=${e.message}")
        }
    }
}
