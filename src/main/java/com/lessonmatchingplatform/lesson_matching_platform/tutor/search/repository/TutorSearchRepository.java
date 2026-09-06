package com.lessonmatchingplatform.lesson_matching_platform.tutor.search.repository;

import com.lessonmatchingplatform.lesson_matching_platform.tutor.search.document.TutorDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TutorSearchRepository extends ElasticsearchRepository<TutorDocument, Long> {
}
