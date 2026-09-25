package com.zoonza.pay.verification.internal.application.service;

import com.zoonza.pay.shared.error.BusinessException;
import com.zoonza.pay.verification.internal.application.dto.ConfirmVerificationCommand;
import com.zoonza.pay.verification.internal.application.dto.RequestVerificationCommand;
import com.zoonza.pay.verification.internal.application.port.in.VerificationCommandUseCase;
import com.zoonza.pay.verification.internal.application.port.out.SmsSender;
import com.zoonza.pay.verification.internal.domain.Verification;
import com.zoonza.pay.verification.internal.domain.VerificationCode;
import com.zoonza.pay.verification.internal.domain.VerificationErrorCode;
import com.zoonza.pay.verification.internal.domain.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class VerificationCommandService implements VerificationCommandUseCase {
    private final VerificationRepository verificationRepository;
    private final SmsSender smsSender;

    @Override
    public String request(RequestVerificationCommand command) {
        Verification verification = Verification.request(
                command.phoneNumber(),
                command.purpose(),
                VerificationCode.generate(),
                Instant.now()
        );

        verificationRepository.save(verification);
        smsSender.send(
                verification.getPhoneNumber(),
                "[Pay] 인증번호 [%s]를 입력해 주세요.".formatted(verification.getCode().value())
        );

        return verification.getId();
    }

    @Override
    public void confirm(ConfirmVerificationCommand command) {
        Verification verification = verificationRepository.findById(command.verificationId())
                .orElseThrow(() -> new BusinessException(VerificationErrorCode.VERIFICATION_NOT_FOUND));

        boolean matched = verification.confirm(command.code(), Instant.now());
        verificationRepository.save(verification);

        if (!matched) {
            throw new BusinessException(VerificationErrorCode.CODE_MISMATCH);
        }
    }
}
