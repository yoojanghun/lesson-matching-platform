package com.lessonmatchingplatform.lesson_matching_platform.tutor.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.Location;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.LocationTutor;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.LocationRepository;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Category;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.CategoryTutor;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Subject;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.SubjectTutor;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.CategoryRepository;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.SubjectRepository;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.ReviewResponse;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.ReviewRepository;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.request.TutorProfileUpdateRequest;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.request.TutorSearchCondition;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response.TutorProfileResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response.TutorWithReviewsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.response.TutorsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class TutorsService {

    private final TutorsRepository tutorsRepository;
    private final ReviewRepository reviewRepository;
    private final CategoryRepository categoryRepository;
    private final SubjectRepository subjectRepository;
    private final LocationRepository locationRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // 공개 강사 상세 프로필 조회 (리뷰 제외) - Redis 캐싱 적용
    @Cacheable(value = "tutorDetail", key = "#tutorId")
    @Transactional(readOnly = true)
    public TutorProfileResponse getTutorProfile(Long tutorId) {
        TutorAccount tutorAccount = tutorsRepository.findProfileById(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 강사를 찾을 수 없습니다. id=" + tutorId));

        return TutorProfileResponse.from(tutorAccount);
    }

    @Transactional(readOnly = true)
    public Page<TutorsResponse> getTutorsList(TutorSearchCondition tutorSearchCondition, Pageable pageable) {
        return tutorsRepository.searchTutors(tutorSearchCondition, pageable)
                .map(TutorsResponse::from);
    }

    @Transactional(readOnly = true)
    public TutorWithReviewsResponse getTutorAndReviews(Long tutorId, Pageable pageable) {
        TutorAccount tutorAccount = tutorsRepository.searchTutor(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 강사를 찾을 수 없습니다. id=" + tutorId));

        Slice<ReviewResponse> reviewResponseSlice = reviewRepository.findReviewsByTutorId(tutorId, pageable);

        return TutorWithReviewsResponse.from(tutorAccount, reviewResponseSlice);
    }

    @Transactional(readOnly = true)
    public TutorProfileResponse getMyProfile(Long tutorId) {
        TutorAccount tutorAccount = tutorsRepository.findProfileById(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 강사를 찾을 수 없습니다. id=" + tutorId));

        return TutorProfileResponse.from(tutorAccount);
    }

    @CacheEvict(value = "tutorDetail", key = "#tutorId")
    public void updateMyProfile(Long tutorId, TutorProfileUpdateRequest request) {
        TutorAccount tutorAccount = tutorsRepository.findById(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 강사를 찾을 수 없습니다. id=" + tutorId));

        tutorAccount.updateProfile(
                request.title(),
                request.content(),
                request.introduction(),
                request.career()
        );

        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
            List<Category> categoryList = categoryRepository.findAllById(request.categoryIds());
            tutorAccount.getCategoryTutorSet().clear();
            categoryList.forEach(category -> tutorAccount.addCategoryTutor(CategoryTutor.of(tutorAccount, category)));
        }

        if (request.subjectIds() != null && !request.subjectIds().isEmpty()) {
            List<Subject> subjectList = subjectRepository.findAllById(request.subjectIds());
            tutorAccount.getSubjectTutorSet().clear();
            subjectList.forEach(subject -> tutorAccount.addSubjectTutor(SubjectTutor.of(tutorAccount, subject)));
        }

        if (request.locationIds() != null && !request.locationIds().isEmpty()) {
            List<Location> locationList = locationRepository.findAllById(request.locationIds());
            tutorAccount.getLocationTutorSet().clear();
            locationList.forEach(location -> tutorAccount.addLocationTutor(LocationTutor.of(tutorAccount, location)));
        }

    }
}
