package com.lessonmatchingplatform.lesson_matching_platform.account.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.LocationDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QLocation.location;
import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QLocationStudent.locationStudent;

@RequiredArgsConstructor
public class StudentRepositoryImpl implements StudentRepositoryCustom{

    private JPAQueryFactory jpaQueryFactory;

    @Override
    public List<LocationDto> findLocationDtosByStudentId(Long studentId) {
        return jpaQueryFactory
                .select(
                        Projections.constructor(LocationDto.class,
                                        location.locationId,
                                        location.name
                ))
                .from(locationStudent)
                .join(locationStudent.location, location)
                .where(locationStudent.studentAccount.studentId.eq(studentId))
                .fetch();
    }
}
