package com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Reservation;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.ReservationStatus;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.ReservationResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QStudentAccount.studentAccount;
import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QUserAccount.userAccount;
import static com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.QMatching.matching;
import static com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.QReservation.reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
                        reservation.endTime.gt(startTime),
                        reservation.reservationStatus.in(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
                )
                .fetchFirst();

        return fetchOne != null;
    }

    @Override
    public List<Reservation> findActiveReservationsByTutorIdAndDateRange(Long tutorId, LocalDate startDate, LocalDate endDate) {
        return queryFactory
                .selectFrom(reservation)
                .where(
                        reservation.tutorAccount.tutorId.eq(tutorId),
                        reservation.lessonDate.between(startDate, endDate),
                        reservation.reservationStatus.in(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.COMPLETED)
                ).fetch();
    }

    @Override
    public Page<ReservationResponse> findTutorReservations(Long tutorId, ReservationStatus status, Pageable pageable) {
        List<ReservationResponse> reservations = queryFactory
                .select(Projections.constructor(
                        ReservationResponse.class,
                        reservation.reservationId,
                        studentAccount.studentId,
                        userAccount.name,
                        reservation.lessonDate,
                        reservation.startTime,
                        reservation.endTime,
                        reservation.reservationStatus,
                        reservation.createdAt
                        )
                )
                .from(reservation)
                .join(reservation.matching, matching)
                .join(matching.studentAccount, studentAccount)
                .join(studentAccount.userAccount, userAccount)
                .where(
                        reservation.tutorAccount.tutorId.eq(tutorId),
                        eqState(status)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(reservation.createdAt.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(reservation.count())
                .from(reservation)
                .where(
                        reservation.tutorAccount.tutorId.eq(tutorId),
                        eqState(status)
                );

        return PageableExecutionUtils.getPage(reservations, pageable, countQuery::fetchOne);
    }

    private BooleanExpression eqState(ReservationStatus status) {
        return status != null ? reservation.reservationStatus.eq(status) : null;
    }
}
