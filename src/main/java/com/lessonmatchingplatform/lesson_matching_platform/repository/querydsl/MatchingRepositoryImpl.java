package com.lessonmatchingplatform.lesson_matching_platform.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.Matching;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.lessonmatchingplatform.lesson_matching_platform.domain.account.QTutorAccount.tutorAccount;
import static com.lessonmatchingplatform.lesson_matching_platform.domain.account.QUserAccount.userAccount;
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
}
