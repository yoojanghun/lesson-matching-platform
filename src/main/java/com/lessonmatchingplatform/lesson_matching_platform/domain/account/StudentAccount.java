package com.lessonmatchingplatform.lesson_matching_platform.domain.account;

import com.lessonmatchingplatform.lesson_matching_platform.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

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

    @Column(length = 50)
    private String location;

    protected StudentAccount() {}

    private StudentAccount(UserAccount userAccount, String introduction, String location) {
        this.userAccount = userAccount;
        this.introduction = introduction;
        this.location = location;
    }

    public StudentAccount of(UserAccount userAccount, String introduction, String location) {
        return new StudentAccount(userAccount, introduction, location);
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
