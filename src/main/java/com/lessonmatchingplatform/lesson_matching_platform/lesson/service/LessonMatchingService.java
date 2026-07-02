package com.lessonmatchingplatform.lesson_matching_platform.lesson.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.StudentAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.LessonMatchingRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.request.LessonStatusRequest;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.MyMatchingResponseAsStudent;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.MyMatchingResponseAsTutor;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.repository.MatchingRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.StudentRepository;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.MatchingStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class LessonMatchingService {

    private final StudentRepository studentRepository;
    private final TutorsRepository tutorsRepository;
    private final MatchingRepository matchingRepository;

    // Student가 레슨 등록
    public MyMatchingResponseAsStudent lessonMatching(BoardPrincipal boardPrincipal, Long tutorId, LessonMatchingRequest request) {
        if(matchingRepository.existsActiveMatching(boardPrincipal.id(), tutorId)) {
            throw new IllegalStateException("이미 진행중이거나 승인된 매칭 요청이 있습니다");
        }

        StudentAccount studentAccount = studentRepository.getReferenceById(boardPrincipal.id());
        TutorAccount tutorAccount = tutorsRepository.getReferenceById(tutorId);                     // pk만 필요하므로 proxy 생성

        Matching lessonMatching = Matching.of(studentAccount, tutorAccount, request.requestMsg(), MatchingStatus.PENDING);
        Matching savedMatching = matchingRepository.save(lessonMatching);

        Matching matchingDetails = matchingRepository.findByIdWithDetails(savedMatching.getMatchingId())
                .orElseThrow(EntityNotFoundException::new);

        return MyMatchingResponseAsStudent.from(matchingDetails);
    }

    // Tutor가 레슨 승인 / 거절 / 취소
    public MyMatchingResponseAsTutor postMyMatching(BoardPrincipal boardPrincipal, Long matchingId, LessonStatusRequest request) throws AccessDeniedException {
        Matching matching = matchingRepository.findByIdWithDetails(matchingId)
                .orElseThrow(EntityNotFoundException::new);

        if(!boardPrincipal.id().equals(matching.getTutorAccount().getTutorId())) {
            throw new AccessDeniedException("본인에게 온 요청만 처리할 수 있습니다");
        }

        if(!matching.getStatus().equals(request.status())) {
            matching.setStatus(request.status());
        }

        return MyMatchingResponseAsTutor.from(matching);
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
}
