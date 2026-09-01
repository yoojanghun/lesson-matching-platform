package com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.LocationDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.ProfileStatus;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.request.TutorSearchCondition;
import com.lessonmatchingplatform.lesson_matching_platform.category.type.CategoryType;
import com.lessonmatchingplatform.lesson_matching_platform.category.type.SubjectType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QLocation.location;
import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QLocationTutor.locationTutor;
import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QTutorAccount.tutorAccount;
import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QUserAccount.userAccount;
import static com.lessonmatchingplatform.lesson_matching_platform.category.domain.QCategory.category;
import static com.lessonmatchingplatform.lesson_matching_platform.category.domain.QCategoryTutor.categoryTutor;
import static com.lessonmatchingplatform.lesson_matching_platform.category.domain.QSubject.subject;
import static com.lessonmatchingplatform.lesson_matching_platform.category.domain.QSubjectTutor.subjectTutor;
import static com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.QMatching.matching;

@RequiredArgsConstructor
public class TutorsRepositoryImpl implements TutorsRepositoryCustom {

        private final JPAQueryFactory queryFactory;

        @Override
        public Page<TutorAccount> searchTutors(TutorSearchCondition condition, Pageable pageable) {

                // 실제 데이터 조회
                List<TutorAccount> content = queryFactory
                                .selectFrom(tutorAccount).distinct()
                                .leftJoin(tutorAccount.userAccount, userAccount).fetchJoin()
                                .leftJoin(tutorAccount.categoryTutorSet, categoryTutor) // 여기서 leftJoin은 필터링 용(데이터 가져오기 X)
                                .leftJoin(categoryTutor.category, category) // proxy 객체가 채워짐. 나중에 필요할 때 query 발생
                                .leftJoin(tutorAccount.subjectTutorSet, subjectTutor)
                                .leftJoin(subjectTutor.subject, subject)
                                .where(
                                                categoryEq(condition.category()),
                                                subjectEq(condition.subject()))
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .orderBy(tutorAccount.createdAt.desc()) // 최신순 정렬, 별점순과 같은 필터 추가 시 OrderSpecifier를 사용해
                                                                        // 메서드로 분리
                                .fetch();

                // 페이징 용 카운트 쿼리(사용자가 보는 리스트가 전체 몇 페이지까지 있는지)
                JPAQuery<Long> countQuery = queryFactory
                                .select(tutorAccount.countDistinct())
                                .from(tutorAccount)
                                .leftJoin(tutorAccount.categoryTutorSet, categoryTutor)
                                .leftJoin(categoryTutor.category, category)
                                .leftJoin(tutorAccount.subjectTutorSet, subjectTutor)
                                .leftJoin(subjectTutor.subject, subject)
                                .where(
                                                categoryEq(condition.category()),
                                                subjectEq(condition.subject()));

                return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
        }

        @Override
        public Optional<TutorAccount> searchTutor(Long tutorId) {
                TutorAccount content = queryFactory
                                .selectFrom(tutorAccount)
                                .leftJoin(tutorAccount.userAccount, userAccount).fetchJoin()
                                .where(
                                                tutorAccount.tutorId.eq(tutorId))
                                .fetchOne();

                return Optional.ofNullable(content);
        }

        @Override
        public List<LocationDto> findLocationDtosByTutorId(Long tutorId) {
                return queryFactory
                                .select(
                                                Projections.constructor(LocationDto.class,
                                                                location.locationId,
                                                                location.name))
                                .from(locationTutor)
                                .join(locationTutor.location, location)
                                .where(locationTutor.tutorAccount.tutorId.eq(tutorId))
                                .fetch();
        }

    @Override
    public List<TutorAccount> findTop8ByCategoryIdOrderByMatchingCountDesc(Long categoryId) {
        LocalDateTime fifteenDaysAgo = LocalDateTime.now().minusDays(15);
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        NumberExpression<Long> matchingCount = matching.count();
        NumberExpression<BigDecimal> averageRating = tutorAccount.averageRating;
        NumberExpression<Integer> reviewCount = tutorAccount.reviewCount;

        // 매칭 수 스케일링 (0~20개를 0 ~ 5점으로 변환, 20개 넘어가면 5.0점 고정)
        NumberExpression<Double> scaledMatchingScore = new CaseBuilder()
                .when(matchingCount.goe(20L)).then(5.0)
                .otherwise((matchingCount.doubleValue().divide(20.0)).multiply(5.0));

        // 평점 (이미 0 ~ 5점 범위이므로 그대로 사용)
        NumberExpression<Double> scaledRatingScore = averageRating.castToNum(Double.class);         // BigDecimal -> Double

        // 리뷰 수 스케일링 (0~10개를 0~5점으로 변환, 10개 넘어가면 5.0점 고정
        NumberExpression<Double> scaledReviewScore = new CaseBuilder()
                .when(reviewCount.goe(10)).then(5.0)
                .otherwise((reviewCount.doubleValue().divide(10.0)).multiply(5.0));

        // 가장 최신성 스케일링
        NumberExpression<Double> scaledRecency = new CaseBuilder()
                .when(tutorAccount.createdAt.goe(thirtyDaysAgo)).then(5.0)
                .otherwise(0.0);

        NumberExpression<Double> totalScore = scaledMatchingScore.multiply(0.4)
                .add(scaledRatingScore.multiply(0.3))
                .add(scaledReviewScore.multiply(0.2))
                .add(scaledRecency.multiply(0.1));

        return queryFactory
                .selectFrom(tutorAccount)
                .join(tutorAccount.categoryTutorSet, categoryTutor)
                .join(tutorAccount.userAccount, userAccount).fetchJoin()
                .leftJoin(tutorAccount.matchingSet, matching)               // matching으로 한 tutorId에 여러 matching 생김
                .on(matching.createdAt.goe(fifteenDaysAgo))
                .where(
                        eqCategoryId(categoryId),
                        tutorAccount.profileStatus.eq(ProfileStatus.COMPLETED)
                )
                .groupBy(tutorAccount.tutorId, userAccount.userId)          // 집계함수 count 사용으로 인해 groupBy 필요
                .orderBy(
                        totalScore.desc(),
                        tutorAccount.createdAt.desc()
                )
                .limit(8)
                .fetch();
    }

    @Override
    public List<TutorAccount> findTop8RookieTutorsByCategoryId(Long categoryId) {

        return queryFactory
                .selectFrom(tutorAccount)
                .distinct()
                .join(tutorAccount.categoryTutorSet, categoryTutor)
                .join(tutorAccount.userAccount, userAccount).fetchJoin()
                .where(
                        eqCategoryId(categoryId),
                        tutorAccount.profileStatus.eq(ProfileStatus.COMPLETED)
                )
                .orderBy(tutorAccount.createdAt.desc())
                .limit(8)
                .fetch();
    }

        // BooleanExpression: 참 또는 거짓을 판단하는 SQL의 조건절을 자바 객체로 만든 것
        private BooleanExpression categoryEq(CategoryType categoryType) {
            return categoryType != null ? category.name.eq(categoryType) : null;
        }

        private BooleanExpression subjectEq(SubjectType subjectType) {
                return subjectType != null ? subject.name.eq(subjectType) : null;
        }

        private BooleanExpression eqCategoryId(Long categoryId) {
            return (categoryId != null && categoryId > 0)
                    ? categoryTutor.category.categoryId.eq(categoryId)
                    : null;
        }


}
