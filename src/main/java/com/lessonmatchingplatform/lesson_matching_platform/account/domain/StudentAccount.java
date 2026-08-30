package com.lessonmatchingplatform.lesson_matching_platform.account.domain;

import com.lessonmatchingplatform.lesson_matching_platform.account.type.LessonType;
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
    private final Set<CategoryStudent> categoryStudentSet = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "studentAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<GoalStudent> goalStudentSet = new LinkedHashSet<>();

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column
    private LessonType lessonType;

    @Column
    private Integer minBudget; // 예: 50000

    @Column
    private Integer maxBudget; // 예: 100000

    protected StudentAccount() {}

    private StudentAccount(UserAccount userAccount, String introduction, LessonType lessonType, Integer minBudget, Integer maxBudget) {
        this.userAccount = userAccount;
        this.introduction = introduction;
        this.lessonType = lessonType;
        this.minBudget = minBudget;
        this.maxBudget = maxBudget;
    }

    public static StudentAccount of(UserAccount userAccount, String introduction, LessonType lessonType, Integer minBudget, Integer maxBudget) {
        return new StudentAccount(userAccount, introduction, lessonType, minBudget, maxBudget);
    }

    public static StudentAccount ofRegister(UserAccount userAccount) {
        return new StudentAccount(userAccount, null, null, null, null);
    }

    public void updateStudentAccount(String introduction, LessonType lessonType, Integer minBudget, Integer maxBudget) {
        if (introduction != null) {
            this.introduction = introduction;
        }
        if (lessonType != null) {
            this.lessonType = lessonType;
        }
        if (minBudget != null) {
            this.minBudget = minBudget;
        }
        if (maxBudget != null) {
            this.maxBudget = maxBudget;
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
