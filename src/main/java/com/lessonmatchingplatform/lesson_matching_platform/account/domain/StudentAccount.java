package com.lessonmatchingplatform.lesson_matching_platform.account.domain;

import com.lessonmatchingplatform.lesson_matching_platform.account.type.BudgetType;
import com.lessonmatchingplatform.lesson_matching_platform.global.domain.AuditingFields;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
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
    @MapsId                                 // 여기에 이미 index 설정이 되어 있음
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private UserAccount userAccount;

    @ToString.Exclude
    @OneToMany(mappedBy = "studentAccount", cascade = CascadeType.ALL)
    private final Set<Matching> matchingSet = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "studentAccount", cascade = CascadeType.ALL, orphanRemoval = true)    // 부모 객체에서 자식 객체 remove시
    private final Set<LocationStudent> locationStudentSet = new LinkedHashSet<>();              // DB에도 DELETE 쿼리가 실행되도록

    @ToString.Exclude
    @OneToMany(mappedBy = "studentAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<StyleStudent> styleStudentSet = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "studentAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<InterestCategory> interestCategorySet = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "studentAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<LessonGoal> lessonGoalSet = new LinkedHashSet<>();

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column
    private String lessonType;

    @Enumerated(EnumType.STRING)
    @Column
    private BudgetType budgetType;

    protected StudentAccount() {}

    private StudentAccount(UserAccount userAccount, String introduction, String lessonType, BudgetType budgetType) {
        this.userAccount = userAccount;
        this.introduction = introduction;
        this.lessonType = lessonType;
        this.budgetType = budgetType;
    }

    public static StudentAccount of(UserAccount userAccount, String introduction, String lessonType, BudgetType budgetType) {
        return new StudentAccount(userAccount, introduction, lessonType, budgetType);
    }

    public static StudentAccount ofRegister(UserAccount userAccount) {
        return new StudentAccount(userAccount, null, null, null);
    }

    public void updateStudentAccount(String introduction, String lessonType, BudgetType budgetType) {
        if (introduction != null) {
            this.introduction = introduction;
        }
        if (lessonType != null) {
            this.lessonType = lessonType;
        }
        if (budgetType != null) {
            this.budgetType = budgetType;
        }
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
