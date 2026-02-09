package com.lessonmatchingplatform.lesson_matching_platform.repository;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
}
