package com.lessonmatchingplatform.lesson_matching_platform.lesson.domain;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.global.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Entity
public class Reservation extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id", nullable = false)
    private Matching matching;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private TutorAccount tutorAccount;

    @Column(length = 500)
    private String requestMsg;

    @Column(nullable = false)
    private LocalDate lessonDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private ReservationStatus reservationStatus;

    protected Reservation() {}

    private Reservation(Matching matching, TutorAccount tutorAccount, String requestMsg, LocalDate lessonDate, LocalTime startTime, LocalTime endTime, ReservationStatus reservationStatus) {
        this.matching = matching;
        this.tutorAccount = tutorAccount;
        this.requestMsg = requestMsg;
        this.lessonDate = lessonDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reservationStatus = reservationStatus;
    }

    public static Reservation of(Matching matching, TutorAccount tutorAccount, String requestMsg, LocalDate lessonDate, LocalTime startTime, LocalTime endTime, ReservationStatus reservationStatus) {
        return new Reservation(matching, tutorAccount, requestMsg, lessonDate, startTime, endTime, reservationStatus);
    }

    public void updateReservationStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        return this.reservationId != null && Objects.equals(this.reservationId, that.reservationId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(reservationId);
    }
}
