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
import com.lessonmatchingplatform.lesson_matching_platform.global.cache.DistributedCacheLockHelper;
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
    private final DistributedCacheLockHelper cacheLockHelper;

    // Stampede 동시 진입자 수 모니터링 카운터
    private final AtomicInteger homeConcurrent = new AtomicInteger(0);

    /**
     * 메인 홈 통합 데이터 조회 - Redisson 분산 락 적용
     *
     * [Cache Stampede 분산 락 방어: Redisson + Double-Checked Locking]
     * 캐시 히트 시 락 없이 즉시 반환하며, 캐시 만료 순간 오직 단 1개의 스레드만
     * Redisson 분산 락을 획득하여 DB를 조회하고 캐시에 저장합니다.
     * 대기하던 다른 스레드들은 락 획득 후 2차 확인(Double-Check)에서 캐시를 읽어 즉시 반환합니다.
     */
    public MainHomeResponse getMainHomeData(Long categoryId) {
        if (categoryId != null && categoryId > 0 && !categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("존재하지 않는 카테고리 ID입니다. categoryId: " + categoryId);
        }

        Object cacheKey = categoryId != null ? categoryId : "ALL";

        return cacheLockHelper.getOrLoad(
                "mainHome",
                cacheKey,
                MainHomeResponse.class,
                () -> loadMainHomeData(categoryId)
        );
    }

    @Transactional(readOnly = true)
    public MainHomeResponse loadMainHomeData(Long categoryId) {
        int current = homeConcurrent.incrementAndGet();
        long start = System.currentTimeMillis();
        try {
            List<TutorCardDto> trending = getTrendingTutorsByCategory(categoryId);
            List<TutorCardDto> rookie   = getRookieTutorsByCategory(categoryId);

            long dbTime = System.currentTimeMillis() - start;
            log.warn("[CACHE MISS:mainHome][redisson-lock] categoryId={} | concurrent={} | dbTime={}ms | thread={}",
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
