package com.lessonmatchingplatform.lesson_matching_platform.domain.account;

import com.lessonmatchingplatform.lesson_matching_platform.domain.AuditingFields;
import com.lessonmatchingplatform.lesson_matching_platform.domain.lesson.LessonPost;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@ToString(callSuper = true)
@Getter
@Entity
public class TutorAccount extends AuditingFields {

    @Id
    private Long tutorId;

    @ToString.Exclude
    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private UserAccount userAccount;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String career;

    @ToString.Exclude
    @OneToMany(mappedBy = "tutorAccount", cascade = CascadeType.ALL)
    private final Set<LessonPost> lessonPosts = new LinkedHashSet<>();

    protected TutorAccount() {}

    private TutorAccount(UserAccount userAccount, String introduction, String career) {
        this.userAccount = userAccount;
        this.introduction = introduction;
        this.career = career;
    }

    public static TutorAccount of(UserAccount userAccount, String introduction, String career) {
        return new TutorAccount(userAccount, introduction, career);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TutorAccount that)) return false;
        return this.tutorId != null && Objects.equals(this.tutorId, that.tutorId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tutorId);
    }
}
