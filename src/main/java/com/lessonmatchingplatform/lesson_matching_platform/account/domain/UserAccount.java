package com.lessonmatchingplatform.lesson_matching_platform.account.domain;

import com.lessonmatchingplatform.lesson_matching_platform.global.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import org.apache.catalina.User;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@ToString(callSuper = true)
@Getter
@Table(indexes = {
        @Index(columnList = "name"),
        @Index(columnList = "birthDate"),
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

    @ToString.Exclude
    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL)
    private final Set<UserRole> userRoleSet = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private GenderType gender;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(length = 20, nullable = false, unique = true)
    private String phoneNumber;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 50)
    private String provider;                // e.g., google

    @Column(length = 100)
    private String providerId;              // e.g., google's sub id

    protected UserAccount() {}

    private UserAccount(String userId, String userPassword, String name, GenderType gender, LocalDate birthDate, String phoneNumber, String email, String provider, String providerId) {
        this.userId = userId;
        this.userPassword = userPassword;
        this.name = name;
        this.gender = gender;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
    }

    public static UserAccount of(String userId, String userPassword, String name, GenderType gender, LocalDate birthDate, String phoneNumber, String email) {
        return new UserAccount(userId, userPassword, name, gender, birthDate, phoneNumber, email, null, null);
    }

    public static UserAccount ofOAuth2(String userId, String userPassword, String name, String email, String provider, String providerId, String tempPhoneNumber, Role guestRole) {
        UserAccount user = new UserAccount(userId, userPassword, name, GenderType.UNKNOWN, LocalDate.of(1900, 1, 1), tempPhoneNumber, email, provider, providerId);

        UserRole userRole = UserRole.of(user, guestRole);
        user.getUserRoleSet().add(userRole);

        return user;
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
