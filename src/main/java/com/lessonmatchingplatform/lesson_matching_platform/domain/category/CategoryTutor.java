package com.lessonmatchingplatform.lesson_matching_platform.domain.category;

import com.lessonmatchingplatform.lesson_matching_platform.domain.AuditingFields;
import com.lessonmatchingplatform.lesson_matching_platform.domain.account.TutorAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "category_tutor_tutor_id_category_id",
                columnNames = {"tutor_id", "category_id"}
        )
})
@Entity
public class CategoryTutor extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private TutorAccount tutorAccount;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    protected CategoryTutor() {}

    private CategoryTutor(TutorAccount tutorAccount, Category category) {
        this.tutorAccount = tutorAccount;
        this.category = category;
    }

    public static CategoryTutor of(TutorAccount tutorAccount, Category category) {
        return new CategoryTutor(tutorAccount, category);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryTutor that)) return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
