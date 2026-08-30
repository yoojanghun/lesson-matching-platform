package com.lessonmatchingplatform.lesson_matching_platform.category.repository;

import com.lessonmatchingplatform.lesson_matching_platform.category.domain.SubjectTutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectTutorRepository extends JpaRepository<SubjectTutor, Long> {

    @Query("SELECT st FROM SubjectTutor st JOIN FETCH st.subject s WHERE st.tutorAccount.tutorId IN :tutorIds")
    List<SubjectTutor> findAllByTutorAccount_TutorIdIn(List<Long> tutorIds);
}
