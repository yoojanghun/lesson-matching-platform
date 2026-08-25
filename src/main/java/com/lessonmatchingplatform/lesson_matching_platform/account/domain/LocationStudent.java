package com.lessonmatchingplatform.lesson_matching_platform.account.domain;

import com.lessonmatchingplatform.lesson_matching_platform.global.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Table(name = "location_student", uniqueConstraints = {             // 복합 유니크 키
        @UniqueConstraint(
                name = "uk_student_location",
                columnNames = {"student_id", "location_id"}
        )
})
@Entity
public class LocationStudent extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private StudentAccount studentAccount;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    protected LocationStudent() {}

    private LocationStudent(StudentAccount studentAccount, Location location) {
        this.studentAccount = studentAccount;
        this.location = location;

        if (studentAccount != null) {
            studentAccount.getLocationStudentSet().add(this);
        }
        if (location != null) {
            location.getLocationStudentSet().add(this);
        }
    }

    public static LocationStudent of(StudentAccount studentAccount, Location location) {
        return new LocationStudent(studentAccount, location);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationStudent that)) return false;
        return this.id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
