package com.zoonza.pay.verification.internal.domain;

import com.zoonza.pay.shared.domain.PhoneNumber;
import com.zoonza.pay.shared.error.BusinessException;
import com.zoonza.pay.verification.api.VerificationPurpose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerificationTests {
    private static final Instant REQUESTED_AT = Instant.parse("2026-09-25T00:00:00Z");
    private static final VerificationCode CODE = new VerificationCode("123456");
    private static final VerificationCode WRONG_CODE = new VerificationCode("654321");
    private static final PhoneNumber PHONE_NUMBER = new PhoneNumber("010-1234-5678");

    @Test
    @DisplayName("인증을 요청하면 요청 상태로 생성되고 5분 뒤 만료된다")
    void requestsVerification() {
        PhoneNumber phoneNumber = new PhoneNumber("010-1234-5678");

        Verification verification = Verification.request(phoneNumber, VerificationPurpose.SIGNUP, CODE, REQUESTED_AT);

        assertThat(verification.getId()).isNotBlank();
        assertThat(verification.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(verification.getPurpose()).isEqualTo(VerificationPurpose.SIGNUP);
        assertThat(verification.getCode()).isEqualTo(CODE);
        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.REQUESTED);
        assertThat(verification.getExpiresAt()).isEqualTo(Instant.parse("2026-09-25T00:05:00Z"));
        assertThat(verification.getFailedAttempts()).isZero();
    }

    @Test
    @DisplayName("인증을 요청할 때마다 서로 다른 인증 ID를 발급한다")
    void issuesUniqueIds() {
        assertThat(requested().getId()).isNotEqualTo(requested().getId());
    }

    @Test
    @DisplayName("인증번호가 일치하면 인증 완료 상태가 되고 확인 시점부터 10분 뒤 만료된다")
    void confirmsMatchingCode() {
        Verification verification = requested();
        Instant now = REQUESTED_AT.plusSeconds(60);

        boolean matched = verification.confirm(CODE, now);

        assertThat(matched).isTrue();
        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(verification.getExpiresAt()).isEqualTo(now.plus(Verification.VERIFIED_VALIDITY));
    }

    @Test
    @DisplayName("인증번호가 일치하지 않으면 요청 상태를 유지하고 실패 횟수가 늘어난다")
    void countsFailedAttemptOnMismatch() {
        Verification verification = requested();
        Instant expiresAt = verification.getExpiresAt();

        boolean matched = verification.confirm(WRONG_CODE, REQUESTED_AT.plusSeconds(60));

        assertThat(matched).isFalse();
        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.REQUESTED);
        assertThat(verification.getFailedAttempts()).isEqualTo(1);
        assertThat(verification.getExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("만료 시각이 지나면 인증번호를 확인할 수 없다")
    void rejectsExpiredVerification() {
        Verification verification = requested();

        assertThatThrownBy(() -> verification.confirm(CODE, verification.getExpiresAt()))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(VerificationErrorCode.VERIFICATION_EXPIRED));
    }

    @Test
    @DisplayName("인증번호를 5회 틀리면 올바른 인증번호로도 확인할 수 없다")
    void rejectsAfterTooManyFailedAttempts() {
        Verification verification = requested();
        for (int attempt = 0; attempt < Verification.MAX_FAILED_ATTEMPTS; attempt++) {
            verification.confirm(WRONG_CODE, REQUESTED_AT.plusSeconds(60));
        }

        assertThatThrownBy(() -> verification.confirm(CODE, REQUESTED_AT.plusSeconds(60)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(VerificationErrorCode.TOO_MANY_ATTEMPTS));
    }

    @Test
    @DisplayName("이미 완료된 인증은 다시 확인할 수 없다")
    void rejectsAlreadyVerified() {
        Verification verification = requested();
        verification.confirm(CODE, REQUESTED_AT.plusSeconds(60));

        assertThatThrownBy(() -> verification.confirm(CODE, REQUESTED_AT.plusSeconds(120)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(VerificationErrorCode.ALREADY_VERIFIED));
    }

    @Test
    @DisplayName("확인을 마친 인증을 같은 번호와 목적으로 사용하면 사용 완료 상태가 된다")
    void consumesVerifiedVerification() {
        Verification verification = verified();

        verification.consume(PHONE_NUMBER, VerificationPurpose.SIGNUP, REQUESTED_AT.plusSeconds(120));

        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.CONSUMED);
    }

    @Test
    @DisplayName("인증번호 확인을 마치지 않은 인증은 사용할 수 없다")
    void rejectsConsumingUnconfirmedVerification() {
        Verification verification = requested();

        assertConsumeFails(verification, PHONE_NUMBER, VerificationPurpose.SIGNUP,
                REQUESTED_AT.plusSeconds(60), VerificationErrorCode.NOT_VERIFIED);
    }

    @Test
    @DisplayName("이미 사용한 인증은 다시 사용할 수 없다")
    void rejectsConsumingTwice() {
        Verification verification = verified();
        verification.consume(PHONE_NUMBER, VerificationPurpose.SIGNUP, REQUESTED_AT.plusSeconds(120));

        assertConsumeFails(verification, PHONE_NUMBER, VerificationPurpose.SIGNUP,
                REQUESTED_AT.plusSeconds(180), VerificationErrorCode.ALREADY_CONSUMED);
    }

    @Test
    @DisplayName("확인 후 사용 가능 시간이 지나면 사용할 수 없다")
    void rejectsConsumingExpiredVerification() {
        Verification verification = verified();

        assertConsumeFails(verification, PHONE_NUMBER, VerificationPurpose.SIGNUP,
                verification.getExpiresAt(), VerificationErrorCode.VERIFICATION_EXPIRED);
    }

    @Test
    @DisplayName("인증받은 전화번호와 다른 번호로는 사용할 수 없다")
    void rejectsConsumingWithAnotherPhoneNumber() {
        Verification verification = verified();

        assertConsumeFails(verification, new PhoneNumber("010-9999-9999"), VerificationPurpose.SIGNUP,
                REQUESTED_AT.plusSeconds(120), VerificationErrorCode.VERIFICATION_MISMATCH);
    }

    @Test
    @DisplayName("인증 목적과 다른 목적으로는 사용할 수 없다")
    void rejectsConsumingForAnotherPurpose() {
        Verification verification = verified();

        assertConsumeFails(verification, PHONE_NUMBER, VerificationPurpose.LOGIN,
                REQUESTED_AT.plusSeconds(120), VerificationErrorCode.VERIFICATION_MISMATCH);
    }

    private void assertConsumeFails(
            Verification verification,
            PhoneNumber phoneNumber,
            VerificationPurpose purpose,
            Instant now,
            VerificationErrorCode expected
    ) {
        VerificationStatus statusBefore = verification.getStatus();

        assertThatThrownBy(() -> verification.consume(phoneNumber, purpose, now))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(expected));
        assertThat(verification.getStatus()).isEqualTo(statusBefore);
    }

    private Verification verified() {
        Verification verification = requested();
        verification.confirm(CODE, REQUESTED_AT.plusSeconds(60));
        return verification;
    }

    private Verification requested() {
        return Verification.request(PHONE_NUMBER, VerificationPurpose.SIGNUP, CODE, REQUESTED_AT);
    }
}
