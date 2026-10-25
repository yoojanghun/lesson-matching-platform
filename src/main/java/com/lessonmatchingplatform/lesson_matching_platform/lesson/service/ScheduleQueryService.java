package com.lessonmatchingplatform.lesson_matching_platform.lesson.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.repository.ScheduleExceptionRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.ScheduleRepository;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.CachedScheduleDto;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.dto.response.CachedScheduleExceptionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ScheduleQueryService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleExceptionRepository scheduleExceptionRepository;

    @Cacheable(value = "tutorSchedule", key = "#tutorId + ':' + #dayOfWeek.name()")
    public List<CachedScheduleDto> getCachedTutorSchedules(Long tutorId, DayOfWeek dayOfWeek) {
        return scheduleRepository.findAllByTutorAccount_TutorIdAndDayOfWeek(tutorId, dayOfWeek).stream()
                .map(CachedScheduleDto::from)
                .toList();
    }

    @Cacheable(value = "tutorScheduleException", key = "#tutorId + ':' + #date.toString()")
    public List<CachedScheduleExceptionDto> getCachedScheduleExceptions(Long tutorId, LocalDate date) {
        return scheduleExceptionRepository.findAllByTutorAccount_TutorIdAndExceptionDate(tutorId, date).stream()
                .map(CachedScheduleExceptionDto::from)
                .toList();
    }

    @CacheEvict(value = "tutorSchedule", allEntries = true)
    public void evictScheduleCache(Long tutorId) {
        log.info("튜터 ID {}의 스케줄 캐시 무효화 완료", tutorId);
    }

    @CacheEvict(value = "tutorScheduleException", allEntries = true)
    public void evictScheduleExceptionCache(Long tutorId) {
        log.info("튜터 ID {}의 스케줄 예외 캐시 무효화 완료", tutorId);
    }
}
