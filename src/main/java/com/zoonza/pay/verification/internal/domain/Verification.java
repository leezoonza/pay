package com.zoonza.pay.verification.internal.domain;

import com.zoonza.pay.shared.domain.PhoneNumber;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Verification(
        String id,
        PhoneNumber phoneNumber,
        VerificationPurpose purpose,
        VerificationCode code,
        VerificationStatus status,
        Instant expiresAt
) {
    public static final Duration CODE_VALIDITY = Duration.ofMinutes(5);

    public Verification {
        Objects.requireNonNull(id, "인증 ID는 필수입니다.");
        Objects.requireNonNull(phoneNumber, "전화번호는 필수입니다.");
        Objects.requireNonNull(purpose, "인증 목적은 필수입니다.");
        Objects.requireNonNull(code, "인증번호는 필수입니다.");
        Objects.requireNonNull(status, "인증 상태는 필수입니다.");
        Objects.requireNonNull(expiresAt, "만료 시각은 필수입니다.");
    }

    public static Verification request(
            PhoneNumber phoneNumber,
            VerificationPurpose purpose,
            VerificationCode code,
            Instant now
    ) {
        return new Verification(
                UUID.randomUUID().toString(),
                phoneNumber,
                purpose,
                code,
                VerificationStatus.REQUESTED,
                now.plus(CODE_VALIDITY)
        );
    }
}
