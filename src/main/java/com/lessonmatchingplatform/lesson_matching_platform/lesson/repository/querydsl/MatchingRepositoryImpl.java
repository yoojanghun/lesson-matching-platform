package com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.querydsl;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.MyMatchingResponseAsTutor;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.type.MatchingStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QStudentAccount.studentAccount;
import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QTutorAccount.tutorAccount;
import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QUserAccount.userAccount;
import static com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.QLessonReview.lessonReview;
import static com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.QMatching.matching;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import com.querydsl.jpa.impl.JPAQuery;

@RequiredArgsConstructor
public class MatchingRepositoryImpl implements MatchingRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<MyMatchingResponseAsTutor> findAllByTutorId(Long tutorId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        MyMatchingResponseAsTutor.class,
                        matching.matchingId,
                        matching.requestMsg,
                        matching.status,
                        userAccount.name,
                        userAccount.gender,
                        userAccount.birthDate,
                        userAccount.phoneNumber,
                        userAccount.email,
                        matching.createdAt
                ))
                .from(matching).distinct()
                .leftJoin(matching.studentAccount, studentAccount)
                .leftJoin(studentAccount.userAccount, userAccount)
                .where(
                        matching.tutorAccount.tutorId.eq(tutorId)
                )
                .orderBy(matching.createdAt.desc())         // 최신순 정렬
                .fetch();
    }

    @Override
    public Page<MyMatchingResponseAsTutor> findMatchingsByTutorId(Long tutorId, Pageable pageable) {
        List<MyMatchingResponseAsTutor> content = jpaQueryFactory
                .select(Projections.constructor(
                        MyMatchingResponseAsTutor.class,
                        matching.matchingId,
                        matching.requestMsg,
                        matching.status,
                        userAccount.name,
                        userAccount.gender,
                        userAccount.birthDate,
                        userAccount.phoneNumber,
                        userAccount.email,
                        matching.createdAt
                ))
                .from(matching)
                .leftJoin(matching.studentAccount, studentAccount)
                .leftJoin(studentAccount.userAccount, userAccount)
                .where(
                        matching.tutorAccount.tutorId.eq(tutorId)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(matching.createdAt.desc())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(matching.count())
                .from(matching)
                .where(
                        matching.tutorAccount.tutorId.eq(tutorId)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<Matching> findAllByStudentId(Long studentId) {
        return jpaQueryFactory
                .selectFrom(matching).distinct()
                .leftJoin(matching.tutorAccount, tutorAccount).fetchJoin()
                .leftJoin(tutorAccount.userAccount, userAccount).fetchJoin()
                .where(
                        matching.studentAccount.studentId.eq(studentId)
                )
                .orderBy(matching.createdAt.desc())         // 최신순 정렬
                .fetch();
    }

    @Override
    public Page<Matching> findMatchingsByStudentId(Long studentId, Pageable pageable) {
        List<Matching> content = jpaQueryFactory
                .selectFrom(matching)
                .leftJoin(matching.tutorAccount, tutorAccount).fetchJoin()
                .leftJoin(tutorAccount.userAccount, userAccount).fetchJoin()
                .where(
                        matching.studentAccount.studentId.eq(studentId)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(matching.createdAt.desc())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(matching.count())
                .from(matching)
                .where(
                        matching.studentAccount.studentId.eq(studentId)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Boolean existsActiveMatching(Long studentId, Long tutorId) {
        Integer content = jpaQueryFactory
                .selectOne()                // 조건에 맞는 게 있다면 그냥 숫자 1을 던짐
                .from(matching)
                .where(
                        matching.tutorAccount.tutorId.eq(tutorId),
                        matching.studentAccount.studentId.eq(studentId),
                        matching.status.ne(MatchingStatus.REJECTED),
                        matching.status.ne(MatchingStatus.CANCELLED),
                        matching.status.ne(MatchingStatus.ACCEPTED)
                ).fetchFirst();             // 조건에 맞는 record를 모두 조회하는 것이 아니라, 처음 1개만 조회 (limit 1)

        return content != null;
    }

    @Override
    public Boolean hasAlreadyReviewedTutor(Long tutorId, Long studentId) {
        Integer content = jpaQueryFactory
                .selectOne()
                .from(lessonReview)
                .leftJoin(lessonReview.matching, matching)
                .where(
                        matching.tutorAccount.tutorId.eq(tutorId),
                        matching.studentAccount.studentId.eq(studentId)
                )
                .fetchFirst();

        return content != null;
    }
}
