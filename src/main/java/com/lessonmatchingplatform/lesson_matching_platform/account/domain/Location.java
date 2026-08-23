package com.lessonmatchingplatform.lesson_matching_platform.account.domain;

import com.lessonmatchingplatform.lesson_matching_platform.global.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.*;

@ToString(callSuper = true)
@Getter
@Table(indexes = {
        @Index(columnList = "name")
})
@Entity
public class Location extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long locationId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Location parent;

    @ToString.Exclude                                               // 연관관계 주인만 값을 INSERT, UPDATE 가능
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)      // 주인이 아닌 쪽은 조회만 가능
    private final Set<Location> children = new LinkedHashSet<>();   // Linked처럼 순서 보장, Hash처럼 조회O(1), Set처럼 중복 방지

    @ToString.Exclude
    @OneToMany(mappedBy = "tutorAccount", cascade = CascadeType.ALL)
    private final Set<LocationTutor> locationTutorSet = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "studentAccount", cascade = CascadeType.ALL)
    private final Set<LocationStudent> locationStudentSet = new LinkedHashSet<>();

    @Column(length = 50, nullable = false)
    private String name;

    protected Location() {}

    private Location(String name, Location parent) {
        this.name = name;
        this.parent = parent;
        if (parent != null) {
            parent.getChildren().add(this);
        }
    }

    // 최상위 지역(시/도) 생성 팩토리 메서드
    public static Location of(String name) {
        return new Location(name, null);
    }

    // 하위 지역(시/군/구) 생성 팩토리 메서드
    public static Location ofSubregion(String name, Location parent) {
        return new Location(name, parent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location that)) return false;
        return this.locationId != null && Objects.equals(this.locationId, that.locationId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(locationId);
    }
}
