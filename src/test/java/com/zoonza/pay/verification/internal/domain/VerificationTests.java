package com.zoonza.pay.verification.internal.domain;

import com.zoonza.pay.shared.domain.PhoneNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class VerificationTests {

    @Test
    @DisplayName("인증을 요청하면 요청 상태로 생성되고 5분 뒤 만료된다")
    void requestsVerification() {
        PhoneNumber phoneNumber = new PhoneNumber("010-1234-5678");
        VerificationCode code = new VerificationCode("123456");
        Instant now = Instant.parse("2026-09-25T00:00:00Z");

        Verification verification = Verification.request(phoneNumber, VerificationPurpose.SIGNUP, code, now);

        assertThat(verification.id()).isNotBlank();
        assertThat(verification.phoneNumber()).isEqualTo(phoneNumber);
        assertThat(verification.purpose()).isEqualTo(VerificationPurpose.SIGNUP);
        assertThat(verification.code()).isEqualTo(code);
        assertThat(verification.status()).isEqualTo(VerificationStatus.REQUESTED);
        assertThat(verification.expiresAt()).isEqualTo(Instant.parse("2026-09-25T00:05:00Z"));
    }

    @Test
    @DisplayName("인증을 요청할 때마다 서로 다른 인증 ID를 발급한다")
    void issuesUniqueIds() {
        PhoneNumber phoneNumber = new PhoneNumber("010-1234-5678");
        VerificationCode code = new VerificationCode("123456");
        Instant now = Instant.now();

        Verification first = Verification.request(phoneNumber, VerificationPurpose.SIGNUP, code, now);
        Verification second = Verification.request(phoneNumber, VerificationPurpose.SIGNUP, code, now);

        assertThat(first.id()).isNotEqualTo(second.id());
    }
}
