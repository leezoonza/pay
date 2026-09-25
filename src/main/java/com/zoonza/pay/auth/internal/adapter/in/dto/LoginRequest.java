package com.zoonza.pay.auth.internal.adapter.in.dto;

import com.zoonza.pay.auth.internal.application.dto.LoginCommand;
import com.zoonza.pay.shared.domain.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotNull(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "010-\\d{4}-\\d{4}", message = "전화번호는 010-XXXX-XXXX 형식이어야 합니다.")
        String phoneNumber,

        @NotBlank(message = "인증 ID는 필수입니다.")
        String verificationId
) {
    public LoginCommand toCommand() {
        return new LoginCommand(
                new PhoneNumber(phoneNumber),
                verificationId
        );
    }
}
