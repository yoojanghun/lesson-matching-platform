package com.lessonmatchingplatform.lesson_matching_platform.dto.security;
import com.lessonmatchingplatform.lesson_matching_platform.domain.account.Role;
import com.lessonmatchingplatform.lesson_matching_platform.domain.account.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.type.GenderType;
import com.lessonmatchingplatform.lesson_matching_platform.type.RoleType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record BoardPrincipal(
        String username,
        String password,
        Collection<? extends GrantedAuthority> authorities,
        String name,
        GenderType gender,
        LocalDate birthDate,
        String phoneNumber,
        String email
) implements UserDetails {

    public static BoardPrincipal from(UserAccount entity) {

        // 사용자의 권한 리스트 (ROLE_TUTOR, ROLE_STUDENT 등)
        Collection<SimpleGrantedAuthority> authorities = entity.getUserRoleSet().stream()
                .map(userRole -> {
                    String roleName = userRole.getRole().getRoleType().name();      // "TUTOR"
                    return new SimpleGrantedAuthority("ROLE_" + roleName);
                })
                .toList();

        return new BoardPrincipal(
                entity.getUserId(),
                entity.getUserPassword(),
                authorities,
                entity.getName(),
                entity.getGender(),
                entity.getBirthDate(),
                entity.getPhoneNumber(),
                entity.getEmail()
        );
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {return authorities;}
    @Override public String getPassword() {return password;}
    @Override public String getUsername() {return username;}
    @Override public boolean isAccountNonExpired() {return true;}
    @Override public boolean isAccountNonLocked() {return true;}
    @Override public boolean isCredentialsNonExpired() {return true;}
    @Override public boolean isEnabled() {return true;}

}
