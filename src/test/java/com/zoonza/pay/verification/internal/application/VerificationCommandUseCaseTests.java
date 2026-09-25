package com.zoonza.pay.verification.internal.application;

import com.zoonza.pay.shared.domain.PhoneNumber;
import com.zoonza.pay.verification.internal.application.dto.RequestVerificationCommand;
import com.zoonza.pay.verification.internal.application.port.in.VerificationCommandUseCase;
import com.zoonza.pay.verification.internal.application.service.VerificationCommandService;
import com.zoonza.pay.verification.internal.domain.Verification;
import com.zoonza.pay.verification.internal.domain.VerificationPurpose;
import com.zoonza.pay.verification.internal.domain.VerificationStatus;
import com.zoonza.pay.verification.internal.fixture.InMemoryVerificationRepository;
import com.zoonza.pay.verification.internal.fixture.RecordingSmsSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificationCommandUseCaseTests {
    private final InMemoryVerificationRepository verificationRepository = new InMemoryVerificationRepository();
    private final RecordingSmsSender smsSender = new RecordingSmsSender();
    private final VerificationCommandUseCase useCase = new VerificationCommandService(verificationRepository, smsSender);

    @Test
    @DisplayName("인증을 요청하면 인증을 저장하고 인증번호를 문자로 발송한 뒤 인증 ID를 반환한다")
    void requestsVerification() {
        PhoneNumber phoneNumber = new PhoneNumber("010-1234-5678");

        String verificationId = useCase.request(
                new RequestVerificationCommand(phoneNumber, VerificationPurpose.SIGNUP)
        );

        assertThat(verificationRepository.savedVerifications()).singleElement()
                .satisfies(verification -> {
                    assertThat(verification.id()).isEqualTo(verificationId);
                    assertThat(verification.phoneNumber()).isEqualTo(phoneNumber);
                    assertThat(verification.purpose()).isEqualTo(VerificationPurpose.SIGNUP);
                    assertThat(verification.status()).isEqualTo(VerificationStatus.REQUESTED);
                });

        Verification saved = verificationRepository.savedVerifications().getFirst();
        assertThat(smsSender.sentMessages()).singleElement()
                .satisfies(sms -> {
                    assertThat(sms.phoneNumber()).isEqualTo(phoneNumber);
                    assertThat(sms.message()).contains(saved.code().value());
                });
    }
}
