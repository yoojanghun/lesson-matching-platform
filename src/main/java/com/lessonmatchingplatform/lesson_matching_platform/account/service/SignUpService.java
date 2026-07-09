package com.lessonmatchingplatform.lesson_matching_platform.account.service;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.Role;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.StudentAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserRole;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.RoleRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.StudentRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.UserRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.UserRoleRepository;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;

import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.StudentSignupRequest;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.StudentSwitchRequest;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.TutorSignUpRequest;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.TutorSwitchRequest;
import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class SignUpService {

    private final UserRepository userRepository;
    private final TutorsRepository tutorsRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    public void signUpTutor(TutorSignUpRequest request) {       // TODO: userRole에 GUEST 있으면 삭제
        UserAccount userAccount = UserAccount.of(
                request.userId(),
                passwordEncoder.encode(request.userPassword()),         // password는 암호화 한 후 저장
                request.name(),
                request.gender(),
                request.birthDate(),
                request.phoneNumber(),
                request.email()
        );
        userRepository.save(userAccount);

        Role role = roleRepository.getReferenceById(1L);
        UserRole userRole = UserRole.of(userAccount, role);
        userRoleRepository.save(userRole);

        TutorAccount tutorAccount = TutorAccount.of(
                userAccount,
                request.introduction(),
                request.career(),
                request.title(),
                request.content()
        );
        tutorsRepository.save(tutorAccount);
    }

    public void signUpStudent(StudentSignupRequest request) {
        UserAccount userAccount = UserAccount.of(
                request.userId(),
                passwordEncoder.encode(request.userPassword()),
                request.name(),
                request.gender(),
                request.birthDate(),
                request.phoneNumber(),
                request.email()
        );
        userRepository.save(userAccount);

        Role role = roleRepository.getReferenceById(2L);
        UserRole userRole = UserRole.of(userAccount, role);
        userRoleRepository.save(userRole);

        StudentAccount studentAccount = StudentAccount.of(
                userAccount,
                request.introduction()
        );
        studentRepository.save(studentAccount);
    }

    public void switchTutor(BoardPrincipal boardPrincipal, TutorSwitchRequest request) {
        UserAccount userAccount = userRepository.getReferenceById(boardPrincipal.id());

        Role role = roleRepository.getReferenceById(1L);
        UserRole userRole = UserRole.of(userAccount, role);
        userRoleRepository.save(userRole);

        TutorAccount tutorAccount = TutorAccount.of(
                 userAccount,
                 request.introduction(),
                 request.career(),
                 request.title(),
                 request.content()
         );
         tutorsRepository.save(tutorAccount);
    }

    public void switchStudent(BoardPrincipal boardPrincipal, StudentSwitchRequest request) {
        UserAccount userAccount = userRepository.getReferenceById(boardPrincipal.id());

        Role role = roleRepository.getReferenceById(2L);
        UserRole userRole = UserRole.of(userAccount, role);
        userRoleRepository.save(userRole);

        StudentAccount studentAccount = StudentAccount.of(
                userAccount,
                request.introduction()
        );

        studentRepository.save(studentAccount);
    }
}
