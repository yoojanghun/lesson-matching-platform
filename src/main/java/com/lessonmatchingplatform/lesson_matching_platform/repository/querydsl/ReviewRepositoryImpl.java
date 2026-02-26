package com.lessonmatchingplatform.lesson_matching_platform.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.dto.response.ReviewResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.QLessonReview.lessonReview;
import static com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.QMatching.matching;

@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<ReviewResponse> findReviewsByTutorId(Long tutorId, Pageable pageable) {
        List<ReviewResponse> content = queryFactory
                .select(Projections.constructor(
                        ReviewResponse.class,
                        lessonReview.content,
                        lessonReview.rating,
                        new CaseBuilder()
                                .when(lessonReview.isAnonymous.isTrue()).then("익명")
                                .otherwise(lessonReview.createdBy)
                ))
                .from(lessonReview)
                .join(lessonReview.matching, matching)
                .where(
                        matching.tutorAccount.tutorId.eq(tutorId)
                )
                .orderBy(lessonReview.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)      // 6개가 가져와짐 -> hasNext 보여줌
                .fetch();

        boolean hasNext = false;
        if(content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());     // +1로 가져온 마지막 항목 제거
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }
}
