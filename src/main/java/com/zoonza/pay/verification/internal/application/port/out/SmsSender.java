package com.zoonza.pay.verification.internal.application.port.out;

import com.zoonza.pay.shared.domain.PhoneNumber;

public interface SmsSender {
    void send(PhoneNumber phoneNumber, String message);
}
