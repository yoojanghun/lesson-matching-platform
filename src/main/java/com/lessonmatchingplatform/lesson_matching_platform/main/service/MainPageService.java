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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;



@Slf4j
@RequiredArgsConstructor
@Service
public class MainPageService {

    private final CategoryRepository categoryRepository;
    private final TutorsRepository tutorsRepository;
    private final GoalTutorRepository goalTutorRepository;
    private final CategoryTutorRepository categoryTutorRepository;
    private final SubjectTutorRepository subjectTutorRepository;
    private final TutorLessonPriceRepository tutorLessonPriceRepository;

    // Stampede 동시 진입자 수 모니터링 카운터
    private final AtomicInteger homeConcurrent = new AtomicInteger(0);

    /**
     * 메인 홈 통합 데이터 조회 - sync = true 적용
     *
     * [Cache Stampede 완전 방어]
     * CompletableFuture 비동기 풀에 작업을 넘기지 않고,
     * 메인 홈 전체 응답에 대해 sync = true 락을 적용합니다.
     * 캐시 만료 순간 오직 단 1개의 스레드만 DB를 조회하여 완성본을 채우고,
     * 나머지 99~199개 스레드는 로컬 락에서 대기 후 캐시 결과를 공유받아 즉시 반환합니다.
     */
    @Cacheable(value = "mainHome", key = "#categoryId != null ? #categoryId : 'ALL'", sync = true)
    @Transactional(readOnly = true)
    public MainHomeResponse getMainHomeData(Long categoryId) {
        if (categoryId != null && categoryId > 0 && !categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("존재하지 않는 카테고리 ID입니다. categoryId: " + categoryId);
        }

        int current = homeConcurrent.incrementAndGet();
        long start = System.currentTimeMillis();
        try {
            List<TutorCardDto> trending = getTrendingTutorsByCategory(categoryId);
            List<TutorCardDto> rookie   = getRookieTutorsByCategory(categoryId);

            long dbTime = System.currentTimeMillis() - start;
            log.warn("[CACHE MISS:mainHome] categoryId={} | concurrent={} | dbTime={}ms | thread={}",
                    categoryId, current, dbTime, Thread.currentThread().getName());

            return MainHomeResponse.of(trending, rookie);
        } finally {
            homeConcurrent.decrementAndGet();
        }
    }

    public List<TutorCardDto> getTrendingTutorsByCategory(Long categoryId) {
        if (categoryId != null && categoryId > 0 && !categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("존재하지 않는 카테고리 ID입니다. categoryId: " + categoryId);
        }

        List<TutorAccount> trendingTutors = tutorsRepository.findTop8ByCategoryIdOrderByMatchingCountDesc(categoryId);
        if (trendingTutors.isEmpty()) {
            return List.of();
        }
        return mapTutorCardDetails(trendingTutors);
    }

    public List<TutorCardDto> getRookieTutorsByCategory(Long categoryId) {
        if (categoryId != null && categoryId > 0 && !categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("존재하지 않는 카테고리 ID입니다. categoryId: " + categoryId);
        }

        List<TutorAccount> rookieTutors = tutorsRepository.findTop8RookieTutorsByCategoryId(categoryId);
        if (rookieTutors.isEmpty()) {
            return List.of();
        }
        return mapTutorCardDetails(rookieTutors);
    }

    private List<TutorCardDto> mapTutorCardDetails(List<TutorAccount> tutorAccounts) {
        List<Long> tutorIds = tutorAccounts.stream().map(TutorAccount::getTutorId).toList();

        List<GoalTutor> goalTutorList = goalTutorRepository.findAllByTutorIdIn(tutorIds);
        Map<Long, List<GoalTypeDto>> goalTypeMap = goalTutorList.stream()
                .collect(Collectors.groupingBy(
                        goalTutor -> goalTutor.getTutorAccount().getTutorId(),
                        Collectors.mapping(
                                goalTutor -> GoalTypeDto.of(goalTutor.getLessonGoal()),
                                Collectors.toList()
                        )
                ));

        List<CategoryTutor> categoryTutorList = categoryTutorRepository.findAllByTutorAccount_TutorIdIn(tutorIds);
        Map<Long, List<CategoryTypeDto>> categoryTypeMap = categoryTutorList.stream()
                .collect(Collectors.groupingBy(
                        categoryTutor -> categoryTutor.getTutorAccount().getTutorId(),
                        Collectors.mapping(
                                categoryTutor -> CategoryTypeDto.from(categoryTutor.getCategory()),
                                Collectors.toList()
                        )
                ));

        List<SubjectTutor> subjectTutorList = subjectTutorRepository.findAllByTutorAccount_TutorIdIn(tutorIds);
        Map<Long, List<SubjectTypeDto>> subjectTypeMap = subjectTutorList.stream()
                .collect(Collectors.groupingBy(
                        subjectTutor -> subjectTutor.getTutorAccount().getTutorId(),
                        Collectors.mapping(
                                subjectTutor -> SubjectTypeDto.from(subjectTutor.getSubject()),
                                Collectors.toList()
                        )
                ));

        List<TutorLessonPrice> tutorLessonPriceList = tutorLessonPriceRepository.findAllByTutorAccount_TutorIdIn(tutorIds);
        Map<Long, TutorLessonPriceRangeDto> tutorLessonPriceMap = tutorLessonPriceList.stream()
                .collect(Collectors.groupingBy(
                        tutorLessonPrice -> tutorLessonPrice.getTutorAccount().getTutorId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                TutorLessonPriceRangeDto::from
                        )
                ));

        return tutorAccounts.stream()
                .map(tutorAccount -> TutorCardDto.of(
                        tutorAccount,
                        goalTypeMap.getOrDefault(tutorAccount.getTutorId(), List.of()),
                        categoryTypeMap.getOrDefault(tutorAccount.getTutorId(), List.of()),
                        subjectTypeMap.getOrDefault(tutorAccount.getTutorId(), List.of()),
                        tutorLessonPriceMap.getOrDefault(tutorAccount.getTutorId(), TutorLessonPriceRangeDto.from(List.of()))
                ))
                .toList();
    }

    /**
     * 스케줄러 선갱신 전용 메서드 - @Cacheable 없음
     *
     * 스케줄러(MainPageCacheRefreshScheduler)에서 직접 DB를 조회한 뒤
     * CacheManager에 put()으로 캐시를 주입할 때 사용합니다.
     * AOP 프록시를 경유하지 않으므로 @Cacheable/@CachePut 대신
     * 순수 DB 조회 결과만 반환합니다.
     */
    @Transactional(readOnly = true)
    public MainHomeResponse buildMainHomeData(Long categoryId) {
        List<TutorCardDto> trending = getTrendingTutorsByCategory(categoryId);
        List<TutorCardDto> rookie   = getRookieTutorsByCategory(categoryId);
        return MainHomeResponse.of(trending, rookie);
    }
}
