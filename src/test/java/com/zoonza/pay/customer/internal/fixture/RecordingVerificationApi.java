package com.zoonza.pay.customer.internal.fixture;

import com.zoonza.pay.shared.domain.PhoneNumber;
import com.zoonza.pay.shared.error.BusinessException;
import com.zoonza.pay.verification.api.VerificationApi;
import com.zoonza.pay.verification.api.VerificationPurpose;

import java.util.ArrayList;
import java.util.List;

public class RecordingVerificationApi implements VerificationApi {
    private final List<ConsumedVerification> consumedVerifications = new ArrayList<>();
    private BusinessException failure;

    @Override
    public void consume(String verificationId, PhoneNumber phoneNumber, VerificationPurpose purpose) {
        if (failure != null) {
            throw failure;
        }

        consumedVerifications.add(new ConsumedVerification(verificationId, phoneNumber, purpose));
    }

    public void failWith(BusinessException failure) {
        this.failure = failure;
    }

    public List<ConsumedVerification> consumedVerifications() {
        return List.copyOf(consumedVerifications);
    }

    public record ConsumedVerification(String verificationId, PhoneNumber phoneNumber, VerificationPurpose purpose) {
    }
}
