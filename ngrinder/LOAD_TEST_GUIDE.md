# nGrinder 동시성 부하 테스트 가이드

## 📁 스크립트 목록

| 파일 | 시나리오 | 핵심 검증 포인트 |
|------|----------|----------------|
| [`MatchingConcurrencyTest.groovy`](./scripts/MatchingConcurrencyTest.groovy) | 단일 인기 튜터 몰림 테스트 | Race Condition, 중복 매칭 방지 |
| [`MatchingStatusUpdateTest.groovy`](./scripts/MatchingStatusUpdateTest.groovy) | 수락/거절 동시 처리 테스트 | 상태 원자성, SMS 알림 병목, DB Lock |
| [`ReservationConcurrencyTest.groovy`](./scripts/ReservationConcurrencyTest.groovy) | 레슨 예약 시간 슬롯 선점 테스트 | Double Booking 방지, Overlapping Schedule Check, 알림/캘린더 병목 |
| [`PaymentConcurrencyTest.groovy`](./scripts/PaymentConcurrencyTest.groovy) | 결제 상태 전이 동시성 테스트 | 중복 결제 방지, 상태 전이 원자성, 입금 확인 Race Condition |
| [`TutorSearchConcurrencyTest.groovy`](./scripts/TutorSearchConcurrencyTest.groovy) | 튜터 검색 부하 테스트 (Elasticsearch) | Cache Stampede, Deep Pagination, ES Thread Pool Rejection |
| [`ReviewConcurrencyTest.groovy`](./scripts/ReviewConcurrencyTest.groovy) | 리뷰 시스템 동시성 테스트 | 평점 집계 정합성, 중복 리뷰 방지 |
| [`AuthConcurrencyTest.groovy`](./scripts/AuthConcurrencyTest.groovy) | 회원가입/인증 동시성 테스트 | 아이디 중복 체크 Race, Bcrypt CPU 부하 |
| [`RecommendTutorTest.groovy`](./scripts/RecommendTutorTest.groovy) | AI 튜터 추천 부하 테스트 | LLM API Rate Limit, Latency |

---

## 🚀 실행 전 필수 설정

### 1. DB 계정 준비 (`data.sql`)

```sql
-- 학생 계정 50명 (MatchingConcurrencyTest용)
-- student1@test.com ~ student50@test.com

-- 튜터 계정 10명 (MatchingStatusUpdateTest용)
-- tutor1@test.com ~ tutor10@test.com

-- 인기 튜터 ID 확인
SELECT tutor_id FROM tutor_account LIMIT 1;
```

### 2. PENDING 매칭 ID 조회 (`MatchingStatusUpdateTest` 실행 전)

```sql
-- 튜터별 PENDING 매칭 ID 조회
SELECT m.matching_id, ta.tutor_id, ua.user_id
FROM matching m
JOIN tutor_account ta ON m.tutor_id = ta.tutor_id
JOIN user_account ua  ON ta.tutor_id = ua.id
WHERE m.status = 'PENDING'
ORDER BY ta.tutor_id, m.matching_id;
```

→ 조회 결과를 `TUTOR_PENDING_MATCHING_IDS` 맵에 직접 입력

### 3. ACCEPTED 매칭 ID + CONFIRMED 예약 ID 조회

#### ReservationConcurrencyTest 실행 전

```sql
-- 학생별 ACCEPTED 매칭 ID 조회 (레슨 예약 가능한 매칭)
SELECT m.matching_id, sa.student_id, ta.tutor_id, ua.username
FROM matching m
JOIN student_account sa ON m.student_id = sa.student_id
JOIN tutor_account ta ON m.tutor_id = ta.tutor_id
JOIN user_account ua ON sa.user_account_id = ua.user_account_id
WHERE m.status = 'ACCEPTED'
AND ta.tutor_id = 1  -- 인기 튜터 ID
ORDER BY sa.student_id;
```

→ 조회 결과를 `STUDENT_USERS` 리스트의 `matchingId` 필드에 입력

#### PaymentConcurrencyTest 실행 전

```sql
-- 학생별 ACCEPTED 매칭 + 결제 가능한 예약 조회
SELECT m.matching_id, sa.student_id, ua.username,
       GROUP_CONCAT(r.reservation_id) as reservation_ids
FROM matching m
JOIN student_account sa ON m.student_id = sa.student_id
JOIN user_account ua ON sa.user_account_id = ua.user_account_id
JOIN reservation r ON r.matching_id = m.matching_id
WHERE m.status = 'ACCEPTED'
AND r.status = 'CONFIRMED'
AND r.payment_id IS NULL  -- 아직 결제 안 된 예약
GROUP BY m.matching_id, sa.student_id, ua.username;
```

→ 조회 결과를 `PaymentConcurrencyTest`의 `STUDENT_USERS`에 입력

### 4. 스크립트 파라미터 수정

