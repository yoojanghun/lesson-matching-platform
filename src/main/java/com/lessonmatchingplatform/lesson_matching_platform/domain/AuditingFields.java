package com.lessonmatchingplatform.lesson_matching_platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@Getter
@EntityListeners(AuditingFields.class)
@MappedSuperclass
public class AuditingFields {

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 100, nullable = false)
    private String createdBy;

    @Column
    private LocalDateTime modifiedAt;

    @Column(length = 100)
    private String modifiedBy;
}
