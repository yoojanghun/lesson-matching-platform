package com.lessonmatchingplatform.lesson_matching_platform.service;

import com.lessonmatchingplatform.lesson_matching_platform.dto.request.TutorSearchCondition;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.TutorsResponse;
import com.lessonmatchingplatform.lesson_matching_platform.repository.TutorsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class TutorsService {

    private final TutorsRepository tutorsRepository;

    @Transactional(readOnly = true)
    public Page<TutorsResponse> getTutorsList(TutorSearchCondition tutorSearchCondition, Pageable pageable) {
        return tutorsRepository.searchTutors(tutorSearchCondition, pageable)
                .map(TutorsResponse::from);
    }
}
