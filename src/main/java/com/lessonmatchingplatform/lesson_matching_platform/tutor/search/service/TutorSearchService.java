package com.lessonmatchingplatform.lesson_matching_platform.tutor.search.service;

import com.lessonmatchingplatform.lesson_matching_platform.tutor.search.document.TutorDocument;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Service
public class TutorSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public Page<TutorDocument> searchTutors(String keyword, TutorSearchCondition condition, Pageable pageable) {
        Criteria criteria = new Criteria();

        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();

            // 다중 필드 OR 검색 조건 생성 (가중치 Boost 부여)
            Criteria keywordCriteria = new Criteria("name").matches(trimmedKeyword).boost(3.0f)
                    .or(new Criteria("title").matches(trimmedKeyword).boost(2.0f))
                    .or(new Criteria("introduction").matches(trimmedKeyword))
                    .or(new Criteria("categories.analyzed").matches(trimmedKeyword))
                    .or(new Criteria("subjects.analyzed").matches(trimmedKeyword));     // Nori 분석기가 적용된 서브필드(.analyzed)로 검색

            criteria = criteria.and(keywordCriteria);
        }

        // --- 다중 필터 적용 시작 ---
        if (condition != null) {
            if (!CollectionUtils.isEmpty(condition.categoryIds())) {
                criteria = criteria.and("categoryIds").in(condition.categoryIds());
            }
            if (!CollectionUtils.isEmpty(condition.subjectIds())) {
                criteria = criteria.and("subjectIds").in(condition.subjectIds());
            }
            if (!CollectionUtils.isEmpty(condition.locationIds())) {
                criteria = criteria.and("locationIds").in(condition.locationIds());
            }
            if (!CollectionUtils.isEmpty(condition.goalIds())) {
                criteria = criteria.and("goalIds").in(condition.goalIds());
            }
            if (!CollectionUtils.isEmpty(condition.styleIds())) {
                criteria = criteria.and("styleIds").in(condition.styleIds());
            }
            
            // 금액 필터: (document.minPrice <= maxPrice AND document.maxPrice >= minPrice) 와 같이 겹치는 범위 조건
            if (condition.minPrice() != null) {
                criteria = criteria.and("maxPrice").greaterThanEqual(condition.minPrice());
            }
            if (condition.maxPrice() != null) {
                criteria = criteria.and("minPrice").lessThanEqual(condition.maxPrice());
            }
            
            // 레슨 타입 필터
            if (condition.lessonType() != null) {
                if (condition.lessonType() == LessonType.ONLINE) {
                    criteria = criteria.and("lessonType").in(LessonType.ONLINE.name(), LessonType.BOTH.name());
                } else if (condition.lessonType() == LessonType.OFFLINE) {
                    criteria = criteria.and("lessonType").in(LessonType.OFFLINE.name(), LessonType.BOTH.name());
                } else if (condition.lessonType() == LessonType.BOTH) {
                    criteria = criteria.and("lessonType").is(LessonType.BOTH.name());
                }
            }
        }
        // --- 다중 필터 적용 끝 ---

        CriteriaQuery query = new CriteriaQuery(criteria);
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
}
