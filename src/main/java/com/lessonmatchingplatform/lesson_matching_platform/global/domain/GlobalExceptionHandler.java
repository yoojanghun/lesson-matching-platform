package com.lessonmatchingplatform.lesson_matching_platform.global.domain;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // DTO 검증 실패 시
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        CustomErrorResponse response = new CustomErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 엔티티 조회 실패 시 (404 Not Found)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<CustomErrorResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        CustomErrorResponse response = new CustomErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // DTO @Valid 입력값 검증 실패 시 (400 Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        CustomErrorResponse response = new CustomErrorResponse(errorMessage, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(response);
    }

    // 비즈니스 로직 실패 시 (409 Conflict)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<CustomErrorResponse> handleIllegalStateException(IllegalStateException e) {
        // 상황에 따라 400(Bad Request) 혹은 409(Conflict)로 내려주면 적절
        CustomErrorResponse response = new CustomErrorResponse(e.getMessage(), HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // 예측하지 못한 심각한 서버 에러 발생 시 (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponse> handleException(Exception e) {
        CustomErrorResponse response = new CustomErrorResponse("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    public record CustomErrorResponse(
            String message,
            int status
    ) {}
}
