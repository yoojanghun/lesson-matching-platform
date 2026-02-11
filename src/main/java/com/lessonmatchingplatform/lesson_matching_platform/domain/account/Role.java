package com.lessonmatchingplatform.lesson_matching_platform.domain.account;

import com.lessonmatchingplatform.lesson_matching_platform.domain.AuditingFields;
import com.lessonmatchingplatform.lesson_matching_platform.type.RoleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@Getter
@Table(indexes = {
        @Index(columnList = "roleType")
})
@Entity
public class Role extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private RoleType roleType;

    protected Role() {}

    private Role(RoleType roleType) {
        this.roleType = roleType;
    }

    public static Role of(RoleType roleType) {
        return new Role(roleType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role that)) return false;
        return this.roleId != null && Objects.equals(this.roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(roleId);
    }
}
