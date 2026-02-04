package com.lessonmatchingplatform.lesson_matching_platform.domain.account;

import com.lessonmatchingplatform.lesson_matching_platform.domain.AuditingFields;
import com.lessonmatchingplatform.lesson_matching_platform.type.GenderType;
import com.lessonmatchingplatform.lesson_matching_platform.type.RoleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Table(indexes = {
        @Index(columnList = "userId"),
        @Index(columnList = "phoneNumber"),
        @Index(columnList = "email")
})
@Entity
public class UserAccount extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String userId;

    @Column(length = 255, nullable = false)
    private String userPassword;

    @Column(length = 50, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private RoleType role;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private GenderType gender;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(length = 20, nullable = false)
    private String phoneNumber;

    @Column(length = 100, nullable = false)
    private String email;

    protected UserAccount() {}

    private UserAccount(String userId, String userPassword, String name, RoleType role, GenderType gender ,LocalDate birthDate, String phoneNumber, String email) {
        this.userId = userId;
        this.userPassword = userPassword;
        this.name = name;
        this.role = role;
        this.gender = gender;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public static UserAccount of(String userId, String userPassword, String name, RoleType role, GenderType gender ,LocalDate birthDate, String phoneNumber, String email) {
        return new UserAccount(userId, userPassword, name, role, gender, birthDate, phoneNumber, email);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAccount that)) return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
