package com.lessonmatchingplatform.lesson_matching_platform.account.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.*;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.*;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.*;
import com.lessonmatchingplatform.lesson_matching_platform.account.type.LessonType;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Category;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.CategoryTutor;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Subject;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.SubjectTutor;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.CategoryRepository;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.CategoryTutorRepository;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.SubjectRepository;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.SubjectTutorRepository;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;

import com.lessonmatchingplatform.lesson_matching_platform.global.security.BoardPrincipal;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private final CategoryRepository categoryRepository;
    private final CategoryTutorRepository categoryTutorRepository;
    private final SubjectRepository subjectRepository;
    private final SubjectTutorRepository subjectTutorRepository;
    private final LocationRepository locationRepository;
    private final LocationTutorRepository locationTutorRepository;
    private final TutorStyleRepository tutorStyleRepository;
    private final StyleTutorRepository styleTutorRepository;
    private final LessonGoalRepository lessonGoalRepository;
    private final GoalTutorRepository goalTutorRepository;

    public void signUpTutor(TutorSignUpRequest request) {
        UserAccount userAccount = UserAccount.ofRegister(
                request.userId(),
                passwordEncoder.encode(request.userPassword()), // password는 암호화 한 후 저장
                request.name());
        userRepository.save(userAccount);
        registerAsTutor(userAccount, request);
    }

    public void signUpTutorFromGuest(Long id, SwitchToTutorRequest request) {
        UserAccount userAccount = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("관련 GUEST 계정이 없습니다."));

        userRoleRepository.deleteByUserAccount(userAccount);
        userAccount.getUserRoleSet().clear();

        registerAsTutor(userAccount, request);
    }

    public void signUpStudent(StudentSignupRequest request) {
        UserAccount userAccount = UserAccount.ofRegister(
                request.userId(),
                passwordEncoder.encode(request.userPassword()),
                request.name());
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
        userToUpdate.getUserRoleSet().clear();

        Role studentRole = roleRepository.getReferenceById(2L);
        UserRole userRole = UserRole.of(userToUpdate, studentRole);
        userRoleRepository.save(userRole);

        StudentAccount studentAccount = StudentAccount.ofRegister(userToUpdate);
        studentRepository.save(studentAccount);
    }

    // Student로 등록한 경우 Tutor 등록(계정 전환)
    public void switchTutor(Long id, SwitchToTutorRequest request) {
        UserAccount userAccount = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다. id=" + id));

        registerAsTutor(userAccount, request);
    }

    public void switchStudent(Long id) {
        UserAccount userAccount = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다. id=" + id));

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

    @Transactional(readOnly = true)
    public boolean checkDuplicateEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private void registerAsTutor(UserAccount userAccount, TutorRegistrable request) {
        if (request.lessonType() != LessonType.ONLINE) {
            if (request.locationIds() == null || request.locationIds().isEmpty()) {
                throw new IllegalArgumentException("대면 레슨을 진행하는 경우 활동 지역을 최소 하나 이상 선택해야 합니다.");
            }
        }

        Role role = roleRepository.getReferenceById(1L);
        UserRole userRole = UserRole.of(userAccount, role);
        userRoleRepository.save(userRole);

        TutorAccount tutorAccount = TutorAccount.ofRegister(
                userAccount,
                request.title(),
                request.introduction(),
                request.educations(),
                request.experiences(),
                request.lessonType()
        );

        tutorsRepository.save(tutorAccount);

        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.categoryIds());
            List<CategoryTutor> categoryTutors = categories.stream()
                    .map(category -> CategoryTutor.of(tutorAccount, category))
                    .toList();
            categoryTutorRepository.saveAll(categoryTutors);
        }

        if (request.subjectIds() != null && !request.subjectIds().isEmpty()) {
            List<Subject> subjects = subjectRepository.findAllById(request.subjectIds());
            List<SubjectTutor> subjectTutors = subjects.stream()
                    .map(subject -> SubjectTutor.of(tutorAccount, subject))
                    .toList();
            subjectTutorRepository.saveAll(subjectTutors);
        }

        if (request.locationIds() != null && !request.locationIds().isEmpty()) {
            List<Location> locations = locationRepository.findAllById(request.locationIds());
            List<LocationTutor> locationTutors = locations.stream()
                    .map(location -> LocationTutor.of(tutorAccount, location))
                    .toList();
            locationTutorRepository.saveAll(locationTutors);
        }

        if (request.styleIds() != null && !request.styleIds().isEmpty()) {
            List<TutorStyle> tutorStyles = tutorStyleRepository.findAllById(request.styleIds());
            List<StyleTutor> styleTutors = tutorStyles.stream()
                    .map(tutorStyle -> StyleTutor.of(tutorAccount, tutorStyle))
                    .toList();
            styleTutorRepository.saveAll(styleTutors);
        }

        if (request.goalIds() != null && !request.goalIds().isEmpty()) {
            List<LessonGoal> lessonGoals = lessonGoalRepository.findAllById(request.goalIds());
            List<GoalTutor> goalTutors = lessonGoals.stream()
                    .map(lessonGoal -> GoalTutor.of(tutorAccount, lessonGoal))
                    .toList();
            goalTutorRepository.saveAll(goalTutors);
        }

        if (request.lessonPriceDtos() != null && !request.lessonPriceDtos().isEmpty()) {
            request.lessonPriceDtos()
                    .forEach(lessonPriceDto -> tutorAccount.addTutorLessonPrice(
                            TutorLessonPrice.of(tutorAccount, lessonPriceDto.className(), lessonPriceDto.price())
                    ));
        }

        if (request.bankName() != null && !request.bankName().isBlank()
                && request.bankAccountNumber() != null && !request.bankAccountNumber().isBlank()
                && request.bankAccountHolder() != null && !request.bankAccountHolder().isBlank()) {
            tutorAccount.updateBankAccount(request.bankName(), request.bankAccountNumber(), request.bankAccountHolder());
        }
    }
}
