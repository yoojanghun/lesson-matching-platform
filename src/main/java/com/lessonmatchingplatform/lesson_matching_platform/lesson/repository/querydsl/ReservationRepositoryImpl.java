package com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import static com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.QReservation.reservation;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsOverlappingReservation(Long tutorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        Integer fetchOne = queryFactory
                .selectOne()
                .from(reservation)
                .where(
                        reservation.tutorAccount.tutorId.eq(tutorId),
                        reservation.lessonDate.eq(date),
                        reservation.startTime.lt(endTime),
                        reservation.endTime.gt(startTime)
                )
                .fetchFirst();

        return fetchOne != null;
    }
}