```groovy
// MatchingConcurrencyTest.groovy
static final Long POPULAR_TUTOR_ID = 1L  // ← 실제 DB tutor_id 로 교체

// MatchingStatusUpdateTest.groovy
static final Map TUTOR_PENDING_MATCHING_IDS = [
    "tutor1@test.com": [1L, 2L, 3L, ...],  // ← 실제 matchingId로 교체
    ...
]

// ReservationConcurrencyTest.groovy
static final Long POPULAR_TUTOR_ID = 1L  // ← 실제 DB tutor_id 로 교체
static final String TARGET_DATE = "2026-10-15"  // ← 미래 날짜로 설정
static final List<Map> STUDENT_USERS = [
    [username: "student1@test.com", password: "testaccount123", matchingId: 1L],  // ← 실제 matchingId
    ...
]

// PaymentConcurrencyTest.groovy
static final String TEST_PHASE = "PREPARE_PHASE"  // ← 단계 선택: PREPARE/CLAIM/CONFIRM
static final List<Map> STUDENT_USERS = [
    [username: "student1@test.com", password: "testaccount123", 
     matchingId: 1L, reservationIds: [1L, 2L]],  // ← 실제 데이터
    ...
]
```

---

## 🖥️ nGrinder 실행 방법

### 1. 컨테이너 실행
```bash
docker-compose up -d ngrinder-controller ngrinder-agent
```

