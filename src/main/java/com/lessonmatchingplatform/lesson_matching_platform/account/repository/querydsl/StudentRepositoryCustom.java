package com.lessonmatchingplatform.lesson_matching_platform.account.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.LocationDto;

import java.util.List;

public interface StudentRepositoryCustom {
    List<LocationDto> findLocationDtosByStudentId(Long studentId);
}
