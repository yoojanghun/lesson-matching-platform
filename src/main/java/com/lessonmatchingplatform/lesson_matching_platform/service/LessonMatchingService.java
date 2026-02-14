package com.lessonmatchingplatform.lesson_matching_platform.service;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.StudentAccount;
import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.dto.request.LessonMatchingRequest;
import com.lessonmatchingplatform.lesson_matching_platform.dto.request.LessonStatusRequest;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.LessonMatchingResponse;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.MyMatchingResponse;
import com.lessonmatchingplatform.lesson_matching_platform.dto.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.repository.MatchingRepository;
import com.lessonmatchingplatform.lesson_matching_platform.repository.StudentRepository;
import com.lessonmatchingplatform.lesson_matching_platform.repository.TutorsRepository;
import com.lessonmatchingplatform.lesson_matching_platform.type.MatchingStatus;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class LessonMatchingService {

    private final StudentRepository studentRepository;
    private final TutorsRepository tutorsRepository;
    private final MatchingRepository matchingRepository;

    public LessonMatchingResponse lessonMatching(BoardPrincipal boardPrincipal, Long tutorId, LessonMatchingRequest request) {
        StudentAccount studentAccount = studentRepository.getReferenceById(boardPrincipal.id());
        TutorAccount tutorAccount = tutorsRepository.getReferenceById(tutorId);                     // pk만 필요하므로 proxy 생성

        Matching lessonMatching = Matching.of(studentAccount, tutorAccount, request.requestMsg(), MatchingStatus.PENDING);
        Matching savedMatching = matchingRepository.save(lessonMatching);

        Matching matchingDetails = matchingRepository.findByIdWithDetails(savedMatching.getMatchingId())
                .orElseThrow(EntityNotFoundException::new);

        return LessonMatchingResponse.from(matchingDetails);
    }

    public MyMatchingResponse postMyMatching(BoardPrincipal boardPrincipal, Long matchingId, LessonStatusRequest request) throws AccessDeniedException {
        Matching matching = matchingRepository.findByIdWithDetails(matchingId)
                .orElseThrow(EntityNotFoundException::new);

        if(!boardPrincipal.id().equals(matching.getTutorAccount().getTutorId())) {
            throw new AccessDeniedException("본인에게 온 요청만 처리할 수 있습니다");
        }

        if(!matching.getStatus().equals(request.status())) {
            matching.setStatus(request.status());
        }

        return MyMatchingResponse.from(matching);
    }

    public List<MyMatchingResponse> myMatchingsAsTutor(BoardPrincipal boardPrincipal) {
        List<Matching> myMatchings = matchingRepository.findAllByTutorId(boardPrincipal.id());

        return myMatchings.stream().map(MyMatchingResponse::from).toList();
    }

    public List<MyMatchingResponse> myMatchingsAsStudent(BoardPrincipal boardPrincipal) {
        List<Matching> myMatchings = matchingRepository.findAllByStudentId(boardPrincipal.id());

        return myMatchings.stream().map(MyMatchingResponse::from).toList();
    }
}
