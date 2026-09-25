package com.zoonza.pay.verification.internal.application.port.in;

import com.zoonza.pay.verification.internal.application.dto.ConfirmVerificationCommand;
import com.zoonza.pay.verification.internal.application.dto.RequestVerificationCommand;

public interface VerificationCommandUseCase {
    String request(RequestVerificationCommand command);

    void confirm(ConfirmVerificationCommand command);
}
