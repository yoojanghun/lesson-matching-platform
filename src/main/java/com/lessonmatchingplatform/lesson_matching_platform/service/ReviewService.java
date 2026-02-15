package com.lessonmatchingplatform.lesson_matching_platform.service;

import com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.LessonReview;
import com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.dto.request.ReviewRequest;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.ReviewResponse;
import com.lessonmatchingplatform.lesson_matching_platform.dto.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.repository.MatchingRepository;
import com.lessonmatchingplatform.lesson_matching_platform.repository.ReviewRepository;
import com.lessonmatchingplatform.lesson_matching_platform.type.MatchingStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MatchingRepository matchingRepository;

    public ReviewResponse postReview(BoardPrincipal boardPrincipal, ReviewRequest request, Long tutorId) {
        if(matchingRepository.hasAlreadyReviewedTutor(tutorId, boardPrincipal.id())) {
            throw new IllegalStateException("이미 리뷰를 작성한 수업입니다.");
        }

        Matching matching = matchingRepository.findByStudentAccount_StudentIdAndStatus(
                        boardPrincipal.id(),
                        MatchingStatus.ACCEPTED
                ).orElseThrow(() -> new EntityNotFoundException("리뷰를 작성할 수 있는 승인된 매칭이 없습니다."));

        LessonReview lessonReview = LessonReview.of(matching, request.content(), request.rating(), request.isAnonymous());

        return ReviewResponse.from(reviewRepository.save(lessonReview));
    }
}
