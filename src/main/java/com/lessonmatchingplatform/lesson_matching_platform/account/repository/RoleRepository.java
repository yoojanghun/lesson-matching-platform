package com.lessonmatchingplatform.lesson_matching_platform.account.repository;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}
