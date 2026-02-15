package com.lessonmatchingplatform.lesson_matching_platform.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.type.MatchingStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.lessonmatchingplatform.lesson_matching_platform.domain.account.QStudentAccount.studentAccount;
import static com.lessonmatchingplatform.lesson_matching_platform.domain.account.QTutorAccount.tutorAccount;
import static com.lessonmatchingplatform.lesson_matching_platform.domain.account.QUserAccount.userAccount;
import static com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.QLessonReview.lessonReview;
import static com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.QMatching.matching;

@RequiredArgsConstructor
public class MatchingRepositoryImpl implements MatchingRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Matching> findByIdWithDetails(Long matchingId) {
        Matching content = jpaQueryFactory
                .selectFrom(matching).distinct()
                .leftJoin(matching.tutorAccount, tutorAccount).fetchJoin()      // fetchJoin 없으면 proxy가 들어옴
                .leftJoin(tutorAccount.userAccount, userAccount).fetchJoin()    // 일반 join은 검색조건(where)으로 사용할 때 씀
                .where(
                        matching.matchingId.eq(matchingId)
                )
                .fetchOne();

        return Optional.ofNullable(content);
    }

    @Override
    public List<Matching> findAllByTutorId(Long tutorId) {
        return jpaQueryFactory
                .selectFrom(matching).distinct()
                .leftJoin(matching.studentAccount, studentAccount).fetchJoin()
                .leftJoin(studentAccount.userAccount, userAccount).fetchJoin()
                .where(
                        matching.tutorAccount.tutorId.eq(tutorId)
                )
                .orderBy(matching.createdAt.desc())         // 최신순 정렬
                .fetch();
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
