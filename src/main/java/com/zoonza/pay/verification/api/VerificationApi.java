package com.zoonza.pay.verification.api;

import com.zoonza.pay.shared.domain.PhoneNumber;

public interface VerificationApi {
    void consume(String verificationId, PhoneNumber phoneNumber, VerificationPurpose purpose);
}
