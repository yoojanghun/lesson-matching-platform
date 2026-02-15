package com.lessonmatchingplatform.lesson_matching_platform.domain.account;

import com.lessonmatchingplatform.lesson_matching_platform.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_location_tutor_tutor_id_location_id",
                columnNames = {"tutor_id", "location_id"}
        )
})
@Getter
@Entity
public class LocationTutor extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private TutorAccount tutorAccount;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    protected LocationTutor() {}

    private LocationTutor(TutorAccount tutorAccount, Location location) {
        this.tutorAccount = tutorAccount;
        this.location = location;
    }

    public static LocationTutor of(TutorAccount tutorAccount, Location location) {
        return new LocationTutor(tutorAccount, location);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationTutor that)) return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
