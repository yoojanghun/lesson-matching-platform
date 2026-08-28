package com.lessonmatchingplatform.lesson_matching_platform.account.service;

import com.lessonmatchingplatform.lesson_matching_platform.account.domain.*;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.*;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.StudentProfileRequest;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.request.TutorProfileRequest;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.StudentProfileResponse;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.LessonGoalRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.LocationRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.StudentRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.repository.TutorStyleRepository;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Category;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.CategoryTutor;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.Subject;
import com.lessonmatchingplatform.lesson_matching_platform.category.domain.SubjectTutor;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.CategoryRepository;
import com.lessonmatchingplatform.lesson_matching_platform.category.repository.SubjectRepository;
import com.lessonmatchingplatform.lesson_matching_platform.account.dto.response.TutorProfileResponse;
import com.lessonmatchingplatform.lesson_matching_platform.tutor.repository.TutorsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class ProfileService {

    private final StudentRepository studentRepository;
    private final TutorsRepository tutorsRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final SubjectRepository subjectRepository;
    private final TutorStyleRepository tutorStyleRepository;
    private final LessonGoalRepository lessonGoalRepository;

    @Transactional(readOnly = true)
    public StudentProfileResponse getMyStudentProfile(Long id) {
        StudentAccount studentAccount = studentRepository.findProfileById(id)
                .orElseThrow(() -> new EntityNotFoundException("학생 프로필을 찾을 수 없습니다."));

        List<StyleTypeDto> styles = studentAccount.getStyleStudentSet().stream()
                .map(styleStudent -> StyleTypeDto.of(styleStudent.getTutorStyle()))
                .toList();

        List<CategoryTypeDto> interestCategoryTypeDtos = studentAccount.getCategoryStudentSet().stream()
                .map(categoryStudent -> CategoryTypeDto.from(categoryStudent.getCategory()))
                .toList();

        List<GoalTypeDto> goals = studentAccount.getGoalStudentSet().stream()
                .map(goalStudent -> GoalTypeDto.of(goalStudent.getLessonGoal()))
                .toList();

        List<LocationDto> locationDtos = studentRepository.findLocationDtosByStudentId(id);

        return StudentProfileResponse.of(
                studentAccount,
                styles,
                interestCategoryTypeDtos,
                goals,
                locationDtos
        );
    }

    public void postMyStudentProfile(Long id, StudentProfileRequest request) {
        StudentAccount studentAccount = studentRepository.findProfileById(id)
                .orElseThrow(() -> new EntityNotFoundException("학생 프로필을 찾을 수 없습니다."));      // 영속성 컨텍스트에 StudentAccount 스냅샷 저장

        UserAccount userAccount = studentAccount.getUserAccount();

        if (request.phoneNumber() != null) {
            userAccount.updatePhoneNumber(request.phoneNumber());
        }

        // CascadeType.ALL에 의해 studentAccount 자식도 영속성 컨텍스트가 관리. Dirty Checking 이후 commit 시점에 flush 될 때, DB에 save됨
        if (request.styleIds() != null) {
            List<TutorStyle> tutorStyleList = tutorStyleRepository.findAllById(request.styleIds());
            tutorStyleList.forEach(tutorStyle -> studentAccount.getStyleStudentSet().add(
                    StyleStudent.of(studentAccount, tutorStyle)
            ));
        }
        if (request.categoryIds() != null) {
            List<Category> categoryList = categoryRepository.findAllById(request.categoryIds());
            categoryList.forEach(category -> studentAccount.getCategoryStudentSet().add(
                    CategoryStudent.of(studentAccount, category)
            ));
        }
        if (request.goalIds() != null){
            List<LessonGoal> lessonGoalList = lessonGoalRepository.findAllById(request.goalIds());
            lessonGoalList.forEach(lessonGoal -> studentAccount.getGoalStudentSet().add(
                    GoalStudent.of(studentAccount, lessonGoal)
            ));
        }
        if (request.locationIds() != null && !request.locationIds().isEmpty()) {
            List<Location> locationList = locationRepository.findAllById(request.locationIds());
            locationList.forEach(location -> studentAccount.getLocationStudentSet().add(
                    LocationStudent.of(studentAccount, location)
            ));
        }
        studentAccount.updateStudentAccount(request.introduction(), request.lessonType(), request.budgetType());
    }

    public void putMyStudentProfile(Long id, StudentProfileRequest request) {
        StudentAccount studentAccount = studentRepository.findProfileById(id)
                .orElseThrow(() -> new EntityNotFoundException("학생 프로필을 찾을 수 없습니다."));

        UserAccount userAccount = studentAccount.getUserAccount();

        if (request.phoneNumber() != null) {
            userAccount.updatePhoneNumber(request.phoneNumber());
        }
        if (request.styleIds() != null) {
            studentAccount.getStyleStudentSet().clear(); // 빈 배열([]) 요청 시에도 기존 목록 해제
            if (!request.styleIds().isEmpty()) {
                List<TutorStyle> tutorStyleList = tutorStyleRepository.findAllById(request.styleIds());
                tutorStyleList.forEach(tutorStyle ->
                        studentAccount.getStyleStudentSet().add(StyleStudent.of(studentAccount, tutorStyle))
                );
            }
        }
        if (request.categoryIds() != null) {
            studentAccount.getCategoryStudentSet().clear();
            if (!request.categoryIds().isEmpty()) {
                List<Category> categoryList = categoryRepository.findAllById(request.categoryIds());
                categoryList.forEach(category ->
                        studentAccount.getCategoryStudentSet().add(CategoryStudent.of(studentAccount, category))
                );
            }
        }
        if (request.goalIds() != null) {
            studentAccount.getGoalStudentSet().clear();
            if (!request.goalIds().isEmpty()) {
                List<LessonGoal> lessonGoalList = lessonGoalRepository.findAllById(request.goalIds());
                lessonGoalList.forEach(lessonGoal ->
                        studentAccount.getGoalStudentSet().add(GoalStudent.of(studentAccount, lessonGoal))
                );
            }
        }
        if (request.locationIds() != null) {
            studentAccount.getLocationStudentSet().clear();
            if (!request.locationIds().isEmpty()) {
                List<Location> locationList = locationRepository.findAllById(request.locationIds());
                locationList.forEach(location ->
                        studentAccount.getLocationStudentSet().add(LocationStudent.of(studentAccount, location))
                );
            }
        }
        studentAccount.updateStudentAccount(request.introduction(), request.lessonType(), request.budgetType());
    }

    @Transactional(readOnly = true)
    public TutorProfileResponse getMyTutorProfile(Long id) {
        TutorAccount tutorAccount = tutorsRepository.findProfileById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 강사를 찾을 수 없습니다. id=" + id));

        List<CategoryTypeDto> categoryTypeDtos = tutorAccount.getCategoryTutorSet().stream()
                .map(categoryTutor -> CategoryTypeDto.from(categoryTutor.getCategory()))
                .toList();

        List<SubjectTypeDto> subjectTypeDtos = tutorAccount.getSubjectTutorSet().stream()
                .map(subjectTutor -> SubjectTypeDto.from(subjectTutor.getSubject()))
                .toList();

        List<LocationDto> locationDtos = tutorsRepository.findLocationDtosByTutorId(id);

        List<StyleTypeDto> styleTypeDtos = tutorAccount.getStyleTutorSet().stream()
                .map(styleTutor -> StyleTypeDto.of(styleTutor.getTutorStyle()))
                .toList();

        return TutorProfileResponse.of(
                tutorAccount,
                categoryTypeDtos,
                subjectTypeDtos,
                locationDtos,
                styleTypeDtos
        );
    }

    public void postMyTutorProfile(Long tutorId, TutorProfileRequest request) {
        TutorAccount tutorAccount = tutorsRepository.findProfileById(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 강사를 찾을 수 없습니다. id=" + tutorId));

        UserAccount userAccount = tutorAccount.getUserAccount();
        if (request.name() != null) {
            userAccount.updateName(request.name());
        }
        if (request.phoneNumber() != null) {
            userAccount.updatePhoneNumber(request.phoneNumber());
        }
        if (request.birthDate() != null) {
            userAccount.updateBirthDate(request.birthDate());
        }
        if (request.email() != null) {
            userAccount.updateEmail(request.email());
        }

        tutorAccount.updateProfile(
                request.title(),
                request.content(),
                request.introduction(),
                request.career(),
                request.isBirthDatePublic(),
                request.isEmailPublic(),
                request.isPhoneNumberPublic()
        );

        if (request.styleIds() != null && !request.styleIds().isEmpty()) {
            List<TutorStyle> tutorStyleList = tutorStyleRepository.findAllById(request.styleIds());
            tutorStyleList.forEach(tutorStyle -> tutorAccount.addStyleTutor(StyleTutor.of(tutorAccount, tutorStyle)));
        }

        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
            List<Category> categoryList = categoryRepository.findAllById(request.categoryIds());
            categoryList.forEach(category -> tutorAccount.addCategoryTutor(CategoryTutor.of(tutorAccount, category)));
        }

        if (request.subjectIds() != null && !request.subjectIds().isEmpty()) {
            List<Subject> subjectList = subjectRepository.findAllById(request.subjectIds());
            subjectList.forEach(subject -> tutorAccount.addSubjectTutor(SubjectTutor.of(tutorAccount, subject)));
        }

        if (request.locationIds() != null && !request.locationIds().isEmpty()) {
            List<Location> locationList = locationRepository.findAllById(request.locationIds());
            locationList.forEach(location -> tutorAccount.addLocationTutor(LocationTutor.of(tutorAccount, location)));
        }
    }

    public void putMyTutorProfile(Long tutorId, TutorProfileRequest request) {
        TutorAccount tutorAccount = tutorsRepository.findProfileById(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("해당 강사를 찾을 수 없습니다. id=" + tutorId));

        UserAccount userAccount = tutorAccount.getUserAccount();
        if (request.name() != null) {
            userAccount.updateName(request.name());
        }
        if (request.phoneNumber() != null) {
            userAccount.updatePhoneNumber(request.phoneNumber());
        }
        if (request.birthDate() != null) {
            userAccount.updateBirthDate(request.birthDate());
        }
        if (request.email() != null) {
            userAccount.updateEmail(request.email());
        }

        tutorAccount.updateProfile(
                request.title(),
                request.content(),
                request.introduction(),
                request.career(),
                request.isBirthDatePublic(),
                request.isEmailPublic(),
                request.isPhoneNumberPublic()
        );

        if (request.styleIds() != null && !request.styleIds().isEmpty()) {
            tutorAccount.getStyleTutorSet().clear();
            List<TutorStyle> tutorStyleList = tutorStyleRepository.findAllById(request.styleIds());
            tutorStyleList.forEach(tutorStyle -> tutorAccount.addStyleTutor(StyleTutor.of(tutorAccount, tutorStyle)));
        }

        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
            tutorAccount.getCategoryTutorSet().clear();
            List<Category> categoryList = categoryRepository.findAllById(request.categoryIds());
            categoryList.forEach(category -> tutorAccount.addCategoryTutor(CategoryTutor.of(tutorAccount, category)));
        }

        if (request.subjectIds() != null && !request.subjectIds().isEmpty()) {
            tutorAccount.getSubjectTutorSet().clear();
            List<Subject> subjectList = subjectRepository.findAllById(request.subjectIds());
            subjectList.forEach(subject -> tutorAccount.addSubjectTutor(SubjectTutor.of(tutorAccount, subject)));
        }

        if (request.locationIds() != null && !request.locationIds().isEmpty()) {
            tutorAccount.getLocationTutorSet().clear();
            List<Location> locationList = locationRepository.findAllById(request.locationIds());
            locationList.forEach(location -> tutorAccount.addLocationTutor(LocationTutor.of(tutorAccount, location)));
        }
    }
}
