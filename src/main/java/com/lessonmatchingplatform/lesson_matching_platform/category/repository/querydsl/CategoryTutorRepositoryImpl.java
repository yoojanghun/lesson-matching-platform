package com.lessonmatchingplatform.lesson_matching_platform.category.repository.querydsl;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.*;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.TutorProfileResponse;

import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QGoalTutor.goalTutor;
import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QLessonGoal.lessonGoal;
import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QLocation.location;
import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QLocationTutor.locationTutor;
import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QStyleTutor.styleTutor;
import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QTutorAccount.tutorAccount;
import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QTutorLessonPrice.tutorLessonPrice;
import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QTutorStyle.tutorStyle;
import static com.lessonmatchingplatform.lesson_matching_platform.account.domain.QUserAccount.userAccount;
import static com.lessonmatchingplatform.lesson_matching_platform.category.domain.QCategory.category;
import static com.lessonmatchingplatform.lesson_matching_platform.category.domain.QCategoryTutor.categoryTutor;
import static com.lessonmatchingplatform.lesson_matching_platform.category.domain.QSubject.subject;
import static com.lessonmatchingplatform.lesson_matching_platform.category.domain.QSubjectTutor.subjectTutor;

import com.lessonmatchingplatform.lesson_matching_platform.account.type.ProfileStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CategoryTutorRepositoryImpl implements CategoryTutorRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<TutorProfileResponse> findProfileResponseByIds(List<Long> tutorIds) {
        if (tutorIds == null || tutorIds.isEmpty()) {
            return List.of();
        }

        // 1. 강사 계정 기본 정보 IN 쿼리 (단 1번)
        List<TutorAccount> accounts = queryFactory
                .selectFrom(tutorAccount)
                .join(tutorAccount.userAccount, userAccount).fetchJoin()
                .where(
                        tutorAccount.tutorId.in(tutorIds),
                        tutorAccount.profileStatus.eq(ProfileStatus.COMPLETED)
                )
                .fetch();

        if (accounts.isEmpty()) {
            return List.of();
        }

        // 2. 각 연관 데이터 IN 쿼리 (단 1번씩)

        // Category Map
        Map<Long, List<CategoryTypeDto>> categoryMap = queryFactory
                .select(
                        categoryTutor.tutorAccount.tutorId,
                        Projections.constructor(
                                CategoryTypeDto.class,
                                category.categoryId,
                                category.name
                        )
                )
                .from(categoryTutor)
                .join(categoryTutor.category, category)
                .where(categoryTutor.tutorAccount.tutorId.in(tutorIds))
                .fetch()
                .stream()
                .filter(tuple -> tuple != null && tuple.get(0, Long.class) != null)
                .collect(Collectors.groupingBy(
                        tuple -> Objects.requireNonNull(tuple.get(0, Long.class)),
                        Collectors.mapping(
                                tuple -> Objects.requireNonNull(tuple.get(1, CategoryTypeDto.class)),
                                Collectors.toList()
                        )
                ));

        // Subject Map
        Map<Long, List<SubjectTypeDto>> subjectMap = queryFactory
                .select(
                        subjectTutor.tutorAccount.tutorId,
                        Projections.constructor(
                                SubjectTypeDto.class,
                                subject.subjectId,
                                subject.name
                        )
                )
                .from(subjectTutor)
                .join(subjectTutor.subject, subject)
                .where(subjectTutor.tutorAccount.tutorId.in(tutorIds))
                .fetch()
                .stream()
                .filter(tuple -> tuple != null && tuple.get(0, Long.class) != null)
                .collect(Collectors.groupingBy(
                        tuple -> Objects.requireNonNull(tuple.get(0, Long.class)),
                        Collectors.mapping(
                                tuple -> Objects.requireNonNull(tuple.get(1, SubjectTypeDto.class)),
                                Collectors.toList()
                        )
                ));

        // Location Map
        Map<Long, List<LocationDto>> locationMap = queryFactory
                .select(
                        locationTutor.tutorAccount.tutorId,
                        Projections.constructor(
                                LocationDto.class,
                                location.locationId,
                                location.name
                        )
                )
                .from(locationTutor)
                .join(locationTutor.location, location)
                .where(locationTutor.tutorAccount.tutorId.in(tutorIds))
                .fetch()
                .stream()
                .filter(tuple -> tuple != null && tuple.get(0, Long.class) != null)
                .collect(Collectors.groupingBy(
                        tuple -> Objects.requireNonNull(tuple.get(0, Long.class)),
                        Collectors.mapping(
                                tuple -> Objects.requireNonNull(tuple.get(1, LocationDto.class)),
                                Collectors.toList()
                        )
                ));

        // Style Map
        Map<Long, List<StyleTypeDto>> styleMap = queryFactory
                .select(
                        styleTutor.tutorAccount.tutorId,
                        Projections.constructor(
                                StyleTypeDto.class,
                                tutorStyle.styleId,
                                tutorStyle.styleType.stringValue()
                        )
                )
                .from(styleTutor)
                .join(styleTutor.tutorStyle, tutorStyle)
                .where(styleTutor.tutorAccount.tutorId.in(tutorIds))
                .fetch()
                .stream()
                .filter(tuple -> tuple != null && tuple.get(0, Long.class) != null)
                .collect(Collectors.groupingBy(
                        tuple -> Objects.requireNonNull(tuple.get(0, Long.class)),
                        Collectors.mapping(
                                tuple -> Objects.requireNonNull(tuple.get(1, StyleTypeDto.class)),
                                Collectors.toList()
                        )
                ));

        // Goal Map
        Map<Long, List<GoalTypeDto>> goalMap = queryFactory
                .select(
                        goalTutor.tutorAccount.tutorId,
                        Projections.constructor(
                                GoalTypeDto.class,
                                lessonGoal.goalId,
                                lessonGoal.lessonGoalType
                        )
                )
                .from(goalTutor)
                .join(goalTutor.lessonGoal, lessonGoal)
                .where(goalTutor.tutorAccount.tutorId.in(tutorIds))
                .fetch()
                .stream()
                .filter(tuple -> tuple != null && tuple.get(0, Long.class) != null)
                .collect(Collectors.groupingBy(
                        tuple -> Objects.requireNonNull(tuple.get(0, Long.class)),
                        Collectors.mapping(
                                tuple -> Objects.requireNonNull(tuple.get(1, GoalTypeDto.class)),
                                Collectors.toList()
                        )
                ));

        // Price Map
        Map<Long, List<TutorLessonPriceDto>> priceMap = queryFactory
                .select(
                        tutorLessonPrice.tutorAccount.tutorId,
                        Projections.constructor(
                                TutorLessonPriceDto.class,
                                tutorLessonPrice.className,
                                tutorLessonPrice.price
                        )
                )
                .from(tutorLessonPrice)
                .where(tutorLessonPrice.tutorAccount.tutorId.in(tutorIds))
                .fetch()
                .stream()
                .filter(tuple -> tuple != null && tuple.get(0, Long.class) != null)
                .collect(Collectors.groupingBy(
                        tuple -> Objects.requireNonNull(tuple.get(0, Long.class)),
                        Collectors.mapping(
                                tuple -> Objects.requireNonNull(tuple.get(1, TutorLessonPriceDto.class)),
                                Collectors.toList()
                        )
                ));

        // 3. 자바 메모리 단에서 최종 DTO 조립
        return accounts.stream()
                .map(account -> TutorProfileResponse.of(
                        account,
                        categoryMap.getOrDefault(account.getTutorId(), List.of()),
                        subjectMap.getOrDefault(account.getTutorId(), List.of()),
                        locationMap.getOrDefault(account.getTutorId(), List.of()),
                        styleMap.getOrDefault(account.getTutorId(), List.of()),
                        goalMap.getOrDefault(account.getTutorId(), List.of()),
                        priceMap.getOrDefault(account.getTutorId(), List.of())
                ))
                .toList();
    }

    @Override
    public TutorProfileResponse findProfileResponseById(Long tutorId) {
        TutorAccount account = queryFactory
                .selectFrom(tutorAccount)
                .join(tutorAccount.userAccount, userAccount).fetchJoin()
                .where(
                        tutorAccount.tutorId.eq(tutorId),
                        tutorAccount.profileStatus.eq(ProfileStatus.COMPLETED)
                )
                .fetchOne();

        if (account == null) {
            return null;
        }

        List<CategoryTypeDto> categoryTypeDtoList = queryFactory
                .select(Projections.constructor(CategoryTypeDto.class, category.categoryId, category.name))
                .from(categoryTutor)
                .join(categoryTutor.category, category)
                .where(categoryTutor.tutorAccount.tutorId.eq(tutorId))
                .fetch();

        List<SubjectTypeDto> subjectTypeDtoList = queryFactory
                .select(Projections.constructor(SubjectTypeDto.class, subject.subjectId, subject.name))
                .from(subjectTutor)
                .join(subjectTutor.subject, subject)
                .where(subjectTutor.tutorAccount.tutorId.eq(tutorId))
                .fetch();

        List<LocationDto> locationDtoList = queryFactory
                .select(Projections.constructor(LocationDto.class, location.locationId, location.name))
                .from(locationTutor)
                .join(locationTutor.location, location)
                .where(locationTutor.tutorAccount.tutorId.eq(tutorId))
                .fetch();

        List<StyleTypeDto> styleTypeDtoList = queryFactory
                .select(Projections.constructor(StyleTypeDto.class, tutorStyle.styleId, tutorStyle.styleType.stringValue()))
                .from(styleTutor)
                .join(styleTutor.tutorStyle, tutorStyle)
                .where(styleTutor.tutorAccount.tutorId.eq(tutorId))
                .fetch();

        List<GoalTypeDto> goalTypeDtoList = queryFactory
                .select(Projections.constructor(GoalTypeDto.class, lessonGoal.goalId, lessonGoal.lessonGoalType))
                .from(goalTutor)
                .join(goalTutor.lessonGoal, lessonGoal)
                .where(goalTutor.tutorAccount.tutorId.eq(tutorId))
                .fetch();

        List<TutorLessonPriceDto> tutorLessonPriceDtoList = queryFactory
                .select(Projections.constructor(TutorLessonPriceDto.class, tutorLessonPrice.className, tutorLessonPrice.price))
                .from(tutorLessonPrice)
                .where(tutorLessonPrice.tutorAccount.tutorId.eq(tutorId))
                .fetch();

        return TutorProfileResponse.of(
                account,
                categoryTypeDtoList,
                subjectTypeDtoList,
                locationDtoList,
                styleTypeDtoList,
                goalTypeDtoList,
                tutorLessonPriceDtoList
        );
    }
}
