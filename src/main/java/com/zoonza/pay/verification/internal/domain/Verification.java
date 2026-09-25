package com.zoonza.pay.verification.internal.domain;

import com.zoonza.pay.shared.domain.PhoneNumber;
import com.zoonza.pay.shared.error.BusinessException;
import com.zoonza.pay.verification.api.VerificationPurpose;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Verification {
    public static final Duration CODE_VALIDITY = Duration.ofMinutes(5);
    public static final Duration VERIFIED_VALIDITY = Duration.ofMinutes(10);
    public static final int MAX_FAILED_ATTEMPTS = 5;

    private String id;
    private PhoneNumber phoneNumber;
    private VerificationPurpose purpose;
    private VerificationCode code;
    private VerificationStatus status;
    private Instant expiresAt;
    private int failedAttempts;

    private Verification(
            String id,
            PhoneNumber phoneNumber,
            VerificationPurpose purpose,
            VerificationCode code,
            Instant expiresAt
    ) {
        this.id = Objects.requireNonNull(id, "인증 ID는 필수입니다.");
        this.phoneNumber = Objects.requireNonNull(phoneNumber, "전화번호는 필수입니다.");
        this.purpose = Objects.requireNonNull(purpose, "인증 목적은 필수입니다.");
        this.code = Objects.requireNonNull(code, "인증번호는 필수입니다.");
        this.status = VerificationStatus.REQUESTED;
        this.expiresAt = Objects.requireNonNull(expiresAt, "만료 시각은 필수입니다.");
        this.failedAttempts = 0;
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
                now.plus(CODE_VALIDITY)
        );
    }

    public boolean confirm(VerificationCode input, Instant now) {
        if (status != VerificationStatus.REQUESTED) {
            throw new BusinessException(VerificationErrorCode.ALREADY_VERIFIED);
        }

        if (!now.isBefore(expiresAt)) {
            throw new BusinessException(VerificationErrorCode.VERIFICATION_EXPIRED);
        }

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            throw new BusinessException(VerificationErrorCode.TOO_MANY_ATTEMPTS);
        }

        if (!code.equals(input)) {
            failedAttempts++;
            
            return false;
        }

        status = VerificationStatus.VERIFIED;
        expiresAt = now.plus(VERIFIED_VALIDITY);

        return true;
    }

    public void consume(PhoneNumber requestedPhoneNumber, VerificationPurpose requestedPurpose, Instant now) {
        if (status == VerificationStatus.CONSUMED) {
            throw new BusinessException(VerificationErrorCode.ALREADY_CONSUMED);
        }

        if (status != VerificationStatus.VERIFIED) {
            throw new BusinessException(VerificationErrorCode.NOT_VERIFIED);
        }

        if (!now.isBefore(expiresAt)) {
            throw new BusinessException(VerificationErrorCode.VERIFICATION_EXPIRED);
        }

        if (!phoneNumber.equals(requestedPhoneNumber) || purpose != requestedPurpose) {
            throw new BusinessException(VerificationErrorCode.VERIFICATION_MISMATCH);
        }

        status = VerificationStatus.CONSUMED;
    }
}
