package com.zoonza.pay.customer.internal.adapter.in.dto;

import com.zoonza.pay.customer.internal.application.dto.RegisterCustomerCommand;
import com.zoonza.pay.customer.internal.domain.Name;
import com.zoonza.pay.customer.internal.domain.PhoneNumber;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(
        @NotNull(message = "이름은 필수입니다.")
        @Pattern(regexp = "(?s).{2,30}", message = "이름은 2자 이상 30자 이하여야 합니다.")
        @Pattern(regexp = "\\p{L}*", message = "이름에는 문자만 사용할 수 있습니다.")
        String name,

        @NotNull(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "010-\\d{4}-\\d{4}", message = "전화번호는 010-XXXX-XXXX 형식이어야 합니다.")
        String phoneNumber
) {
    public RegisterCustomerCommand toCommand() {
        return new RegisterCustomerCommand(
                new Name(name),
                new PhoneNumber(phoneNumber)
        );
    }
}