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
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.ReviewResponse;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.ReviewRepository;
import com.lessonmatchingplatform.lesson_matching_platform.main.dto.TutorCardDto;
import com.lessonmatchingplatform.lesson_matching_platform.main.dto.TutorLessonPriceRangeDto;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.request.TutorSearchCondition;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response.TutorWithReviewsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;
import org.springframework.cache.annotation.Cacheable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // 공개 강사 상세 프로필 조회 (리뷰 제외) - Redis 캐싱 적용
    @Cacheable(value = "tutorDetail", key = "#tutorId", unless = "#result == null")
    @Transactional(readOnly = true)
    public TutorProfileResponse getTutorProfile(Long tutorId) {
        TutorAccount tutorAccount = tutorsRepository.findProfileById(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 강사를 찾을 수 없습니다. id=" + tutorId));

        return categoryTutorRepository.findProfileResponseById(tutorId);
    }

    // 선생님 조회 필터로 선생님 리스트 조회
    @Transactional(readOnly = true)
    public Page<TutorCardDto> getTutorsList(TutorSearchCondition tutorSearchCondition, Pageable pageable) {
        Page<TutorAccount> tutorAccountPage = tutorsRepository.searchTutors(tutorSearchCondition, pageable);
        if (tutorAccountPage.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<TutorCardDto> tutorCardDtoList = mapTutorCardDetails(tutorAccountPage.getContent());

        return new PageImpl<>(tutorCardDtoList, pageable, tutorAccountPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Slice<ReviewResponse> findReviewsByTutorId(Long tutorId, Pageable pageable) {
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
}
