package com.lessonmatchingplatform.lesson_matching_platform.domain.account;

import com.lessonmatchingplatform.lesson_matching_platform.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

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

    @Column(length = 50, nullable = false)
    private String name;

    protected Location() {}

    private Location(String name) {
        this.name = name;
    }

    public static Location of(String name) {
        return new Location(name);
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
