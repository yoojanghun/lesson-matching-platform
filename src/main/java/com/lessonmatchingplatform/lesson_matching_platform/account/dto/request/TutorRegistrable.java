package com.lessonmatchingplatform.lesson_matching_platform.account.dto.request;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.TutorLessonPriceDto;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.LessonType;

import java.util.List;

public interface TutorRegistrable {
    List<Long> categoryIds();
    List<Long> subjectIds();
    String title();
    String introduction();
    LessonType lessonType();
    List<String> educations();
    List<String> experiences();
    List<Long> locationIds();
    List<Long> styleIds();
    List<Long> goalIds();
    List<TutorLessonPriceDto> lessonPriceDtos();
    String bankName();
    String bankAccountNumber();
    String bankAccountHolder();
}