### 2. Management UI 접속
- URL: [http://localhost:8000](http://localhost:8000)
- 계정: `admin` / `admin`

### 3. 스크립트 업로드
1. 상단 메뉴 → **Script** → **Create Script**
2. Groovy 파일 내용 붙여넣기 또는 파일 업로드

### 5. 테스트 실행 순서

```
Step 1: MatchingConcurrencyTest 실행 (PENDING 매칭 데이터 생성)
    └→ 완료 후 DB에서 matchingId 목록 확인

Step 2: MatchingStatusUpdateTest 실행 (수락/거절 동시 처리)
    └→ matchingId 목록을 스크립트에 입력 후 실행
    └→ 수락된 매칭이 ACCEPTED 상태가 됨

Step 3: ReservationConcurrencyTest 실행 (레슨 예약 시간 슬롯 선점)
    └→ ACCEPTED 매칭 ID를 스크립트에 입력 후 실행
    └→ 동일 시간대 중복 예약(Double Booking) 방지 검증
    └→ CONFIRMED 예약 생성됨

Step 4: PaymentConcurrencyTest 실행 (결제 동시성)
    └→ PREPARE_PHASE: 결제 생성 동시성 테스트
    └→ CLAIM_PHASE: 이체 완료 신고 동시성 테스트
    └→ CONFIRM_PHASE: 입금 확인/취소 동시성 테스트
```

---

## 📊 권장 nGrinder 설정값

### 시나리오 1: 단일 인기 튜터 몰림 (`MatchingConcurrencyTest`)

| 단계 | Agent | vUser | Duration | Ramp-Up | 목적 |
|------|-------|-------|----------|---------|------|
| Warmup | 1 | 10 | 1min | - | JIT, 커넥션 풀 예열 |
| 기본 부하 | 1 | 50 | 3min | 30s | 정상 TPS 측정 |
| 집중 부하 | 1 | 200 | 3min | 60s | 병목 / Lock 경합 |
| 스파이크 | 1 | 500 | 2min | 30s | Race Condition 재현 |

### 시나리오 2: 수락/거절 동시 처리 (`MatchingStatusUpdateTest`)

| 단계 | Agent | vUser | Duration | 목적 |
|------|-------|-------|----------|------|
| 기본 | 1 | 10 | 3min | 알림 지연 측정 |
| 중간 부하 | 1 | 30 | 3min | Lock 경합 확인 |
| 스파이크 | 1 | 100 | 2min | Thread Pool / DLX 발생 |

### 시나리오 3: 레슨 예약 시간 슬롯 선점 (`ReservationConcurrencyTest`)

| 단계 | Agent | vUser | Duration | Ramp-Up | 목적 |
|------|-------|-------|----------|---------|------|
| Warmup | 1 | 10 | 1min | - | JIT, 커넥션 풀 예열 |
| 기본 부하 | 1 | 30 | 3min | 30s | 정상 TPS 측정 |
| 중간 부하 | 1 | 100 | 3min | 60s | 시간대 충돌 검증 |
| 스파이크 | 1 | 300 | 2min | 30s | Double Booking 재현 |

### 시나리오 4: 결제 상태 전이 동시성 (`PaymentConcurrencyTest`)

| 단계 | Agent | vUser | Duration | Ramp-Up | 목적 |
|------|-------|-------|----------|---------|------|
| PREPARE_PHASE | 1 | 30 | 3min | 30s | 결제 생성 동시성, orderId 중복 |
| CLAIM_PHASE | 1 | 50 | 3min | 30s | 이체 신고 중복 방지 |
| CONFIRM_PHASE | 1 | 100 | 3min | 60s | 입금 확인/취소 Race |

**📌 PaymentConcurrencyTest 실행 순서:**
1. `TEST_PHASE = "PREPARE_PHASE"` 설정 → 결제 생성 테스트 실행
2. DB에서 생성된 `orderId` 수집 후 `CREATED_ORDER_IDS`에 입력
3. `TEST_PHASE = "CLAIM_PHASE"` 설정 → 이체 신고 테스트 실행
4. `TEST_PHASE = "CONFIRM_PHASE"` 설정 → 입금 확인/취소 테스트 실행

### 시나리오 5: 튜터 검색 (Elasticsearch + Cache Stampede)

| 단계 | Agent | vUser | Duration | Ramp-Up | 목적 |
|------|-------|-------|----------|---------|------|
| Warmup | 1 | 10 | 1min | - | ES/Redis 워밍업 |
| 기본 부하 | 1 | 50 | 5min | 30s | 캐시 히트율, 정상 TPS |
| 인기 키워드 | 1 | 200 | 3min | 60s | Cache Stampede 재현 |
| 스파이크 | 1 | 500 | 2min | 30s | ES Thread Pool Rejection |

### 시나리오 6: 리뷰 시스템 (평점 집계 정합성)

| 단계 | Agent | vUser | Duration | Ramp-Up | 목적 |
|------|-------|-------|----------|---------|------|
| Warmup | 1 | 10 | 1min | - | DB 워밍업 |
| 기본 부하 | 1 | 30 | 3min | 30s | 정상 TPS |
| 중간 부하 | 1 | 100 | 3min | 60s | 평점 집계 정합성 검증 |
| 스파이크 | 1 | 300 | 2min | 30s | 중복 리뷰 Race 재현 |

### 시나리오 7: 회원가입/인증 (중복 체크 Race + Bcrypt CPU)

| 단계 | Agent | vUser | Duration | Ramp-Up | 목적 |
|------|-------|-------|----------|---------|------|
| Warmup | 1 | 10 | 1min | - | JIT 컴파일 |
| 기본 부하 | 1 | 50 | 3min | 30s | CPU 사용률, 정상 TPS |
| 중간 부하 | 1 | 100 | 3min | 60s | 중복 체크 Race 검증 |
| 스파이크 | 1 | 300 | 2min | 30s | CPU Throttling, 중복 계정 |

**📌 AuthConcurrencyTest 모드:**
- `TEST_MODE = "SIGNUP_RACE"`: 동일 아이디로 동시 가입 (중복 체크 Race)
- `TEST_MODE = "SIGNUP_SPIKE"`: 대량 가입 (Bcrypt CPU 부하)
- `TEST_MODE = "LOGIN_SPIKE"`: 대량 로그인 (JWT 발급 부하)
- `TEST_MODE = "DUPLICATE_CHECK"`: 중복 체크 API 부하

---

## 🔍 결과 해석 가이드

### ✅ 정상 케이스

| 지표 | 정상 범위 | 비고 |
|------|-----------|------|
| TPS | 100 이상 | 기본 부하 기준 |
| Mean Latency | 200ms 이하 | 매칭 요청/상태변경 |
| 201 vs 409 비율 | 예측 가능 | 중복 방지 로직 정상 |
| 500 에러 | 0건 | 동시성 버그 없음 |

### ⚠️ 문제 징후별 원인 및 조치

#### 500 에러 다수 발생
```
원인: DB Duplicate Key Exception, Deadlock, Lock Wait Timeout
진단: MySQL Error Log 확인
     → SHOW ENGINE INNODB STATUS\G  (Deadlock 섹션)
조치: 비관적 락(@Lock) 추가 또는 UNIQUE 제약조건 + Retry 로직
```

#### 동일 학생-튜터 쌍에 201이 2회 이상
```
원인: Race Condition — existsActiveMatching() 체크 후 INSERT 사이 갭
진단: DB에서 중복 레코드 확인
     SELECT student_id, tutor_id, COUNT(*) FROM matching
     WHERE status IN ('PENDING', 'ACCEPTED')
     GROUP BY student_id, tutor_id HAVING COUNT(*) > 1;
조치: 1) 애플리케이션 레벨에서 상태 검증 (Payment 엔티티에 이미 구현됨)
        if (this.paymentStatus != PaymentStatus.PENDING_TRANSFER) {
            throw new IllegalStateException("이체 대기 상태만 신고 가능");
        }
     2) 비관적 락으로 결제 상태 잠금
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        Optional<Payment> findByOrderIdWithLock(String orderId);
     3) 낙관적 락 (@Version) 사용
```

#### 동일 예약에 2개 이상 결제 생성 (중복 결제)
```
원인: Race Condition — 예약 결제 여부 체크와 결제 생성 사이 시간 갭
진단: DB에서 중복 결제 확인
     SELECT r.reservation_id, COUNT(DISTINCT p.payment_id) as payment_count
     FROM reservation r
     JOIN payment p ON r.payment_id = p.payment_id
     GROUP BY r.reservation_id
     HAVING COUNT(DISTINCT p.payment_id) > 1;
조치: 1) DB UNIQUE 제약조건 추가
        ALTER TABLE payment ADD CONSTRAINT uq_order_id UNIQUE (order_id);
        -- orderId는 UUID로 생성되므로 중복 방지
     2) 예약의 payment_id가 NULL인지 비관적 락으로 확인
        SELECT r FROM Reservation r WHERE r.id = :id AND r.payment IS NULL
        FOR UPDATE
     3) 트랜잭션 격리 수준 SERIALIZABLE (성능 저하 우려)
```

#### 동일 시간대에 2명 이상 예약 성공 (Double Booking)
```
원인: Race Condition — Overlapping Schedule Check와 INSERT 사이 시간 갭
진단: DB에서 중복 예약 확인
     SELECT r.tutor_id, r.date, r.start_time, r.end_time, COUNT(*) 
     FROM reservation r
     WHERE r.status IN ('PENDING', 'CONFIRMED')
     GROUP BY r.tutor_id, r.date, r.start_time, r.end_time
     HAVING COUNT(*) > 1;
조치: 1) DB UNIQUE 제약조건 추가
        ALTER TABLE reservation 
        ADD CONSTRAINT uq_tutor_time_slot 
        UNIQUE (tutor_id, date, start_time, end_time)
        WHERE status IN ('PENDING', 'CONFIRMED');
     2) 비관적 락으로 튜터 스케줄 잠금
     3) Redis 분산 락으로 시간대별 잠금
```

#### Latency 급증 (P95 > 2000ms)
```
원인: DB Lock 대기 시간 증가 (트랜잭션 범위 과다)
진단: SHOW PROCESSLIST; 로 Lock 대기 쿼리 확인
조치: @Transactional 범위 축소 (SMS 발행은 트랜잭션 밖으로)
     → TransactionSynchronizationManager.registerSynchronization() 활용
```

#### RabbitMQ Consumer 처리 지연 (SMS 알림 병목)
```
원인: Thread Pool 고갈, 지수 백오프 재시도 쌓임
진단: RabbitMQ Management UI (http://localhost:15672)
     → Queues 탭에서 큐 깊이(Messages Ready) 확인
     → 큐 깊이 지속 증가 = Consumer 처리 속도 < 발행 속도
조치: maxConcurrentConsumers 증가 (RabbitMQConfig.java)
     또는 지수 백오프 지연 시간 단축
```

#### Thread Pool 고갈 (404/500 동시 급증)
```
원인: RabbitMQ Consumer 스레드 점유, DB 커넥션 풀 고갈
진단: Spring Actuator → /actuator/metrics/executor.active
     HikariCP → /actuator/metrics/hikaricp.connections.active
조치: server.tomcat.threads.max 증가
     spring.datasource.hikari.maximum-pool-size 증가
```

---

## 📈 측정 결과 기록 템플릿

```
테스트 일시: 2026-__-__ __:__
테스트 단계: [ Warmup | 기본 | 중간 | 스파이크 ]
vUser 수: ____
Duration: ____min

── nGrinder 결과 ──────────────────────────────────────────
TPS:              _______ req/s
Mean Latency:     _______ ms
P95 Latency:      _______ ms
Success Rate:     _______ %
201 Created:      _______ 건
409/400 (중복):   _______ 건
500 Error:        _______ 건

── DB 확인 ────────────────────────────────────────────────
Deadlock 발생:    [ 없음 | ___건 ]
중복 매칭 생성:   [ 없음 | ___건 ]
Lock Wait 시간:   _______ ms

── RabbitMQ 확인 ──────────────────────────────────────────
SMS 큐 깊이 최대: _______ 건
DLX 이동 건수:    _______ 건
Consumer 지연:    _______ ms

── 조치 사항 ──────────────────────────────────────────────
```

---

## 🛠️ 동시성 문제 해결 방법론

### 옵션 1: DB UNIQUE 제약조건 + 예외 처리 (권장 — 가장 간단)

#### Matching 테이블 (학생-튜터 중복 방지)
```sql
-- matching 테이블에 UNIQUE 제약 추가
ALTER TABLE matching
ADD CONSTRAINT uq_active_matching
UNIQUE (student_id, tutor_id, status);
-- 단, status별 유니크이므로 CANCELLED 이후 재신청은 가능
```

#### Reservation 테이블 (시간 슬롯 중복 예약 방지)
```sql
-- reservation 테이블에 시간대 중복 방지 UNIQUE 제약 추가
-- MySQL의 경우 조건부 UNIQUE INDEX 지원 안됨
-- PostgreSQL의 경우:
CREATE UNIQUE INDEX uq_tutor_time_slot 
ON reservation (tutor_id, date, start_time, end_time)
WHERE status IN ('PENDING', 'CONFIRMED');

-- MySQL 대안: 애플리케이션 레벨에서 상태 검증 + 낙관적 락
-- 또는 트리거로 중복 체크
```

### 옵션 2: 비관적 락 (Pessimistic Lock)

#### Matching에 대한 비관적 락
```java
// StudentRepository에 추가
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM StudentAccount s WHERE s.studentId = :id")
Optional<StudentAccount> findByIdWithLock(@Param("id") Long id);
```

#### Reservation에 대한 비관적 락 (시간대 중복 방지)
```java
// ReservationRepository에 추가
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
    SELECT r FROM Reservation r 
    WHERE r.tutorId = :tutorId 
    AND r.date = :date 
    AND r.status IN ('PENDING', 'CONFIRMED')
    AND (
        (r.startTime <= :startTime AND r.endTime > :startTime)
        OR (r.startTime < :endTime AND r.endTime >= :endTime)
        OR (r.startTime >= :startTime AND r.endTime <= :endTime)
    )
    """)
List<Reservation> findOverlappingReservationsWithLock(
    @Param("tutorId") Long tutorId,
    @Param("date") LocalDate date,
    @Param("startTime") LocalTime startTime,
    @Param("endTime") LocalTime endTime
);
```

### 옵션 3: Redis 분산 락 (고트래픽 환경)

#### Matching에 대한 분산 락
```java
// Redisson을 사용한 분산 락
RLock lock = redissonClient.getLock("matching:lock:" + studentId + ":" + tutorId);
try {
    lock.lock(5, TimeUnit.SECONDS);
    // 매칭 생성 로직
} finally {
    lock.unlock();
}
```

#### Reservation에 대한 분산 락 (시간 슬롯 단위 잠금)
```java
// 튜터-날짜-시간대별 분산 락
String lockKey = String.format("reservation:lock:%d:%s:%s:%s", 
    tutorId, date, startTime, endTime);
RLock lock = redissonClient.getLock(lockKey);
try {
    // 5초 타임아웃, 3초 임대 시간
    boolean acquired = lock.tryLock(5, 3, TimeUnit.SECONDS);
    if (!acquired) {
        throw new ConcurrencyException("해당 시간대 예약이 진행 중입니다. 잠시 후 다시 시도해주세요.");
    }
    
    // 1. Overlapping Schedule Check (DB 조회)
    if (hasOverlappingReservation(tutorId, date, startTime, endTime)) {
        throw new DuplicateReservationException("해당 시간대는 이미 예약되었습니다.");
    }
    
    // 2. Reservation 생성
    Reservation reservation = reservationRepository.save(newReservation);
    
    return reservation;
} finally {
    if (lock.isHeldByCurrentThread()) {
        lock.unlock();
    }
}
```

### 옵션 4: 트랜잭션 범위 최적화 (알림/외부 API 호출 분리)

#### SMS 알림을 트랜잭션 밖으로 분리
```java
// SMS 발행은 트랜잭션 커밋 후 실행 (TransactionSynchronization 활용)
TransactionSynchronizationManager.registerSynchronization(
    new TransactionSynchronizationAdapter() {
        @Override
        public void afterCommit() {
            smsNotificationProducer.sendLessonMatchedNotification(studentId, phone);
        }
    }
);
```

#### 구글 캘린더 연동을 비동기로 분리
```java
// 예약 생성 후 구글 캘린더 연동은 비동기 처리
@Transactional
public Reservation createReservation(CreateReservationRequest request) {
    // 1. 예약 생성 (트랜잭션 내부)
    Reservation reservation = reservationRepository.save(newReservation);
    
    // 2. 캘린더 연동은 트랜잭션 커밋 후
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                // 비동기로 구글 캘린더 이벤트 생성
                calendarEventProducer.sendCalendarEvent(reservation);
            }
        }
    );
    
    return reservation;
}

// RabbitMQ Consumer에서 처리
@RabbitListener(queues = "calendar.event.queue")
public void handleCalendarEvent(ReservationEvent event) {
    try {
        googleCalendarService.createEvent(event);
    } catch (Exception e) {
        log.error("구글 캘린더 연동 실패: {}", e.getMessage());
        // DLX로 재시도 또는 보상 트랜잭션
    }
}
```

---

## 🎯 ReservationConcurrencyTest 특화 검증 포인트

### 1. Double Booking 방지 검증
```sql
-- 테스트 후 중복 예약 확인 (0건이어야 정상)
SELECT 
    r.tutor_id,
    r.date,
    r.start_time,
    r.end_time,
    COUNT(*) as duplicate_count,
    GROUP_CONCAT(r.reservation_id) as reservation_ids
FROM reservation r
WHERE r.status IN ('PENDING', 'CONFIRMED')
GROUP BY r.tutor_id, r.date, r.start_time, r.end_time
HAVING COUNT(*) > 1;
```

### 2. Overlapping Schedule Check 성능 측정
```sql
-- Between Query 실행 계획 확인
EXPLAIN SELECT * FROM reservation
WHERE tutor_id = 1
AND date = '2026-10-15'
AND status IN ('PENDING', 'CONFIRMED')
AND (
    (start_time <= '19:00' AND end_time > '19:00')
    OR (start_time < '20:00' AND end_time >= '20:00')
    OR (start_time >= '19:00' AND end_time <= '20:00')
);

-- 인덱스 추가 권장
CREATE INDEX idx_reservation_tutor_date_status 
ON reservation (tutor_id, date, status);
```

### 3. 알림/캘린더 연동 병목 확인
- **RabbitMQ Management UI**: http://localhost:15672
  - Queues 탭에서 `reservation.notification.queue` 깊이 확인
  - Messages Ready가 계속 증가하면 Consumer 처리 속도 부족
- **애플리케이션 로그**:
  ```
  [ReservationService] 예약 생성 완료: reservationId=123 | elapsed=45ms
  [NotificationProducer] RabbitMQ 발행: reservationId=123 | elapsed=5ms
  [NotificationConsumer] SMS 전송 완료: reservationId=123 | elapsed=250ms
  [CalendarConsumer] 구글 캘린더 연동 완료: reservationId=123 | elapsed=1200ms ⚠️
  ```
  → 구글 캘린dar API 호출이 1200ms로 병목 발생 시 외부 API 타임아웃 또는 캐싱 고려

---

## � TutorSearchConcurrencyTest 특화 검증 포인트

### 1. Cache Stampede 검증
```sql
-- Redis 캐시 모니터링 (redis-cli)
MONITOR

-- 캐시 만료 시점에 동시 요청 확인
-- 200개 요청이 동시에 Cache Miss → ES 동시 쿼리 폭주
```

### 2. Elasticsearch 상태 확인
```bash
# ES Thread Pool 상태
curl -X GET "localhost:9200/_cat/thread_pool/search?v&h=node_name,name,active,queue,rejected"

# ES JVM Heap 사용량
curl -X GET "localhost:9200/_nodes/stats/jvm?pretty"

# 느린 쿼리 로그
curl -X GET "localhost:9200/_nodes/stats/indices/search?pretty"
```

### 3. Deep Pagination 성능 측정
```
page=0:    50ms  (정상)
page=10:   150ms (약간 증가)
page=100:  1500ms (Deep Pagination 병목)
page=500:  5000ms (Lucene Segment 지연 심각)

해결책: search_after 커서 페이징 도입
```

### 4. Redis Cache Hit Ratio 목표
```
목표: 80% 이상 (인기 키워드)

실제 측정:
- 캐시 TTL 5분: Hit Ratio 75%
- 캐시 TTL 10분: Hit Ratio 85%
- 캐시 TTL 30분: Hit Ratio 90%
```

---

## ⭐ ReviewConcurrencyTest 특화 검증 포인트

### 1. 평점 집계 정합성 검증
```sql
-- 테스트 후 평점 정합성 확인
SELECT 
    ta.tutor_id,
    ta.average_rating as db_rating,
    AVG(r.rating) as calculated_rating,
    ta.review_count as db_count,
    COUNT(r.review_id) as actual_count,
    ABS(ta.average_rating - AVG(r.rating)) as rating_diff,
    ABS(ta.review_count - COUNT(r.review_id)) as count_diff
FROM tutor_account ta
LEFT JOIN review r ON ta.tutor_id = r.tutor_id
GROUP BY ta.tutor_id, ta.average_rating, ta.review_count
HAVING rating_diff > 0.01 OR count_diff > 0;

-- 차이가 있으면 평점 집계 Race Condition 발생
```

### 2. 중복 리뷰 방지 검증
```sql
-- 동일 학생-튜터 쌍에 2개 이상 리뷰 확인 (0건이어야 정상)
SELECT 
    r.student_id,
    r.tutor_id,
    COUNT(*) as review_count,
    GROUP_CONCAT(r.review_id) as review_ids
FROM review r
GROUP BY r.student_id, r.tutor_id
HAVING COUNT(*) > 1;
```

### 3. 평점 집계 방식 비교

#### 방법 1: 실시간 Synchronous 업데이트 (현재 방식, Lock 경합)
```java
@Transactional
public Review createReview(CreateReviewRequest request) {
    // 1. 리뷰 생성
    Review review = reviewRepository.save(newReview);
    
    // 2. 평점 즉시 재계산 (모든 리뷰 AVG)
    Double avgRating = reviewRepository.calculateAverageRating(tutorId);
    Integer reviewCount = reviewRepository.countByTutorId(tutorId);
    
    // 3. 튜터 테이블 업데이트 (Lock 경합 발생!)
    tutorAccount.updateRating(avgRating, reviewCount);
    
    return review;
}
```

#### 방법 2: 비동기 이벤트 기반 (추천)
```java
@Transactional
public Review createReview(CreateReviewRequest request) {
    Review review = reviewRepository.save(newReview);
    
    // 리뷰 생성 이벤트 발행 (비동기)
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                eventPublisher.publishEvent(new ReviewCreatedEvent(tutorId));
            }
        }
    );
    
    return review;
}

// 별도 Consumer에서 배치 집계
@Async
@EventListener
public void handleReviewCreated(ReviewCreatedEvent event) {
    // Redis에 쌓아두고 주기적으로 배치 업데이트
    redisTemplate.opsForSet().add("review:aggregation:pending", event.getTutorId());
}
```

#### 방법 3: Redis 캐싱 + Eventual Consistency
```java
// 리뷰 작성 시 Redis 캐시만 업데이트
public Review createReview(CreateReviewRequest request) {
    Review review = reviewRepository.save(newReview);
    
    // Redis 캐시 무효화 (다음 조회 시 재계산)
    redisTemplate.delete("tutor:rating:" + tutorId);
    
    return review;
}

// 스케줄러로 주기적 동기화 (예: 1분마다)
@Scheduled(fixedRate = 60000)
public void syncRatings() {
    List<Long> tutorIds = redisTemplate.opsForSet()
        .members("tutor:rating:dirty");
    
    for (Long tutorId : tutorIds) {
        Double avgRating = reviewRepository.calculateAverageRating(tutorId);
        tutorAccount.updateRating(avgRating);
    }
}
```

---

## 🔐 AuthConcurrencyTest 특화 검증 포인트

### 1. 아이디 중복 체크 Race Condition 검증
```sql
-- 동일 아이디로 2개 이상 계정 생성 확인 (0건이어야 정상)
SELECT user_id, COUNT(*) as account_count
FROM user_account
GROUP BY user_id
HAVING COUNT(*) > 1;

-- 동일 이메일로 2개 이상 계정 확인
SELECT email, COUNT(*) as account_count
FROM user_account
GROUP BY email
HAVING COUNT(*) > 1;
```

### 2. Bcrypt CPU 부하 모니터링
```bash
# 서버 CPU 사용률 실시간 모니터링
top -p $(pgrep -f java)

# CPU 100% 도달 시간 측정
# 목표: 300 vUser에서도 CPU < 90%
```

### 3. 중복 방지 해결 방법

#### 방법 1: DB UNIQUE 제약조건 (가장 확실)
```sql
-- user_id, email UNIQUE 제약
ALTER TABLE user_account 
ADD CONSTRAINT uq_user_id UNIQUE (user_id);

ALTER TABLE user_account 
ADD CONSTRAINT uq_email UNIQUE (email);

-- Duplicate Key Exception 발생 시 409 응답
```

#### 방법 2: Redis 분산 락 (Redisson)
```java
public void signUp(SignUpRequest request) {
    String lockKey = "signup:lock:" + request.getUserId();
    RLock lock = redissonClient.getLock(lockKey);
    
    try {
        boolean acquired = lock.tryLock(5, 3, TimeUnit.SECONDS);
        if (!acquired) {
            throw new ConcurrencyException("다른 사용자가 동일 아이디로 가입 중입니다.");
        }
        
        // 1. 중복 체크
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new DuplicateException("이미 존재하는 아이디입니다.");
        }
        
        // 2. 회원가입
        userRepository.save(newUser);
        
    } finally {
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```

#### 방법 3: Idempotency Token
```java
// 클라이언트가 UUID 전송
POST /api/sign-up/student
{
  "idempotencyToken": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "test_user",
  ...
}

// 서버에서 토큰 검증
public void signUp(SignUpRequest request) {
    String token = request.getIdempotencyToken();
    
    // Redis에 토큰 저장 (24시간 TTL)
    Boolean isNew = redisTemplate.opsForValue()
        .setIfAbsent("idempotency:" + token, "processing", 24, TimeUnit.HOURS);
    
    if (!isNew) {
        throw new DuplicateException("이미 처리된 요청입니다.");
    }
    
    // 회원가입 처리
    userRepository.save(newUser);
}
```

### 4. Bcrypt CPU 부하 완화

#### 방법 1: Bcrypt Rounds 조정
```java
// 현재: 12 rounds (약 300ms)
BCryptPasswordEncoder(12)

// 최적화: 10 rounds (약 100ms)
// 보안과 성능의 균형
BCryptPasswordEncoder(10)
```

#### 방법 2: 비동기 회원가입
```java
@Async
public CompletableFuture<User> signUpAsync(SignUpRequest request) {
    // Bcrypt 해싱을 별도 스레드에서 처리
    String hashedPassword = passwordEncoder.encode(request.getPassword());
    User user = userRepository.save(newUser);
    return CompletableFuture.completedFuture(user);
}
```

#### 방법 3: Rate Limiting (회원가입 제한)
```java
// Spring Cloud Gateway 또는 Bucket4j 사용
@RateLimiter(name = "signup", fallbackMethod = "signupFallback")
public ResponseEntity<Void> signUp(SignUpRequest request) {
    // 초당 10건 제한
}
```

---

## 📈 전체 테스트 결과 기록 템플릿 (확장)

### 1. 중복 결제 방지 검증
```sql
-- 테스트 후 동일 예약에 여러 결제 확인 (0건이어야 정상)
SELECT 
    r.reservation_id,
    COUNT(DISTINCT p.payment_id) as payment_count,
    GROUP_CONCAT(p.order_id) as order_ids
FROM reservation r
LEFT JOIN payment_reservation pr ON r.reservation_id = pr.reservation_id
LEFT JOIN payment p ON pr.payment_id = p.payment_id
WHERE p.payment_status NOT IN ('CANCELLED', 'EXPIRED')
GROUP BY r.reservation_id
HAVING COUNT(DISTINCT p.payment_id) > 1;
```

### 2. 상태 전이 원자성 검증
```sql
-- 상태 전이 로그 확인 (애플리케이션 로그 또는 이벤트 테이블)
-- PENDING_TRANSFER → TRANSFER_CLAIMED → DONE 순서 보장되는지 확인

-- 비정상 상태 전이 확인
SELECT p.order_id, p.payment_status, p.created_at, p.transfer_claimed_at, p.confirmed_at
FROM payment p
WHERE p.payment_status = 'DONE'
AND p.transfer_claimed_at IS NULL;  -- CLAIMED 없이 바로 DONE (비정상)
```

### 3. orderId UNIQUE 제약 검증
```sql
-- orderId 중복 확인 (0건이어야 정상)
SELECT order_id, COUNT(*) as duplicate_count
FROM payment
GROUP BY order_id
HAVING COUNT(*) > 1;

-- UNIQUE 제약조건 확인
SHOW INDEX FROM payment WHERE Key_name = 'order_id';
```

### 4. 예약-결제 연관 정합성 검증
```sql
-- 결제 취소 시 예약이 정상적으로 릴리즈되었는지 확인
SELECT p.order_id, p.payment_status, r.reservation_id, r.payment_id
FROM payment p
LEFT JOIN reservation r ON r.payment_id = p.payment_id
WHERE p.payment_status IN ('CANCELLED', 'EXPIRED')
AND r.payment_id IS NOT NULL;  -- 취소된 결제가 여전히 예약과 연결됨 (버그!)

-- 정상: 취소된 결제는 reservation.payment_id = NULL이어야 함
```

### 5. 결제 상태 전이 Race Condition 시나리오

#### 시나리오 A: 이체 신고 더블 클릭
```
학생이 이체 완료 버튼을 빠르게 2회 클릭
→ Thread 1: PENDING_TRANSFER → TRANSFER_CLAIMED (성공)
→ Thread 2: TRANSFER_CLAIMED → TRANSFER_CLAIMED (실패, IllegalStateException)
```

#### 시나리오 B: 입금 확인/취소 동시 클릭
```
튜터가 실수로 "입금 확인" + "결제 취소" 동시 클릭
→ Thread 1: TRANSFER_CLAIMED → DONE (성공)
→ Thread 2: DONE → CANCELLED (실패, IllegalStateException)

또는 반대로:
→ Thread 1: TRANSFER_CLAIMED → CANCELLED (성공, 예약 릴리즈)
→ Thread 2: CANCELLED → DONE (실패, IllegalStateException)
```

#### 시나리오 C: 결제 만료 스케줄러와 이체 신고 충돌
```
학생이 마감 직전 이체 신고 + 동시에 만료 스케줄러 실행
→ Thread 1 (학생): PENDING_TRANSFER → TRANSFER_CLAIMED (성공)
→ Thread 2 (스케줄러): PENDING_TRANSFER → EXPIRED (실패, 이미 CLAIMED)

비관적 락 없으면:
→ Thread 1과 2가 동시에 PENDING_TRANSFER 읽음
→ 둘 다 상태 변경 시도 → DB Deadlock 또는 Lost Update
```

### 6. 해결 방법론: 결제 동시성

#### 방법 1: 비관적 락 (PaymentRepository)
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Payment p WHERE p.orderId = :orderId")
Optional<Payment> findByOrderIdWithLock(@Param("orderId") String orderId);

// 사용
Payment payment = paymentRepository.findByOrderIdWithLock(orderId)
    .orElseThrow(() -> new NotFoundException("결제를 찾을 수 없습니다."));

// 상태 전이 로직 (엔티티 내부에서 검증)
payment.claimTransfer();  // IllegalStateException 던짐 if 잘못된 상태
```

#### 방법 2: 낙관적 락 (@Version)
```java
@Entity
public class Payment {
    @Version
    private Long version;  // JPA가 자동으로 증가
    
    // ... 기존 필드
}

// 충돌 발생 시 OptimisticLockException
// → @Retryable로 재시도 또는 사용자에게 "다른 사용자가 수정 중" 메시지
```

#### 방법 3: DB 트리거 (MySQL)
```sql
-- 상태 전이 유효성 검증 트리거
DELIMITER $$
CREATE TRIGGER payment_status_transition_check
BEFORE UPDATE ON payment
FOR EACH ROW
BEGIN
    IF OLD.payment_status = 'DONE' AND NEW.payment_status != 'DONE' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '완료된 결제는 변경할 수 없습니다.';
    END IF;
    
    IF OLD.payment_status = 'CANCELLED' AND NEW.payment_status != 'CANCELLED' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '취소된 결제는 복구할 수 없습니다.';
    END IF;
END$$
DELIMITER ;
```

#### 방법 4: 결제 만료 스케줄러 개선
```java
@Scheduled(cron = "0 */10 * * * *")  // 10분마다 실행
public void expireOldPayments() {
    LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
    
    // 비관적 락으로 PENDING_TRANSFER 상태만 조회
    List<Payment> expiredPayments = paymentRepository
        .findAllByPaymentStatusAndCreatedAtBeforeWithLock(
            PaymentStatus.PENDING_TRANSFER, cutoff);
    
    for (Payment payment : expiredPayments) {
        try {
            payment.expire();  // PENDING_TRANSFER → EXPIRED 전이
            paymentRepository.save(payment);
        } catch (IllegalStateException e) {
            // 다른 트랜잭션이 이미 상태 변경 → 스킵
            log.info("결제 {}는 이미 처리됨", payment.getOrderId());
        }
    }
}
```
