package com.lessonmatchingplatform.lesson_matching_platform.service;

import com.lessonmatchingplatform.lesson_matching_platform.domain.account.*;
import com.lessonmatchingplatform.lesson_matching_platform.domain.category.Category;
import com.lessonmatchingplatform.lesson_matching_platform.domain.category.CategoryTutor;
import com.lessonmatchingplatform.lesson_matching_platform.domain.category.Subject;
import com.lessonmatchingplatform.lesson_matching_platform.domain.category.SubjectTutor;
import com.lessonmatchingplatform.lesson_matching_platform.dto.request.StudentSignupRequest;
import com.lessonmatchingplatform.lesson_matching_platform.dto.request.TutorSignUpRequest;
import com.lessonmatchingplatform.lesson_matching_platform.dto.request.TutorSwitchRequest;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.StudentMyResponse;
import com.lessonmatchingplatform.lesson_matching_platform.dto.response.TutorResponse;
import com.lessonmatchingplatform.lesson_matching_platform.dto.security.BoardPrincipal;
import com.lessonmatchingplatform.lesson_matching_platform.repository.*;
import com.lessonmatchingplatform.lesson_matching_platform.type.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Transactional
@Service
public class SignUpService {

    private final UserRepository userRepository;
    private final TutorsRepository tutorsRepository;
    private final CategoryRepository categoryRepository;
    private final SubjectRepository subjectRepository;
    private final CategoryTutorRepository categoryTutorRepository;
    private final SubjectTutorRepository subjectTutorRepository;
    private final LocationTutorRepository locationTutorRepository;
    private final LocationRepository locationRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;

    public TutorResponse signUpTutor(TutorSignUpRequest request) {

        UserAccount userAccount = UserAccount.of(
                request.userId(),
                passwordEncoder.encode(request.userPassword()),         // password는 암호화 한 후 저장
                request.name(),
                RoleType.TUTOR,
                request.gender(),
                request.birthDate(),
                request.phoneNumber(),
                request.email()
        );
        userRepository.save(userAccount);

        TutorAccount tutorAccount = TutorAccount.of(
                userAccount,
                request.introduction(),
                request.career(),
                request.title(),
                request.content()
        );
        tutorsRepository.save(tutorAccount);

        return saveTutorAssociations(tutorAccount, request.categoryId(), request.subjectId(), request.locationId());
    }

    public StudentMyResponse signUpStudent(StudentSignupRequest request) {

        UserAccount userAccount = UserAccount.of(
                request.userId(),
                passwordEncoder.encode(request.userPassword()),
                request.name(),
                RoleType.STUDENT,
                request.gender(),
                request.birthDate(),
                request.phoneNumber(),
                request.email()
        );
        userRepository.save(userAccount);

        StudentAccount studentAccount = StudentAccount.of(
                userAccount,
                request.introduction()
        );
        StudentAccount savedStudent = studentRepository.save(studentAccount);

        return StudentMyResponse.from(savedStudent);
    }

    public TutorResponse switchTutor(BoardPrincipal boardPrincipal, TutorSwitchRequest request) {
         UserAccount userAccount = userRepository.findByUserId(boardPrincipal.username())
                 .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다." + boardPrincipal.username()));

         userAccount.setRole(RoleType.TUTOR);

         TutorAccount tutorAccount = TutorAccount.of(
                 userAccount,
                 request.introduction(),
                 request.career(),
                 request.title(),
                 request.content()
         );
         tutorsRepository.save(tutorAccount);

         return saveTutorAssociations(tutorAccount, request.categoryId(), request.subjectId(), request.locationId());
    }

    private TutorResponse saveTutorAssociations(TutorAccount tutorAccount, Set<Long> categoryIds, Set<Long> subjectIds, Set<Long> locationIds) {
        if(categoryIds != null && !categoryIds.isEmpty()) {
            List<CategoryTutor> categoryTutors = categoryIds.stream()
                    .map(id -> {
                        Category category = categoryRepository.getReferenceById(id);
                        CategoryTutor categoryTutor = CategoryTutor.of(tutorAccount, category);
                        tutorAccount.addCategoryTutor(categoryTutor);
                        return categoryTutor;
                    }).toList();

            categoryTutorRepository.saveAll(categoryTutors);
        }

        if(subjectIds != null && !subjectIds.isEmpty()) {
            List<SubjectTutor> subjectTutors = subjectIds.stream()
                    .map(id -> {
                        Subject subject = subjectRepository.getReferenceById(id);
                        SubjectTutor subjectTutor = SubjectTutor.of(tutorAccount, subject);
                        tutorAccount.addSubjectTutor(subjectTutor);
                        return subjectTutor;
                    }).toList();

            subjectTutorRepository.saveAll(subjectTutors);
        }

        if(locationIds != null && !locationIds.isEmpty()) {
            List<LocationTutor> locationTutors = locationIds.stream()
                    .map(id -> {
                        Location location = locationRepository.getReferenceById(id);
                        LocationTutor locationTutor = LocationTutor.of(tutorAccount, location);
                        tutorAccount.addLocationTutor(locationTutor);
                        return locationTutor;
                    }).toList();

            locationTutorRepository.saveAll(locationTutors);
        }

        return TutorResponse.from(tutorAccount);
    }
}
