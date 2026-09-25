package com.zoonza.pay.verification.internal.adapter.in.dto;

import com.zoonza.pay.shared.domain.PhoneNumber;
import com.zoonza.pay.verification.internal.application.dto.RequestVerificationCommand;
import com.zoonza.pay.verification.internal.domain.VerificationPurpose;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RequestVerificationRequest(
        @NotNull(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "010-\\d{4}-\\d{4}", message = "전화번호는 010-XXXX-XXXX 형식이어야 합니다.")
        String phoneNumber,

        @NotNull(message = "인증 목적은 필수입니다.")
        VerificationPurpose purpose
) {
    public RequestVerificationCommand toCommand() {
        return new RequestVerificationCommand(
                new PhoneNumber(phoneNumber),
                purpose
        );
    }
}
