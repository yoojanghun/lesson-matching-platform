package com.lessonmatchingplatform.lesson_matching_platform.account.domain;

import com.lessonmatchingplatform.lesson_matching_platform.account.type.ProfileStatus;
import com.lessonmatchingplatform.lesson_matching_platform.global.domain.AuditingFields;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.CategoryTutor;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.SubjectTutor;
import com.lessonmatchingplatform.lesson_matching_platform.lesson.domain.Matching;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import com.lessonmatchingplatform.lesson_matching_platform.global.converter.StringListJsonConverter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@ToString(callSuper = true)
@Getter
@Entity
public class TutorAccount extends AuditingFields {

    @Id
    private Long tutorId;

    @ToString.Exclude
    @MapsId                                 // 여기에 이미 index 설정이 되어 있음
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private UserAccount userAccount;

    @Column(length = 100)
    private String title;                   // 소개글 제목

    @Lob
    @Column(columnDefinition = "TEXT")
    private String introduction;            // 소개글

    @Convert(converter = StringListJsonConverter.class)
    @Column(columnDefinition = "json")
    private List<String> educations = new ArrayList<>(); // 학력

    @Convert(converter = StringListJsonConverter.class)
    @Column(columnDefinition = "json")
    private List<String> experiences = new ArrayList<>(); // 경력

    @Column(precision = 2, scale = 1, nullable = false)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer reviewCount = 0;

    @Column(nullable = false)
    private Integer matchingCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileStatus profileStatus;

    @Column(nullable = false)
    private Boolean isBirthDatePublic = false;

    @Column(nullable = false)
    private Boolean isEmailPublic = false;

    @Column(nullable = false)
    private Boolean isPhoneNumberPublic = false;

    @ToString.Exclude
    @OneToMany(mappedBy = "tutorAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<CategoryTutor> categoryTutorSet = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "tutorAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<SubjectTutor> subjectTutorSet = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "tutorAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<StyleTutor> styleTutorSet = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "tutorAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<LocationTutor> locationTutorSet = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "tutorAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<GoalTutor> goalTutorSet = new LinkedHashSet<>();

    @ToString.Exclude
    @OrderBy("createdAt DESC")
    @OneToMany(mappedBy = "tutorAccount", cascade = CascadeType.ALL)
    private final Set<Matching> matchingSet = new LinkedHashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "tutorAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<TutorLessonPrice> tutorLessonPriceSet = new LinkedHashSet<>();

    public void addTutorLessonPrice(TutorLessonPrice tutorLessonPrice) {
        if (this.tutorLessonPriceSet.size() >= 3) {
            throw new IllegalArgumentException("레슨 가격은 최대 3개까지만 설정할 수 있습니다.");
        }
        this.tutorLessonPriceSet.add(tutorLessonPrice);
    }

    public void addCategoryTutor(CategoryTutor categoryTutor) {
        this.categoryTutorSet.add(categoryTutor);
    }

    public void addSubjectTutor(SubjectTutor subjectTutor) {
        this.subjectTutorSet.add(subjectTutor);
    }

    public void addLocationTutor(LocationTutor locationTutor) {
        this.locationTutorSet.add(locationTutor);
    }

    public void addGoalTutor(GoalTutor goalTutor) {
        this.goalTutorSet.add(goalTutor);
    }

    public void addStyleTutor(StyleTutor styleTutor) {
        this.styleTutorSet.add(styleTutor);
    }

    public void updateRating(BigDecimal newRating) {
        BigDecimal totalScore = this.averageRating
                .multiply(BigDecimal.valueOf(this.reviewCount))
                .add(newRating);

        this.reviewCount++;

        this.averageRating = totalScore.divide(
                BigDecimal.valueOf(reviewCount),
                1,
                RoundingMode.HALF_UP
        );
    }

    public void increaseMatchingCount() {
        this.matchingCount++;
    }

    public void updateProfile(String title, List<String> educations, String introduction, List<String> experiences, Boolean isBirthDatePublic, Boolean isEmailPublic, Boolean isPhoneNumberPublic) {
        if (title != null) {
            this.title = title;
        }
        if (educations != null) {
            this.educations = new ArrayList<>(educations);
        }
        if (introduction != null) {
            this.introduction = introduction;
        }
        if (experiences != null) {
            this.experiences = new ArrayList<>(experiences);;
        }
        if (isBirthDatePublic != null) {
            this.isBirthDatePublic = isBirthDatePublic;
        }
        if (isEmailPublic != null) {
            this.isEmailPublic = isEmailPublic;
        }
        if (isPhoneNumberPublic != null) {
            this.isPhoneNumberPublic = isPhoneNumberPublic;
        }
    }

    public void updateProfileCompletionStatus() {
        if (this.title != null && this.introduction != null && 
            this.getUserAccount().getName() != null && !this.getUserAccount().getName().isBlank() && 
            this.getGoalTutorSet() != null && !this.getGoalTutorSet().isEmpty()
        ) {
            this.profileStatus = ProfileStatus.COMPLETED;
        } else {
            this.profileStatus = ProfileStatus.INCOMPLETE;
        }
    }

    protected TutorAccount() {}

    private TutorAccount(UserAccount userAccount, String introduction, List<String> experiences, String title, List<String> educations, ProfileStatus profileStatus) {
        this.userAccount = userAccount;
        this.introduction = introduction;
        this.experiences = experiences != null ? experiences : new ArrayList<>();
        this.title = title;
        this.educations = educations != null ? educations : new ArrayList<>();
        this.profileStatus = profileStatus;
    }

    public static TutorAccount of(UserAccount userAccount, String introduction, List<String> experiences, String title, List<String> educations, ProfileStatus profileStatus) {
        return new TutorAccount(userAccount, introduction, experiences, title, educations, profileStatus);
    }

    public static TutorAccount ofRegister(UserAccount userAccount) {
        return new TutorAccount(userAccount, null, new ArrayList<>(), null, new ArrayList<>(), ProfileStatus.INCOMPLETE);
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
