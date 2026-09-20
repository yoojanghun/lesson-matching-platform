package com.lessonmatchingplatform.lesson_matching_platform.tutor.search.service;

import com.lessonmatchingplatform.lesson_matching_platform.tutor.search.document.TutorDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.lessonmatchingplatform.lesson_matching_platform.account.type.LessonType;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.dto.request.TutorSearchCondition;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class TutorSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public TutorSearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public Page<TutorDocument> searchTutors(String keyword, TutorSearchCondition condition, Pageable pageable) {
        Criteria mainCriteria = null;

        // 1. 키워드 검색 조건 생성 (OR 조건 묶음)
        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            mainCriteria = new Criteria("name").matches(trimmedKeyword).boost(3.0f)
                    .or(new Criteria("title").matches(trimmedKeyword).boost(2.0f))
                    .or(new Criteria("introduction").matches(trimmedKeyword))
                    .or(new Criteria("categories.analyzed").matches(trimmedKeyword))
                    .or(new Criteria("subjects.analyzed").matches(trimmedKeyword));
        }

        // 2. 다중 필터 적용 (AND 조건 추가)
        if (condition != null) {
            mainCriteria = appendFilter(mainCriteria, "categoryIds", condition.categoryIds());
            mainCriteria = appendFilter(mainCriteria, "subjectIds", condition.subjectIds());
            mainCriteria = appendFilter(mainCriteria, "locationIds", condition.locationIds());
            mainCriteria = appendFilter(mainCriteria, "goalIds", condition.goalIds());
            mainCriteria = appendFilter(mainCriteria, "styleIds", condition.styleIds());

            // 금액 필터
            if (condition.minPrice() != null) {
                Criteria priceCriteria = new Criteria("maxPrice").greaterThanEqual(condition.minPrice());
                mainCriteria = (mainCriteria == null) ? priceCriteria : mainCriteria.and(priceCriteria);
            }
            if (condition.maxPrice() != null) {
                Criteria priceCriteria = new Criteria("minPrice").lessThanEqual(condition.maxPrice());
                mainCriteria = (mainCriteria == null) ? priceCriteria : mainCriteria.and(priceCriteria);
            }

            // 수강 방식 필터
            if (condition.lessonType() != null) {
                Criteria lessonCriteria;
                if (condition.lessonType() == LessonType.ONLINE) {
                    lessonCriteria = new Criteria("lessonType").in(LessonType.ONLINE.name(), LessonType.BOTH.name());
                } else if (condition.lessonType() == LessonType.OFFLINE) {
                    lessonCriteria = new Criteria("lessonType").in(LessonType.OFFLINE.name(), LessonType.BOTH.name());
                } else {
                    lessonCriteria = new Criteria("lessonType").is(LessonType.BOTH.name());
                }
                mainCriteria = (mainCriteria == null) ? lessonCriteria : mainCriteria.and(lessonCriteria);
            }
        }

        // 키워드 및 필터 조건이 완전히 비어있을 경우 전체 조회
        CriteriaQuery query = (mainCriteria != null) ? new CriteriaQuery(mainCriteria) : new CriteriaQuery(new Criteria());
        query.setPageable(pageable);

        // 정렬 조건 설정 (키워드가 없을 때: 인기순 -> 최신순)
        if (!StringUtils.hasText(keyword)) {
            query.addSort(Sort.by(Sort.Direction.DESC, "totalScore"));
            query.addSort(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        SearchHits<TutorDocument> searchHits = elasticsearchOperations.search(query, TutorDocument.class);

        List<TutorDocument> content = searchHits.stream()
                .map(SearchHit::getContent)
                .toList();

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    // List 필터 생성을 위한 헬퍼 메서드
    private Criteria appendFilter(Criteria baseCriteria, String fieldName, List<?> values) {
        if (CollectionUtils.isEmpty(values)) {
            return baseCriteria;
        }
        Criteria filterCriteria = new Criteria(fieldName).in(values);
        return (baseCriteria == null) ? filterCriteria : baseCriteria.and(filterCriteria);
    }
}
