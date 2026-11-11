# 레슨 매칭 플랫폼

학생과 튜터 간의 레슨 매칭, 일정 조율 및 결제 기능 등을 제공하는 중개 플랫폼

---

## 📌 주요 기능

- **역할별 계정 분리**: 학생과 튜터 계정으로 분리되어 튜터는 일정/과목 프로필을 관리하고 학생은 맞춤 튜터를 탐색합니다.
- **인기 튜터 추천**: 최근 매칭 수, 평균 평점, 누적 리뷰 수, 최신 가입 일자 등에 각각 가중치를 부여하여 인기 / 신규 튜터를 추천합니다.
- **1:1 사전 매칭**: 학생이 신청 후 튜터가 이를 수락해야만 수업을 진행할 수 있는 매칭 기반 서비스입니다.
- **맞춤형 시간 예약**: 사전 매칭이 완료되면 학생이 선생님 스케줄표에서 레슨 시간을 선택하여 중복 없이 예약합니다.
- **안전 결제 및 리뷰**: 완료된 레슨에 대한 결제 요청과 튜터의 신뢰도를 나타내는 평점/리뷰 등록 시스템을 제공합니다.

---

## 📅 프로젝트 개요

- **기간**: 2026.01.29 ~ 현재
- **프로젝트 인원**: 1명

---

## 🛠️ 담당 역할

- 학생·튜터 계정 분리, 1:1 사전 매칭, 레슨 예약, 결제 및 수강 후기 시스템의 RESTful API 설계, ERD 작성
- INSERT/DELETE/UPDATE보다 검색이 많이 일어나는 도메인에 대한 B-tree 기반 index 설정
- WebSocket(STOMP)을 이용한 1:1 실시간 채팅 기능 및 연결 생명주기 관리
- RabbitMQ를 구축하여 매칭·결제 완료 알림을 메인 트랜잭션과 비동기 분리
- nGrinder 기반으로 동시성 제어 문제가 발생할 가능성 있는 모든 상황과 병목이 예상되는 지점에 대한 부하 테스트
- LangChain4j & MongoDB를 연동하여 학생의 자연어 요청을 벡터화 한 후, 유사도를 분석하는 RAG 기반 튜터 추천 기능 구현

---

## 💻 사용 기술

- **Spring Boot 3.4.3**: 웹 애플리케이션 개발을 위한 풍부한 생태계를 기반으로 백엔드 개발
- **MySQL 8.0**: 회원, 결제, 수강 신청 등 데이터의 ACID 트랜잭션과 일관성 보장
- **QueryDSL**: 복잡한 동적 쿼리를 컴파일 타임의 안정성을 확보하여 자바 코드로 작성
- **Redis (+ Redisson)**: 인메모리 기반 빠른 캐싱, 분산락을 이용한 동시성 제어를 위해
- **MongoDB**: 유연한 스키마 구조를 바탕으로 1:1 채팅 메시지 저장 및 AI 시멘틱 튜터 추천을 위한 벡터 저장소로 활용
- **RabbitMQ**: 레슨 매칭 완료, 결제 완료 등의 알림을 비동기 처리하여, 외부 통신 지연이 메인 트랜잭션을 블로킹 하지 않도록 처리
- **nGrinder**: 대규모 동시 요청 부하 환경을 재현하여 동시성 제어, 캐시 스탬피드 극복 등을 위한 성능 테스트
- **Docker**: 애플리케이션 실행 환경을 컨테이너화 하여 개발 운영 환경 간의 이식성을 보장하기 위해
- **Elasticsearch**: 역색인을 이용하여 튜터의 과목, 자기소개, 활동 지역 등 복합 텍스트 데이터에 대한 빠른 검색을 제공하기 위해
- **Spring Security & OAuth2 / JWT**: Stateless JWT 토큰 기반의 인증과 OAuth2를 활용한 검증된 외부 서비스의 인증 정보로 안전하게 사용자를 식별하기 위해
- **WebSocket & STOMP**: HTTP 폴링 오버헤드 없이 학생과 튜터 간의 양방향 실시간 1:1 채팅을 경량화 하기 위해 사용
- **GitHub Actions**: 코드 push, 빌드, 테스트, 배포까지의 파이프라인을 자동화하여 개발 생산성을 높이기 위해

---

## ⚡ 핵심 트러블 슈팅 및 문제 해결

자세한 기술 검증 및 벤치마크 결과는 각 문서 링크에서 확인하실 수 있습니다.

1. **[동시성 제어 전략 선정 및 Trade-off 분석](./docs/troubleshooting/01-concurrency-control.md)**
   - Redis 분산 Lock vs 비관적 Lock vs DB Atomic UPDATE 성능 및 정합성

2. **[Cache Stampede 방어 전략 선정](./docs/troubleshooting/02-cache-stampede.md)**
   - Redis Scheduler 선 갱신 vs Soft / Hard TTL 적용을 통한 대규모 동시 요청 병목 해결

