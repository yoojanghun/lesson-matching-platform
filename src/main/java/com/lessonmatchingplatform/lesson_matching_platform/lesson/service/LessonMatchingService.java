package com.lessonmatchingplatform.lesson_matching_platform.lesson.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.*;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.ScheduleExceptionRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.ScheduleRepository;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Reservation;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.ReservationStatus;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.*;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.*;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.MatchingRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.StudentRepository;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.ReservationRepository;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.MatchingStatus;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Service
public class LessonMatchingService {

    private final StudentRepository studentRepository;
    private final TutorsRepository tutorsRepository;
    private final MatchingRepository matchingRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleExceptionRepository scheduleExceptionRepository;
    private final ReservationRepository reservationRepository;

    // Student가 레슨 등록
    public Long lessonMatching(Long studentId, Long tutorId, LessonMatchingRequest request) {
        if(matchingRepository.existsActiveMatching(studentId, tutorId)) {
            throw new IllegalStateException("이미 진행중이거나 승인된 매칭 요청이 있습니다");
        }

        StudentAccount studentAccount = studentRepository.getReferenceById(studentId);
        TutorAccount tutorAccount = tutorsRepository.getReferenceById(tutorId);                     // pk만 필요하므로 proxy 생성

        Matching lessonMatching = Matching.of(studentAccount, tutorAccount, request.requestMsg(), MatchingStatus.PENDING);
        Matching savedMatching = matchingRepository.save(lessonMatching);

        return savedMatching.getMatchingId();
    }

