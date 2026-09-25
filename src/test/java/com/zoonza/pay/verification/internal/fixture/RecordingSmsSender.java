package com.zoonza.pay.verification.internal.fixture;

import com.zoonza.pay.shared.domain.PhoneNumber;
import com.zoonza.pay.verification.internal.application.port.out.SmsSender;

import java.util.ArrayList;
import java.util.List;

public class RecordingSmsSender implements SmsSender {
    private final List<SentSms> sentMessages = new ArrayList<>();

    @Override
    public void send(PhoneNumber phoneNumber, String message) {
        sentMessages.add(new SentSms(phoneNumber, message));
    }

    public List<SentSms> sentMessages() {
        return List.copyOf(sentMessages);
    }

    public record SentSms(PhoneNumber phoneNumber, String message) {
    }
}
