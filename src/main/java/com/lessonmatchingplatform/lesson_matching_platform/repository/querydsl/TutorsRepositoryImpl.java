package com.lessonmatchingplatform.lesson_matching_platform.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.dto.request.TutorSearchCondition;
import com.lessonmatchingplatform.lesson_matching_platform.type.CategoryType;
import com.lessonmatchingplatform.lesson_matching_platform.type.SubjectType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.lessonmatchingplatform.lesson_matching_platform.domain.account.QTutorAccount.tutorAccount;
import static com.lessonmatchingplatform.lesson_matching_platform.domain.account.QUserAccount.userAccount;
import static com.lessonmatchingplatform.lesson_matching_platform.domain.category.QCategory.category;
import static com.lessonmatchingplatform.lesson_matching_platform.domain.category.QCategoryTutor.categoryTutor;
import static com.lessonmatchingplatform.lesson_matching_platform.domain.category.QSubject.subject;
import static com.lessonmatchingplatform.lesson_matching_platform.domain.category.QSubjectTutor.subjectTutor;

@Getter
@RequiredArgsConstructor
@Repository
public class TutorsRepositoryImpl implements TutorsRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<TutorAccount> searchTutors(TutorSearchCondition condition, Pageable pageable) {

        // 실제 데이터 조회
        List<TutorAccount> content = queryFactory
                .selectFrom(tutorAccount).distinct()
                .leftJoin(tutorAccount.userAccount, userAccount).fetchJoin()
                .leftJoin(tutorAccount.categoryTutorSet, categoryTutor)         // 여기서 leftJoin은 필터링 용(데이터 가져오기 X)
                .leftJoin(categoryTutor.category, category)
                .leftJoin(tutorAccount.subjectTutorSet, subjectTutor)
                .leftJoin(subjectTutor.subject, subject)
                .where(
                        categoryEq(condition.category()),
                        subjectEq(condition.subject())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(tutorAccount.createdAt.desc())     // 최신순 정렬, 별점순과 같은 필터 추가 시 OrderSpecifier를 사용해 메서드로 분리
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
                        subjectEq(condition.subject())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<TutorAccount> searchTutor(Long tutorId) {
        TutorAccount content = queryFactory
                .selectFrom(tutorAccount)
                .leftJoin(tutorAccount.userAccount, userAccount).fetchJoin()
                .where(
                        tutorAccount.tutorId.eq(tutorId)
                ).fetchOne();

        return Optional.ofNullable(content);
    }

    // BooleanExpression: 참 또는 거짓을 판단하는 SQL의 조건절을 자바 객체로 만든 것
    private BooleanExpression categoryEq(CategoryType categoryType) {
        return categoryType != null ? category.name.eq(categoryType.name()) : null;
    }

    private BooleanExpression subjectEq(SubjectType subjectType) {
        return subjectType != null ? subject.name.eq(subjectType.name()) : null;
    }

}
