package com.zoonza.pay.auth.internal.application.dto;

import com.zoonza.pay.shared.domain.PhoneNumber;

public record LoginCommand(
        PhoneNumber phoneNumber,
        String verificationId
) {
}
