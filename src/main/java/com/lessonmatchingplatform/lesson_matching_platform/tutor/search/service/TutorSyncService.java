package com.lessonmatchingplatform.lesson_matching_platform.tutor.search.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorLessonPrice;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.ProfileStatus;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.search.document.TutorDocument;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.search.repository.TutorSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorSyncService {

    private final TutorsRepository tutorsRepository;
    private final TutorSearchRepository tutorSearchRepository;

    @Transactional(readOnly = true)
    public int syncAllCompletedTutors() {
        List<TutorAccount> completedTutors = tutorsRepository.findAllByProfileStatus(ProfileStatus.COMPLETED);

        List<TutorDocument> documents = completedTutors.stream()
                .map(this::toDocument)
                .toList();

        if (!documents.isEmpty()) {
            tutorSearchRepository.saveAll(documents);
        }

        log.info("Successfully synced {} completed tutors to Elasticsearch.", documents.size());
        return documents.size();
    }

    public TutorDocument toDocument(TutorAccount tutorAccount) {
        List<String> categories = tutorAccount.getCategoryTutorSet().stream()
                .map(ct -> ct.getCategory().getName().name())
                .toList();

        List<String> subjects = tutorAccount.getSubjectTutorSet().stream()
                .map(st -> st.getSubject().getName().name())
                .toList();

        List<String> locations = tutorAccount.getLocationTutorSet().stream()
                .map(lt -> lt.getLocation().getName())
                .toList();

        List<String> goals = tutorAccount.getGoalTutorSet().stream()
                .map(gt -> gt.getLessonGoal().getLessonGoalType().name())
                .toList();

        List<Long> categoryIds = tutorAccount.getCategoryTutorSet().stream()
                .map(ct -> ct.getCategory().getCategoryId())
                .toList();
        List<Long> subjectIds = tutorAccount.getSubjectTutorSet().stream()
                .map(st -> st.getSubject().getSubjectId())
                .toList();
        List<Long> locationIds = tutorAccount.getLocationTutorSet().stream()
                .map(lt -> lt.getLocation().getLocationId())
                .toList();
        List<Long> goalIds = tutorAccount.getGoalTutorSet().stream()
                .map(gt -> gt.getLessonGoal().getGoalId())
                .toList();
        List<Long> styleIds = tutorAccount.getStyleTutorSet().stream()
                .map(st -> st.getTutorStyle().getStyleId())
                .toList();

        Integer minPrice = tutorAccount.getTutorLessonPriceSet().stream()
                .map(TutorLessonPrice::getPrice)
                .min(Integer::compareTo)
                .orElse(null);
        Integer maxPrice = tutorAccount.getTutorLessonPriceSet().stream()
                .map(TutorLessonPrice::getPrice)
                .max(Integer::compareTo)
                .orElse(null);
        String lessonTypeStr = tutorAccount.getLessonType() != null ? tutorAccount.getLessonType().name() : null;

        Double totalScore = calculateTotalScore(tutorAccount);

        return TutorDocument.builder()
                .id(tutorAccount.getTutorId())
                .name(tutorAccount.getUserAccount().getName())
                .title(tutorAccount.getTitle())
                .introduction(tutorAccount.getIntroduction())
                .categories(categories)
                .subjects(subjects)
                .locations(locations)
                .goals(goals)
                .categoryIds(categoryIds)
                .subjectIds(subjectIds)
                .locationIds(locationIds)
                .goalIds(goalIds)
                .styleIds(styleIds)
                .lessonType(lessonTypeStr)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .averageRating(tutorAccount.getAverageRating())
                .reviewCount(tutorAccount.getReviewCount())
                .matchingCount(tutorAccount.getMatchingCount())
                .createdAt(tutorAccount.getCreatedAt())
                .totalScore(totalScore)
                .build();
    }

    public Double calculateTotalScore(TutorAccount tutorAccount) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime fifteenDaysAgo = LocalDateTime.now().minusDays(15);

        // 1. 매칭 수 스케일링 (0~20개를 0~5점으로 변환, 20개 넘어가면 5.0점 고정)
        long matchingCount = tutorAccount.getMatchingSet().stream()
                .filter(m -> m.getCreatedAt().isAfter(fifteenDaysAgo))
                .count();
        double scaledMatchingScore = matchingCount >= 20 ? 5.0 : (matchingCount / 20.0) * 5.0;

        // 2. 평점 (이미 0~5점 범위)
        double scaledRatingScore = tutorAccount.getAverageRating() != null ? tutorAccount.getAverageRating().doubleValue() : 0.0;

        // 3. 리뷰 수 스케일링 (0~10개를 0~5점으로 변환, 10개 넘어가면 5.0점 고정)
        int reviewCount = tutorAccount.getReviewCount() != null ? tutorAccount.getReviewCount() : 0;
        double scaledReviewScore = reviewCount >= 10 ? 5.0 : (reviewCount / 10.0) * 5.0;

        // 4. 가장 최신성 스케일링 (30일 이내 가입 시 5.0점, 아니면 0.0점)
        double scaledRecency = tutorAccount.getCreatedAt().isAfter(thirtyDaysAgo) ? 5.0 : 0.0;

        return (scaledMatchingScore * 0.4) + (scaledRatingScore * 0.3) + (scaledReviewScore * 0.2) + (scaledRecency * 0.1);
    }
}
