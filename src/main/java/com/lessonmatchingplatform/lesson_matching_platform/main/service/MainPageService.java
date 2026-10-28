package com.lessonmatchingplatform.lesson_matching_platform.main.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.GoalTutor;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorLessonPrice;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.CategoryTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.GoalTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.SubjectTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.GoalTutorRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.TutorLessonPriceRepository;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.CategoryTutor;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.SubjectTutor;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.CategoryTutorRepository;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.SubjectTutorRepository;
import com.lessonmatchingplatform.lesson_matching_platform.main.dto.TutorCardDto;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.CategoryRepository;
import com.lessonmatchingplatform.lesson_matching_platform.main.dto.TutorLessonPriceRangeDto;
import com.lessonmatchingplatform.lesson_matching_platform.main.dto.response.MainHomeResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class MainPageService {

        private final CategoryRepository categoryRepository;
        private final TutorsRepository tutorsRepository;
        private final GoalTutorRepository goalTutorRepository;
        private final CategoryTutorRepository categoryTutorRepository;
        private final SubjectTutorRepository subjectTutorRepository;
        private final TutorLessonPriceRepository tutorLessonPriceRepository;

        // Spring AOP Self-Invocation(자기 호출 시 @Cacheable 무시되는 문제) 해결을 위한 프록시 공급자
        private final ObjectProvider<MainPageService> selfProvider;

        // Cache Stampede 모니터링용 동시 진입자 카운터
        private final AtomicInteger trendingConcurrent = new AtomicInteger(0);
        private final AtomicInteger rookieConcurrent = new AtomicInteger(0);

        @Transactional(readOnly = true)
        /**
         * 메인 홈 통합 데이터 조회
         *
         * [성능 최적화 주의점]
         * 1. 여기에 @Transactional을 붙이면 캐시를 조회하기도 전에 톰캣 스레드가 DB 커넥션을 물고 들어가 풀이 고갈됩니다.
         * 따라서 상위 진입 메서드는 트랜잭션 없이 캐시 조회를 먼저 수행합니다.
         * 2. 내부 비동기 호출 시 this.get... 대신 selfProvider.getObject().get...를 호출해야
         * Spring AOP 프록시를 정상적으로 경유하여 @Cacheable(sync=true) 락이 작동합니다.
         */
        public MainHomeResponse getMainHomeData(Long categoryId) {
                if (categoryId != null && categoryId > 0 && !categoryRepository.existsById(categoryId)) {
                        throw new IllegalArgumentException("존재하지 않는 카테고리 ID입니다. categoryId: " + categoryId);
                }

                // 1. 인기 선생님 조회를 별도 쓰레드에 비동기로 일시킴 (스레드 1)
                MainPageService self = selfProvider.getObject();

                // 1. 프록시를 통해 호출 → @Cacheable(sync=true) 정상 작동
                CompletableFuture<List<TutorCardDto>> trendingFuture = CompletableFuture
                                .supplyAsync(() -> getTrendingTutorsByCategory(categoryId));
                CompletableFuture.supplyAsync(() -> self.getTrendingTutorsByCategory(categoryId));

                // 2. 루키 선생님 조회를 별도 쓰레드에 비동기로 일시킴 (스레드 2)
                // 2. 프록시를 통해 호출 → @Cacheable(sync=true) 정상 작동
                CompletableFuture<List<TutorCardDto>> rookieFuture = CompletableFuture
                                .supplyAsync(() -> getRookieTutorsByCategory(categoryId));
                CompletableFuture.supplyAsync(() -> self.getRookieTutorsByCategory(categoryId));

                // 3. 두 작업이 모두 끝날 때까지 기다린 뒤 결과 합치기
                // 3. 두 작업이 모두 끝날 때까지 대기
                CompletableFuture.allOf(trendingFuture, rookieFuture).join();

                return MainHomeResponse.of(
                                trendingFuture.join(),
                                rookieFuture.join());
        }

        // sync = true 적용: 캐시 만료 시 단 1개의 스레드만 DB 조회를 수행하고, 나머지 스레드는 대기 후 캐시된 결과를 공유
        @Cacheable(value = "trendingTutors", key = "#categoryId != null ? #categoryId : 'ALL'", sync = true)
        @Transactional(readOnly = true)
        public List<TutorCardDto> getTrendingTutorsByCategory(Long categoryId) {
                if (categoryId != null && categoryId > 0 && !categoryRepository.existsById(categoryId)) {
                        throw new IllegalArgumentException("존재하지 않는 카테고리 ID입니다. categoryId: " + categoryId);
                }

                int current = trendingConcurrent.incrementAndGet();
                long start = System.currentTimeMillis();
                try {
                        List<TutorAccount> trendingTutors = tutorsRepository
                                        .findTop8ByCategoryIdOrderByMatchingCountDesc(categoryId);
                        long dbTime = System.currentTimeMillis() - start;

                        log.warn("[CACHE MISS:trending] categoryId={} | concurrent={} | dbTime={}ms | thread={}",
                                        categoryId, current, dbTime, Thread.currentThread().getName());

                        if (trendingTutors.isEmpty()) {
                                return List.of();
                        }
                        return mapTutorCardDetails(trendingTutors);
                } finally {
                        trendingConcurrent.decrementAndGet();
                }
        }

        // 카테고리별로 가장 최신에 등록한 선생님 조회
        // sync = true 적용: Stampede 방어
        @Cacheable(value = "rookieTutors", key = "#categoryId != null ? #categoryId : 'ALL'", sync = true)
        @Transactional(readOnly = true)
        public List<TutorCardDto> getRookieTutorsByCategory(Long categoryId) {
                if (categoryId != null && categoryId > 0 && !categoryRepository.existsById(categoryId)) {
                        throw new IllegalArgumentException("존재하지 않는 카테고리 ID입니다. categoryId: " + categoryId);
                }

                int current = rookieConcurrent.incrementAndGet();
                long start = System.currentTimeMillis();
                try {
                        List<TutorAccount> rookieTutors = tutorsRepository.findTop8RookieTutorsByCategoryId(categoryId);
                        long dbTime = System.currentTimeMillis() - start;

                        log.warn("[CACHE MISS:rookie] categoryId={} | concurrent={} | dbTime={}ms | thread={}",
                                        categoryId, current, dbTime, Thread.currentThread().getName());

                        if (rookieTutors.isEmpty()) {
                                return List.of();
                        }
                        return mapTutorCardDetails(rookieTutors);
                } finally {
                        rookieConcurrent.decrementAndGet();
                }
        }

        private List<TutorCardDto> mapTutorCardDetails(List<TutorAccount> tutorAccounts) {
                List<Long> tutorIds = tutorAccounts.stream().map(TutorAccount::getTutorId).toList();

                List<GoalTutor> goalTutorList = goalTutorRepository.findAllByTutorIdIn(tutorIds);
                Map<Long, List<GoalTypeDto>> goalTypeMap = goalTutorList.stream()
                                .collect(Collectors.groupingBy(
                                                goalTutor -> goalTutor.getTutorAccount().getTutorId(),
                                                Collectors.mapping(
                                                                goalTutor -> GoalTypeDto.of(goalTutor.getLessonGoal()),
                                                                Collectors.toList())));

                List<CategoryTutor> categoryTutorList = categoryTutorRepository
                                .findAllByTutorAccount_TutorIdIn(tutorIds);
                Map<Long, List<CategoryTypeDto>> categoryTypeMap = categoryTutorList.stream()
                                .collect(Collectors.groupingBy(
                                                categoryTutor -> categoryTutor.getTutorAccount().getTutorId(),
                                                Collectors.mapping(
                                                                categoryTutor -> CategoryTypeDto
                                                                                .from(categoryTutor.getCategory()),
                                                                Collectors.toList())));

                List<SubjectTutor> subjectTutorList = subjectTutorRepository.findAllByTutorAccount_TutorIdIn(tutorIds);
                Map<Long, List<SubjectTypeDto>> subjectTypeMap = subjectTutorList.stream()
                                .collect(Collectors.groupingBy(
                                                subjectTutor -> subjectTutor.getTutorAccount().getTutorId(),
                                                Collectors.mapping(
                                                                subjectTutor -> SubjectTypeDto
                                                                                .from(subjectTutor.getSubject()),
                                                                Collectors.toList())));

                List<TutorLessonPrice> tutorLessonPriceList = tutorLessonPriceRepository
                                .findAllByTutorAccount_TutorIdIn(tutorIds);
                Map<Long, TutorLessonPriceRangeDto> tutorLessonPriceMap = tutorLessonPriceList.stream()
                                .collect(Collectors.groupingBy(
                                                tutorLessonPrice -> tutorLessonPrice.getTutorAccount().getTutorId(),
                                                Collectors.collectingAndThen(
                                                                Collectors.toList(),
                                                                TutorLessonPriceRangeDto::from)));

                return tutorAccounts.stream()
                                .map(tutorAccount -> TutorCardDto.of(
                                                tutorAccount,
                                                goalTypeMap.getOrDefault(tutorAccount.getTutorId(), List.of()),
                                                categoryTypeMap.getOrDefault(tutorAccount.getTutorId(), List.of()),
                                                subjectTypeMap.getOrDefault(tutorAccount.getTutorId(), List.of()),
                                                tutorLessonPriceMap.getOrDefault(tutorAccount.getTutorId(),
                                                                TutorLessonPriceRangeDto.from(List.of()))))
                                .toList();
        }
}