3. **[DB 격리 수준 변화를 통한 Race Condition 문제 해결](./docs/troubleshooting/03-db-isolation-level.md)**
   - Repeatable Read vs Read Committed 격리 수준에 따른 데이터 일관성 검증

4. **[추가 설계 결정](./docs/troubleshooting/04-extra-design.md)**
   - fetchJoin과 Paging을 함께 썼을 때 발생하는 OOM 문제
   - CPU 성능에 맞추어 Bcrypt Hashing을 위한 thread 할당
   - 결제 / 매칭 SMS 알림을 위한 RabbitMQ 비동기 메시지 큐 도입

---

## 📝 프로젝트 회고

### 🔍 시스템 검증

* **MySQL 기본 Isolation Level(`Repeatable Read`) 검증**
  * Next-Key Lock 활용으로 인한 트랜잭션 간 Gap Lock 점유 문제(Deadlock) 가능성을 검토.
  * 모든 락 사용 시 `UNIQUE` 동등 조건으로 대상을 검색하고 기존 Row에 대해서만 `UPDATE`, `SELECT FOR UPDATE`를 수행하여 문제가 없음을 확인 (대상 Row가 없는 경우 즉시 예외 발생 및 Rollback 처리).
* **매칭 신청 정합성 테스트**
  * Redisson Lock과 DB Atomic Update의 이중 방어 체계를 구축.
  * 기존에 3쌍의 튜터-학생 관계에서 `PENDING` 매칭이 각각 2개 이상 생성되던 문제를 방지하여, 반드시 1개의 `PENDING` 매칭만 존재하도록 방어.
* **매칭 상태 전이 테스트**
  * 500개의 `PENDING` 매칭 데이터를 `ACCEPTED`/`REJECTED` 상태로 전환 시, Publish Rate(초당 약 40~43개)와 Consumer ACK Rate(초당 약 39~44개)가 거의 일치하며 메시지 브로커가 안정적으로 작동함을 측정을 통해 확인.
* **결제 동시성 테스트**
  * 동일 매칭에 대한 동시 결제 요청 시 단 1건만 허용되어야 하나, initial 테스트에서 `payment` 테이블에 4건의 결제 대기 Row가 생성되는 문제를 발견.
  * 비관적 Lock 도입을 통해 1건의 결제 대기 Row만 생성되도록 해결. (새 Row를 생성하는 `INSERT` 특성상 `@Version` 기반 낙관적 Lock 충돌 감지가 동작하지 않음을 확인했으며, 비관적 Lock을 사용하더라도 임계 구역 작업이 짧아 Lock Overhead는 미비하다고 판단.)

---

### 💡 배운 점

* **상황에 맞는 동시성 제어 전략 채택**
  * 비관적 Lock, Redisson 분산 락, DB Atomic Update 등 도메인 특성과 대규모 트래픽 환경에 적합한 기법을 적용하는 경험을 함.
* **DB Isolation Level 및 Anomaly 이해**
  * Isolation Level에 따른 Anomaly(Dirty Read, Non-Repeatable Read, Phantom Read, Deadlock, Lock 대기 지연 등)를 고려하여 최적의 격리 수준을 결정.
* **부하 테스트 기반 병목 지점 추출**
  * nGrinder 부하 테스트를 통해 각 API의 TPS, 응답 시간, CPU/DB 사용률, DB Connection 상태 등의 지표를 수집하고 병목 구간을 탐색.
* **동기 Thread Pool 고갈 방지 및 장애 격리**
  * Bcrypt 비밀번호 해싱 스레드 제어, RabbitMQ 기반 SMS 알림 비동기 처리, 백그라운드 배치 스케줄러, 튜터 프로필 백그라운드 비동기 갱신 등을 활용해 핵심 애플리케이션의 Thread 고갈과 장애 전파를 방지.

---

### 🎯 아쉬운 점 및 향후 개선 방향

* **Scale-Out 환경에서의 WebSocket & Redis Pub/Sub 구조 검증 미비**
  * 다중 인스턴스 환경에서 분산된 사용자가 접속했을 때(Scale Out), Redis Pub/Sub를 통한 메시지 전파 시 MongoDB 대화 내역 중복 저장 문제 및 미읽음 카운트 동시성 갱신(Lost Update)에 대한 추가 검증 필요
* **LLM 연동 시 외부 API 부하 및 동기 호출 리스크**
  * 현재 사용자 자연어 요청을 받아 Mongo Vector DB 필터링 후 LLM(OpenAI API)으로 최종 튜터 추천 및 사유를 생성. 그런데 실제 운영 환경에서 외부 API(OpenAi)를 동기식으로 호출할 경우, 트래픽 급증 시 Rate Limit 초과, 비용 급증 및 Tomcat Thread 고갈을 방지하기 위해 메시지 큐를 버퍼로 두는 비동기 속도 제어 방식 도입을 고려하고 있음.
  * 또한 Semantic Caching을 도입하여 벡터 간 코사인 유사도가 높은 유사 질의 요청 시 LLM 재호출 없이 캐시된 응답을 즉시 반환하도록 개선할 계획