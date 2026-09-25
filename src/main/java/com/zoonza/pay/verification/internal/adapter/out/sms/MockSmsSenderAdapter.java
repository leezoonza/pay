package com.zoonza.pay.verification.internal.adapter.out.sms;

import com.zoonza.pay.shared.domain.PhoneNumber;
import com.zoonza.pay.verification.internal.application.port.out.SmsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockSmsSenderAdapter implements SmsSender {

    @Override
    public void send(PhoneNumber phoneNumber, String message) {
        log.info("[Mock SMS] to={}, message={}", phoneNumber.value(), message);
    }
}
