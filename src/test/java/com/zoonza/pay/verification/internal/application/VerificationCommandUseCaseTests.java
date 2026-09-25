package com.zoonza.pay.verification.internal.application;

import com.zoonza.pay.shared.domain.PhoneNumber;
import com.zoonza.pay.shared.error.BusinessException;
import com.zoonza.pay.verification.api.VerificationApi;
import com.zoonza.pay.verification.api.VerificationPurpose;
import com.zoonza.pay.verification.internal.application.dto.ConfirmVerificationCommand;
import com.zoonza.pay.verification.internal.application.dto.RequestVerificationCommand;
import com.zoonza.pay.verification.internal.application.port.in.VerificationCommandUseCase;
import com.zoonza.pay.verification.internal.application.service.VerificationCommandService;
import com.zoonza.pay.verification.internal.domain.Verification;
import com.zoonza.pay.verification.internal.domain.VerificationCode;
import com.zoonza.pay.verification.internal.domain.VerificationErrorCode;
import com.zoonza.pay.verification.internal.domain.VerificationStatus;
import com.zoonza.pay.verification.internal.fixture.InMemoryVerificationRepository;
import com.zoonza.pay.verification.internal.fixture.RecordingSmsSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerificationCommandUseCaseTests {
    private final InMemoryVerificationRepository verificationRepository = new InMemoryVerificationRepository();
    private final RecordingSmsSender smsSender = new RecordingSmsSender();
    private final VerificationCommandService service = new VerificationCommandService(verificationRepository, smsSender);
    private final VerificationCommandUseCase useCase = service;
    private final VerificationApi verificationApi = service;

    @Test
    @DisplayName("인증을 요청하면 인증을 저장하고 인증번호를 문자로 발송한 뒤 인증 ID를 반환한다")
    void requestsVerification() {
        PhoneNumber phoneNumber = new PhoneNumber("010-1234-5678");

        String verificationId = useCase.request(
                new RequestVerificationCommand(phoneNumber, VerificationPurpose.SIGNUP)
        );

        assertThat(verificationRepository.savedVerifications()).singleElement()
                .satisfies(verification -> {
                    assertThat(verification.getId()).isEqualTo(verificationId);
                    assertThat(verification.getPhoneNumber()).isEqualTo(phoneNumber);
                    assertThat(verification.getPurpose()).isEqualTo(VerificationPurpose.SIGNUP);
                    assertThat(verification.getStatus()).isEqualTo(VerificationStatus.REQUESTED);
                });

        Verification saved = verificationRepository.savedVerifications().getFirst();
        assertThat(smsSender.sentMessages()).singleElement()
                .satisfies(sms -> {
                    assertThat(sms.phoneNumber()).isEqualTo(phoneNumber);
                    assertThat(sms.message()).contains(saved.getCode().value());
                });
    }

    @Test
    @DisplayName("인증번호가 일치하면 인증 완료 상태로 저장한다")
    void confirmsVerification() {
        Verification requested = saveRequested();

        useCase.confirm(new ConfirmVerificationCommand(requested.getId(), requested.getCode()));

        assertThat(verificationRepository.findById(requested.getId())).get()
                .extracting(Verification::getStatus)
                .isEqualTo(VerificationStatus.VERIFIED);
    }

    @Test
    @DisplayName("인증번호가 일치하지 않으면 실패 횟수를 저장하고 오류를 반환한다")
    void savesFailedAttemptAndRejectsMismatch() {
        Verification requested = saveRequested();

        assertThatThrownBy(() -> useCase.confirm(new ConfirmVerificationCommand(requested.getId(), new VerificationCode("000000"))))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(VerificationErrorCode.CODE_MISMATCH));
        assertThat(verificationRepository.findById(requested.getId())).get()
                .satisfies(verification -> {
                    assertThat(verification.getStatus()).isEqualTo(VerificationStatus.REQUESTED);
                    assertThat(verification.getFailedAttempts()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("인증 정보가 없으면 오류를 반환한다")
    void rejectsUnknownVerification() {
        assertThatThrownBy(() -> useCase.confirm(new ConfirmVerificationCommand("unknown", new VerificationCode("123456"))))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(VerificationErrorCode.VERIFICATION_NOT_FOUND));
    }

    @Test
    @DisplayName("확인을 마친 인증을 사용하면 사용 완료 상태로 저장한다")
    void consumesVerification() {
        Verification requested = saveRequested();
        useCase.confirm(new ConfirmVerificationCommand(requested.getId(), requested.getCode()));

        verificationApi.consume(requested.getId(), requested.getPhoneNumber(), VerificationPurpose.SIGNUP);

        assertThat(verificationRepository.findById(requested.getId())).get()
                .extracting(Verification::getStatus)
                .isEqualTo(VerificationStatus.CONSUMED);
    }

    @Test
    @DisplayName("사용할 인증 정보가 없으면 오류를 반환한다")
    void rejectsConsumingUnknownVerification() {
        assertThatThrownBy(() -> verificationApi.consume(
                "unknown", new PhoneNumber("010-1234-5678"), VerificationPurpose.SIGNUP
        ))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(VerificationErrorCode.VERIFICATION_NOT_FOUND));
    }

    private Verification saveRequested() {
        Verification verification = Verification.request(
                new PhoneNumber("010-1234-5678"),
                VerificationPurpose.SIGNUP,
                new VerificationCode("123456"),
                Instant.now()
        );
        verificationRepository.save(verification);
        return verification;
    }
}
