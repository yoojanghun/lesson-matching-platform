package com.lessonmatchingplatform.lesson_matching_platform.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TutorBankAccountRequest(

        @NotNull(message = "은행명은 필수입니다.")
        @NotBlank(message = "은행명은 필수입니다.")
        String bankName,

        @NotNull(message = "계좌번호는 필수입니다.")
        @NotBlank(message = "계좌번호는 필수입니다.")
        String bankAccountNumber,

        @NotNull(message = "예금주명은 필수입니다.")
        @NotBlank(message = "예금주명은 필수입니다.")
        String bankAccountHolder
) {
}