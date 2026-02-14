package com.lessonmatchingplatform.lesson_matching_platform.service;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.StudentAccount;
import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.Matching;
import com.lessonmatchingplatform.lesson_matching_platform.dto.request.LessonMatchingRequest;
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

    public List<MyMatchingResponse> myMatchings(BoardPrincipal boardPrincipal) {
        if(!tutorsRepository.existsById(boardPrincipal.id())) {
            return Collections.emptyList();
        }

        List<Matching> myMatchings = matchingRepository.findAllByTutorId(boardPrincipal.id());

        return myMatchings.stream().map(MyMatchingResponse::from).toList();
    }
}
