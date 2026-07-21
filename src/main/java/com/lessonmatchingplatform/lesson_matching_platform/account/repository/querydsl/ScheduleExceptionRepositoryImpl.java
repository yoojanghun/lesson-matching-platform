package com.lessonmatchingplatform.lesson_matching_platform.account.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.ScheduleException;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QScheduleException.scheduleException;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class ScheduleExceptionRepositoryImpl implements ScheduleExceptionRepositoryCustom{

    private JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ScheduleException> findByTutorIdAndDateRange(Long tutorId, LocalDate startTime, LocalDate endTime) {
        return jpaQueryFactory
                .selectFrom(scheduleException)
                .where(
                        scheduleException.tutorAccount.tutorId.eq(tutorId),
                        scheduleException.exceptionDate.between(startTime, endTime)

                ).fetch();
    }
}
