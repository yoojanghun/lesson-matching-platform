package com.lessonmatchingplatform.lesson_matching_platform.tutor.search.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Document(indexName = "tutors")
@Setting(settingPath = "elasticsearch/settings/nori-settings.json")
public class TutorDocument {

    @Id
    private Long id; // tutorId

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer"),
            otherFields = {
                    @InnerField(suffix = "raw", type = FieldType.Keyword)
            }
    )
    private String name;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String introduction;

    @MultiField(
            mainField = @Field(type = FieldType.Keyword),
            otherFields = {
                    @InnerField(suffix = "analyzed", type = FieldType.Text, analyzer = "nori_analyzer")
            }
    )
    @Field(type = FieldType.Keyword)
    private List<String> categories;

    @MultiField(
            mainField = @Field(type = FieldType.Keyword),       // 기본: 체크박스 다중 필터용 (완전 일치)
            otherFields = {
                    @InnerField(suffix = "analyzed", type = FieldType.Text, analyzer = "nori_analyzer")     // 서브: 검색창 키워드 검색용 (형태소 분석)
            }
    )
    private List<String> subjects;
    
    @Field(type = FieldType.Keyword)
    private List<String> locations;

    @Field(type = FieldType.Keyword)
    private List<String> goals;

    // 필터링용 ID 필드들
    @Field(type = FieldType.Keyword)
    private List<Long> categoryIds;
    
    @Field(type = FieldType.Keyword)
    private List<Long> subjectIds;
    
    @Field(type = FieldType.Keyword)
    private List<Long> locationIds;
    
    @Field(type = FieldType.Keyword)
    private List<Long> goalIds;
    
    @Field(type = FieldType.Keyword)
    private List<Long> styleIds;
    
    // 추가 필터링 필드
    @Field(type = FieldType.Keyword)
    private String lessonType;
    
    @Field(type = FieldType.Integer)
    private Integer minPrice;
    
    @Field(type = FieldType.Integer)
    private Integer maxPrice;

    @Field(type = FieldType.Double)
    private BigDecimal averageRating;

    @Field(type = FieldType.Integer)
    private Integer reviewCount;
    
    @Field(type = FieldType.Integer)
    private Integer matchingCount;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS||uuuu-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Field(type = FieldType.Double)
    private Double totalScore;
}
