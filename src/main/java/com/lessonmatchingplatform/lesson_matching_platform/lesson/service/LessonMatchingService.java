package com.lessonmatchingplatform.lesson_matching_platform.lesson.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.Schedule;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.ScheduleException;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.StudentAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.ScheduleExceptionRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.ScheduleRepository;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.LessonMatchingRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.LessonStatusRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.ScheduleExceptionRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.WeeklyScheduleRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.MyMatchingResponseAsStudent;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.MyMatchingResponseAsTutor;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.MatchingRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.StudentRepository;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.MatchingStatus;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.AccessDeniedException;
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

    // Student가 레슨 등록
    public Long lessonMatching(BoardPrincipal boardPrincipal, Long tutorId, LessonMatchingRequest request) {
        if(matchingRepository.existsActiveMatching(boardPrincipal.id(), tutorId)) {
            throw new IllegalStateException("이미 진행중이거나 승인된 매칭 요청이 있습니다");
        }

        StudentAccount studentAccount = studentRepository.getReferenceById(boardPrincipal.id());
        TutorAccount tutorAccount = tutorsRepository.getReferenceById(tutorId);                     // pk만 필요하므로 proxy 생성

        Matching lessonMatching = Matching.of(studentAccount, tutorAccount, request.requestMsg(), MatchingStatus.PENDING);
        Matching savedMatching = matchingRepository.save(lessonMatching);

        return savedMatching.getMatchingId();
    }

    // Tutor가 레슨 승인 / 거절 / 취소
    public Long postMyMatching(BoardPrincipal boardPrincipal, Long matchingId, LessonStatusRequest request) throws AccessDeniedException {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(EntityNotFoundException::new);

        if(!boardPrincipal.id().equals(matching.getTutorAccount().getTutorId())) {
            throw new AccessDeniedException("본인에게 온 요청만 처리할 수 있습니다");
        }

        if(!matching.getStatus().equals(request.status())) {
            matching.setStatus(request.status());
        }

        return matching.getMatchingId();
    }

    // Tutor가 자신이 받은 레슨 리스트 확인
    @Transactional(readOnly = true)
    public List<MyMatchingResponseAsTutor> myMatchingsAsTutor(BoardPrincipal boardPrincipal) {
        return matchingRepository.findAllByTutorId(boardPrincipal.id());
    }

    // Student가 자신이 보낸 레슨 리스트 확인
    @Transactional(readOnly = true)
    public List<MyMatchingResponseAsStudent> myMatchingsAsStudent(BoardPrincipal boardPrincipal) {
        List<Matching> myMatchings = matchingRepository.findAllByStudentId(boardPrincipal.id());

        return myMatchings.stream().map(MyMatchingResponseAsStudent::from).toList();
    }

    // TUTOR는 본인이 레슨 가능한 시간을 시간표에서 표시해 둠
    public void myScheduleAsTutor(BoardPrincipal boardPrincipal, List<WeeklyScheduleRequest> request) {
        Long tutorId = boardPrincipal.id();
        validateOverlappingSchedule(request);

        scheduleRepository.deleteByTutorId(tutorId);
        List<Schedule> schedule = request.stream()
                .map(scheduleRequest -> Schedule.of(scheduleRequest.dayOfWeek(), scheduleRequest.startTime(), scheduleRequest.endTime()))
                .toList();

        scheduleRepository.saveAll(schedule);
    }

    // TUTOR가 특정 날에 레슨 불가 시간을 신청할 수 있도록 함.
    public void registerScheduleExceptions(BoardPrincipal boardPrincipal, List<@Valid ScheduleExceptionRequest> request) {
        Long tutorId = boardPrincipal.id();
        validateExceptionRequests(request);

        List<ScheduleException> exceptionToSave = new ArrayList<>();        // 가변 객체

        for (ScheduleExceptionRequest exceptionRequest : request) {

            // TODO: 해당 날짜, 해당 시간에 겹쳐 있는 '학생 예약'들이 있다면 취소해야 함

            ScheduleException scheduleException = ScheduleException.of(
                    exceptionRequest.exceptionDate(),
                    exceptionRequest.startTime(),
                    exceptionRequest.endTime(),
                    exceptionRequest.exceptionType()
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
}
