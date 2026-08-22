package com.lessonmatchingplatform.lesson_matching_platform.account.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.Role;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.StudentAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.TutorAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserAccount;
import com.lessonmatchingplatform.lesson_matching_platform.account.domain.UserRole;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.*;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.RoleRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.StudentRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.UserRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.UserRoleRepository;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;

import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import jakarta.persistence.EntityNotFoundException;
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

        public void signUpTutor(TutorSignUpRequest request) {
                UserAccount userAccount = UserAccount.ofRegister(
                                request.userId(),
                                passwordEncoder.encode(request.userPassword()), // password는 암호화 한 후 저장
                                request.name(),
                                request.email());
                userRepository.save(userAccount);

                Role role = roleRepository.getReferenceById(1L);
                UserRole userRole = UserRole.of(userAccount, role);
                userRoleRepository.save(userRole);

                TutorAccount tutorAccount = TutorAccount.ofRegister(userAccount);
                tutorsRepository.save(tutorAccount);
        }

        public void signUpTutorFromGuest(BoardPrincipal boardPrincipal) {
                UserAccount userToUpdate = userRepository.findById(boardPrincipal.id())
                                .orElseThrow(() -> new EntityNotFoundException("관련 GUEST 계정이 없습니다."));

                userRoleRepository.deleteByUserAccount(userToUpdate);
                userRoleRepository.flush();                                     //  JPA 쿼리 순서 꼬임 방지

                Role tutorRole = roleRepository.getReferenceById(1L);           // TUTOR
                UserRole userRole = UserRole.of(userToUpdate, tutorRole);
                userRoleRepository.save(userRole);

                TutorAccount tutorAccount = TutorAccount.ofRegister(userToUpdate);
                tutorsRepository.save(tutorAccount);
        }

        public void signUpStudent(StudentSignupRequest request) {
                UserAccount userAccount = UserAccount.ofRegister(
                                request.userId(),
                                passwordEncoder.encode(request.userPassword()),
                                request.name(),
                                request.email());
                userRepository.save(userAccount);

                Role role = roleRepository.getReferenceById(2L);
                UserRole userRole = UserRole.of(userAccount, role);
                userRoleRepository.save(userRole);

                StudentAccount studentAccount = StudentAccount.ofRegister(userAccount);
                studentRepository.save(studentAccount);
        }

        public void signUpStudentFromGuest(BoardPrincipal boardPrincipal) {
                UserAccount userToUpdate = userRepository.findById(boardPrincipal.id())
                                .orElseThrow(() -> new EntityNotFoundException("관련 GUEST 계정이 없습니다."));

                userRoleRepository.deleteByUserAccount(userToUpdate);
                userRoleRepository.flush();

                Role studentRole = roleRepository.getReferenceById(2L);
                UserRole userRole = UserRole.of(userToUpdate, studentRole);
                userRoleRepository.save(userRole);

                StudentAccount studentAccount = StudentAccount.ofRegister(userToUpdate);
                studentRepository.save(studentAccount);
        }

        // Student로 등록한 경우 Tutor 등록(계정 전환)
        public void switchTutor(Long id) {
                UserAccount userAccount = userRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("관련 GUEST 계정이 없습니다."));

                Role role = roleRepository.getReferenceById(1L);
                UserRole userRole = UserRole.of(userAccount, role);
                userRoleRepository.save(userRole);

                TutorAccount tutorAccount = TutorAccount.ofRegister(userAccount);
                tutorsRepository.save(tutorAccount);
        }

        public void switchStudent(Long id) {
                UserAccount userAccount = userRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("관련 GUEST 계정이 없습니다."));

                Role role = roleRepository.getReferenceById(2L);
                UserRole userRole = UserRole.of(userAccount, role);
                userRoleRepository.save(userRole);

                StudentAccount studentAccount = StudentAccount.ofRegister(userAccount);
                studentRepository.save(studentAccount);
        }

        @Transactional(readOnly = true)
        public Boolean checkDuplicateId(String userId) {
                return userRepository.existsByUserId(userId);
        }

        public boolean checkDuplicateEmail(String email) {
                return userRepository.existsByEmail(email);
        }
}
