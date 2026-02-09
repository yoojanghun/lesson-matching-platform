package com.lessonmatchingplatform.lesson_matching_platform.domain.account;

import com.lessonmatchingplatform.lesson_matching_platform.domain.AuditingFields;
import com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.Matching;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@ToString(callSuper = true)
@Getter
@Entity
public class StudentAccount extends AuditingFields {

    @Id
    private Long studentId;

    @ToString.Exclude
    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private UserAccount userAccount;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String introduction;

    @ToString.Exclude
    @OneToMany(mappedBy = "studentAccount", cascade = CascadeType.ALL)
    private final Set<Matching> matchingSet = new LinkedHashSet<>();

    protected StudentAccount() {}

    private StudentAccount(UserAccount userAccount, String introduction) {
        this.userAccount = userAccount;
        this.introduction = introduction;
    }

    public static StudentAccount of(UserAccount userAccount, String introduction) {
        return new StudentAccount(userAccount, introduction);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudentAccount that)) return false;
        return this.studentId != null && Objects.equals(this.studentId, that.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(studentId);
    }
}
