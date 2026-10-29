package com.lessonmatchingplatform.lesson_matching_platform.tutor.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.GoalTutor;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorLessonPrice;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.CategoryTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.GoalTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.SubjectTypeDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.TutorProfileResponse;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.GoalTutorRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.TutorLessonPriceRepository;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.CategoryTutor;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.SubjectTutor;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.CategoryTutorRepository;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.SubjectTutorRepository;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.CachedReviewSliceDto;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.ReviewResponse;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.ReviewRepository;
import com.lessonmatchingplatform.lesson_matching_platform.main.dto.TutorCardDto;
import com.lessonmatchingplatform.lesson_matching_platform.main.dto.TutorLessonPriceRangeDto;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.request.TutorSearchCondition;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;
import org.springframework.cache.annotation.Cacheable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class TutorsService {

    private final TutorsRepository tutorsRepository;
    private final ReviewRepository reviewRepository;
    private final GoalTutorRepository goalTutorRepository;
    private final CategoryTutorRepository categoryTutorRepository;
    private final SubjectTutorRepository subjectTutorRepository;
    private final TutorLessonPriceRepository tutorLessonPriceRepository;

    private final AtomicInteger dbQueryCount = new java.util.concurrent.atomic.AtomicInteger();
    private final AtomicInteger dbReviewQueryCount = new java.util.concurrent.atomic.AtomicInteger();

    // 공개 강사 상세 프로필 조회 (리뷰 제외) - Redis 캐싱 적용
    // [Cache Stampede 방어] sync = true 적용으로 만료 순간 단 1개의 스레드만 DB를 조회하고 나머지는 대기 후 캐시 반환
    @Cacheable(value = "tutorDetail", key = "#tutorId", sync = true)
    @Transactional(readOnly = true)
    public TutorProfileResponse getTutorProfile(Long tutorId) {
        log.warn("🔥 [CACHE MISS] DB 직접 조회 발생! tutorId={} | thread={} | 누적DB조회수={}",
                tutorId, Thread.currentThread().getName(), dbQueryCount.incrementAndGet());

        try {
            // DB 쿼리/연산 지연 재현 (Cache Stampede 테스트 동안 만료 시 스레드 경합을 관찰하기 위한 장치: sleep 150ms 조건)
            Thread.sleep(150);
        } catch (InterruptedException ignored) {}

        TutorAccount tutorAccount = tutorsRepository.findProfileById(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 강사를 찾을 수 없습니다. id=" + tutorId));

        return categoryTutorRepository.findProfileResponseById(tutorId);
    }

    /**
     * 튜터 리뷰 첫 페이지 캐시 조회 (pageNumber == 0 && pageSize == 5 만 캐싱)
     *
     * [캐싱 전략]
     * - condition: pageNumber == 0 && pageSize == 5 (기본 페이징만 캐싱, 키 임의 증식 방지)
     * - key: #tutorId (tutorReviews::tutorId 형태로 단순화)
     * - unless 없음: 리뷰 0건인 튜터도 빈 목록으로 캐싱하여 무분별한 DB 콜드 미스 방지 및 sync=true와 안전하게 호환
     * - 반환 타입: CachedReviewSliceDto (SliceImpl의 GenericJackson2JsonRedisSerializer 역직렬화 오류 방지)
     */
    @Cacheable(
            value     = "tutorReviews",
            key       = "#tutorId",
            condition = "#pageable.pageNumber == 0 && #pageable.pageSize == 5"
    )
    @Transactional(readOnly = true)
    public CachedReviewSliceDto findCachedFirstPageReviews(Long tutorId, Pageable pageable) {
        log.warn("🔥 [CACHE MISS:review] DB 리뷰 직접 조회 발생! tutorId={} | thread={} | 누적리뷰DB조회수={}",
                tutorId, Thread.currentThread().getName(), dbReviewQueryCount.incrementAndGet());
        Slice<ReviewResponse> slice = reviewRepository.findReviewsByTutorId(tutorId, pageable);
        return CachedReviewSliceDto.from(slice);
    }

    @Transactional(readOnly = true)
    public Slice<ReviewResponse> findReviewsByTutorId(Long tutorId, Pageable pageable) {
        if (pageable.getPageNumber() == 0 && pageable.getPageSize() == 5) {
            return findCachedFirstPageReviews(tutorId, pageable).toSlice(pageable);
        }
        return reviewRepository.findReviewsByTutorId(tutorId, pageable);
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

    // 선생님 조회 필터로 선생님 리스트 조회(백업용)
    @Transactional(readOnly = true)
    public Page<TutorCardDto> getTutorsList(TutorSearchCondition tutorSearchCondition, Pageable pageable) {
        Page<TutorAccount> tutorAccountPage = tutorsRepository.searchTutors(tutorSearchCondition, pageable);
        if (tutorAccountPage.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<TutorCardDto> tutorCardDtoList = mapTutorCardDetails(tutorAccountPage.getContent());

        return new PageImpl<>(tutorCardDtoList, pageable, tutorAccountPage.getTotalElements());
    }
}
