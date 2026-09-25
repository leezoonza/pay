package com.zoonza.pay.verification.internal.application.dto;

import com.zoonza.pay.shared.domain.PhoneNumber;
import com.zoonza.pay.verification.api.VerificationPurpose;

public record RequestVerificationCommand(
        PhoneNumber phoneNumber,
        VerificationPurpose purpose
) {
}
