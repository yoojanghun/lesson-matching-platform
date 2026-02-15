package com.lessonmatchingplatform.lesson_matching_platform.domain.account;

import com.lessonmatchingplatform.lesson_matching_platform.domain.AuditingFields;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_user_role_user_id_role_id",
                columnNames = {"user_id", "role_id"}
        )
})
@Entity
public class UserRole extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount userAccount;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    protected UserRole() {}

    private UserRole(UserAccount userAccount, Role role) {
        this.userAccount = userAccount;
        this.role = role;
    }

    public static UserRole of(UserAccount userAccount, Role role) {
        return new UserRole(userAccount, role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRole that)) return false;
        return this.id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
