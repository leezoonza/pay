package com.zoonza.pay.verification.internal.application.dto;

import com.zoonza.pay.verification.internal.domain.VerificationCode;

public record ConfirmVerificationCommand(
        String verificationId,
        VerificationCode code
) {
}