    // Tutor가 레슨 승인 / 거절 / 취소
    public Long postMyMatching(Long tutorId, Long matchingId, LessonStatusRequest request) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new EntityNotFoundException("matchingId에 해당되는 matching이 없습니다."));

        if(!tutorId.equals(matching.getTutorAccount().getTutorId())) {
            throw new AccessDeniedException("본인에게 온 요청만 처리할 수 있습니다");
        }

        MatchingStatus requestStatus = request.status();
        if(!matching.getStatus().equals(requestStatus)) {
            matching.setStatus(requestStatus);
        }

        return matching.getMatchingId();
    }

    // Student가 Tutor가 레슨 가능한 일정을 확인하는 요청
    @Transactional(readOnly = true)
    public TutorScheduleResponse getTutorSchedules(Long tutorId, LocalDate startDate, LocalDate endDate) {
        List<WeeklyScheduleResponse> weeklySchedules = scheduleRepository.findAllByTutorAccount_TutorId(tutorId).stream()
                .map(WeeklyScheduleResponse::from).toList();

        List<ScheduleExceptionResponse> scheduleExceptions = scheduleExceptionRepository.findByTutorIdAndDateRange(tutorId, startDate, endDate).stream()
                .map(ScheduleExceptionResponse::from).toList();

        List<ReservedSlotResponse> reservedSlots = reservationRepository.findActiveReservationsByTutorIdAndDateRange(tutorId, startDate, endDate).stream()
                .map(ReservedSlotResponse::from).toList();

        return TutorScheduleResponse.of(tutorId, weeklySchedules, scheduleExceptions, reservedSlots);
    }

    // Student가 Tutor와 레슨 매칭이 완료된 후, 특정 시간에 레슨 요청
    public void lessonScheduleMatching(Long studentId, Long tutorId, Long matchingId, LessonScheduleRequest request) {
        Matching matching = matchingRepository.findByMatchingIdAndStudentAccount_StudentId(matchingId, studentId)
                .orElseThrow(() -> new EntityNotFoundException("matchingId, studentId 에 해당하는 matching이 없습니다."));

        if (matching.getStatus() != MatchingStatus.ACCEPTED) {
            throw new IllegalStateException("tutor와 student는 매칭된 레슨이 없습니다.");
        }

        DayOfWeek requestDayOfWeek = request.dayOfWeek();
        LocalDate requestDate = request.date();
        LocalTime requestStartTime = request.startTime();
        LocalTime requestEndTime = request.endTime();

        List<Schedule> tutorSchedule = scheduleRepository.findAllByTutorAccount_TutorIdAndDayOfWeek(tutorId, requestDayOfWeek);
        if (tutorSchedule.isEmpty()) {
            throw new IllegalStateException("해당 요일에는 튜터의 레슨 일정이 없습니다.");
        }

        boolean isWithinSchedule = tutorSchedule.stream()
                .anyMatch(schedule ->
                        !requestStartTime.isBefore(schedule.getStartTime()) && !requestEndTime.isAfter(schedule.getEndTime())
                );

        if (!isWithinSchedule) {
            throw new IllegalStateException("요청한 시간이 튜터의 레슨 가능 시간에 포함되지 않습니다.");
        }

        List<ScheduleException> tutorScheduleException = scheduleExceptionRepository.findAllByTutorAccount_TutorIdAndExceptionDate(tutorId, requestDate);

        boolean isOverlappingWithException = tutorScheduleException.stream()
                .anyMatch(exception ->
                        requestStartTime.isBefore(exception.getEndTime()) && requestEndTime.isAfter(exception.getStartTime())
                );

        if (isOverlappingWithException) {
            throw new IllegalStateException("요청한 시간이 튜터의 휴무/불가 시간과 겹칩니다.");
        }

        boolean isAlreadyReserved = reservationRepository.existsOverlappingReservation(tutorId, requestDate, requestStartTime, requestEndTime);
        if (isAlreadyReserved) {
            throw new IllegalStateException("해당 시간대는 이미 다른 학생의 레슨 예약이 차 있습니다.");
        }

        TutorAccount tutorAccount = tutorsRepository.getReferenceById(tutorId);
        Reservation reservation = Reservation.of(
                matching,
                tutorAccount,
                request.requestMsg(),
                requestDate,
                requestStartTime,
                requestEndTime,
                ReservationStatus.PENDING
        );

        reservationRepository.save(reservation);
    }

    // Tutor가 자신이 받은 레슨 리스트 확인
    @Transactional(readOnly = true)
    public List<MyMatchingResponseAsTutor> myMatchingsAsTutor(Long tutorId) {
        return matchingRepository.findAllByTutorId(tutorId);
    }

    // Student가 자신이 보낸 Matching 리스트 확인
    @Transactional(readOnly = true)
    public List<MyMatchingResponseAsStudent> myMatchingsAsStudent(Long studentId) {
        List<Matching> myMatchings = matchingRepository.findAllByStudentId(studentId);

        return myMatchings.stream().map(MyMatchingResponseAsStudent::from).toList();
    }

    // TUTOR는 본인이 레슨 가능한 시간을 시간표에서 표시해 둠
    public void myScheduleAsTutor(Long tutorId, List<WeeklyScheduleRequest> request) {
        TutorAccount tutorAccount = tutorsRepository.getReferenceById(tutorId);
        validateOverlappingSchedule(request);

        scheduleRepository.deleteByTutorAccount_TutorId(tutorId);
        List<Schedule> schedule = request.stream()
                .map(scheduleRequest -> Schedule.of(tutorAccount, scheduleRequest.dayOfWeek(), scheduleRequest.startTime(), scheduleRequest.endTime()))
                .toList();

        scheduleRepository.saveAll(schedule);
    }

    // TUTOR가 특정 날에 레슨 불가 시간을 신청할 수 있도록 함.
    public void registerScheduleExceptions(Long tutorId, List<@Valid ScheduleExceptionRequest> request) {
        TutorAccount tutorAccount = tutorsRepository.getReferenceById(tutorId);
        validateExceptionRequests(request);

        List<ScheduleException> exceptionToSave = new ArrayList<>();        // 가변 객체

        for (ScheduleExceptionRequest exceptionRequest : request) {
            LocalDate requestExceptionDate = exceptionRequest.exceptionDate();
            LocalTime requestStartTime = exceptionRequest.startTime();
            LocalTime requestEndTime = exceptionRequest.endTime();
            ExceptionType requestExceptionType = exceptionRequest.exceptionType();

            boolean isAlreadyReserved = reservationRepository.existsOverlappingReservation(tutorId, requestExceptionDate, requestStartTime, requestEndTime);
            if (isAlreadyReserved) {
                throw new IllegalStateException("해당 시간대는 이미 다른 학생의 레슨 예약이 차 있습니다.");
            }

            ScheduleException scheduleException = ScheduleException.of(
                    tutorAccount,
                    requestExceptionDate,
                    requestStartTime,
                    requestEndTime,
                    requestExceptionType
            );

            exceptionToSave.add(scheduleException);
        }

        scheduleExceptionRepository.saveAll(exceptionToSave);
    }

    private void validateOverlappingSchedule(List<WeeklyScheduleRequest> request) {
        Map<DayOfWeek, List<WeeklyScheduleRequest>> groupedByDay = request.stream()
                .collect(Collectors.groupingBy(
                        WeeklyScheduleRequest::dayOfWeek,
                        Collectors.toCollection(ArrayList::new)
                ));

        for (Map.Entry<DayOfWeek, List<WeeklyScheduleRequest>> entry : groupedByDay.entrySet()) {
            List<WeeklyScheduleRequest> dayRequests = entry.getValue();

            dayRequests.sort(Comparator.comparing(WeeklyScheduleRequest::startTime));
            for (int i = 0; i < dayRequests.size() - 1; i++) {
                WeeklyScheduleRequest request1 = dayRequests.get(i);
                WeeklyScheduleRequest request2 = dayRequests.get(i+1);

                if (isOverlapping(request1.endTime(), request2.startTime())) {
                    throw new IllegalStateException(
                            "겹치는 스케줄이 존재합니다. 요일: " + entry.getKey() +
                                    ", 시간: " + request1.startTime() + "~" + request1.endTime() +
                                    " 와 " + request2.startTime() + "~" + request2.endTime()
                    );
                }
            }
        }
    }

    private void validateExceptionRequests(List<ScheduleExceptionRequest> requests) {
        Map<LocalDate, List<ScheduleExceptionRequest>> groupedByDate = requests.stream()
                .collect(Collectors.groupingBy(
                        ScheduleExceptionRequest::exceptionDate,
                        Collectors.toCollection(ArrayList::new)
                ));

        for (Map.Entry<LocalDate, List<ScheduleExceptionRequest>> entry : groupedByDate.entrySet()) {
            List<ScheduleExceptionRequest> dateRequests = entry.getValue();
            dateRequests.sort(Comparator.comparing(ScheduleExceptionRequest::startTime));

            for (int i = 0; i < dateRequests.size() - 1; i++) {
                ScheduleExceptionRequest request1 = dateRequests.get(i);
                ScheduleExceptionRequest request2 = dateRequests.get(i+1);

                if (isOverlapping(request1.endTime(), request2.startTime())) {
                    throw new IllegalStateException(
                            entry.getKey() + " 날짜에 입력한 휴무 시간이 서로 중복됩니다."
                    );
                }
            }
        }
    }

    private boolean isOverlapping(LocalTime end1, LocalTime start2) {
        return start2.isBefore(end1);
    }

    // TUTOR는 자신에게 요청이 들어온 Reservation들을 Page 형태로 확인할 수 있도록 해야 함.
    public Page<ReservationResponse> getTutorReservations(Long tutorId, ReservationStatus status, Pageable pageable) {
        return reservationRepository.findTutorReservations(tutorId, status, pageable);
    }

    // 선생님이 학생에게 받은 레슨 요청에 대한 상태 변경
    public void updateLessonScheduleStatus(Long tutorId, Long reservationId, LessonScheduleStatusRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("해당 예약이 없습니다."));

        if (!reservation.getTutorAccount().getTutorId().equals(tutorId)) {
            throw new AccessDeniedException("해당 요청을 처리할 권한이 없는 강사입니다.");
        }

        if (!reservation.getReservationStatus().equals(ReservationStatus.PENDING)) {
            throw new IllegalStateException("이미 처리되었거나 변경이 불가능한 상태의 예약입니다.");
        }

        ReservationStatus newStatus = request.reservationStatus();

        reservation.updateReservationStatus(newStatus);
    }

    // 학생이 레슨 신청을 하지 않더라도, 강사는 특정 레슨을 COMPLETED 할 수 있어야 함.
    public void createDirectReservation(Long tutorId, TutorDirectReservationRequest request) {
        Matching matching = matchingRepository.findById(request.matchingId())
                .orElseThrow(() -> new EntityNotFoundException("해당되는 matching이 없습니다."));

        if (!matching.getTutorAccount().getTutorId().equals(tutorId)) {
            throw new IllegalStateException("해당 요청을 처리할 권한이 없는 강사입니다.");
        }

        LocalDate requestDate = request.date();
        LocalTime requestStartTime = request.startTime();
        LocalTime requestEndTime = request.endTime();
        ReservationStatus requestStatus = request.status();

        boolean isOverlappingReservation = reservationRepository.existsOverlappingReservation(tutorId, requestDate, requestStartTime, requestEndTime);
        if (isOverlappingReservation) {
            throw new IllegalStateException("해당 시간대는 이미 다른 학생의 레슨 예약이 차 있습니다.");
        }

        TutorAccount tutorAccount = tutorsRepository.getReferenceById(tutorId);
        Reservation reservation = Reservation.of(
                matching,
                tutorAccount,
                request.requestMsg(),
                requestDate,
                requestStartTime,
                requestEndTime,
                requestStatus
        );

        reservationRepository.save(reservation);
    }


}
