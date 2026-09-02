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

@RequiredArgsConstructor
public class CategoryTutorRepositoryImpl implements CategoryTutorRepositoryCustom{

    private JPAQueryFactory queryFactory;

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
