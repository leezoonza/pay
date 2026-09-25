package com.zoonza.pay.verification.internal.adapter.in.dto;

import com.zoonza.pay.verification.internal.application.dto.ConfirmVerificationCommand;
import com.zoonza.pay.verification.internal.domain.VerificationCode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ConfirmVerificationRequest(
        @NotNull(message = "인증번호는 필수입니다.")
        @Pattern(regexp = "\\d{6}", message = "인증번호는 6자리 숫자여야 합니다.")
        String code
) {
    public ConfirmVerificationCommand toCommand(String verificationId) {
        return new ConfirmVerificationCommand(
                verificationId,
                new VerificationCode(code)
        );
    }
}
