package com.zoonza.pay.verification.internal.fixture;

import com.zoonza.pay.verification.internal.domain.Verification;
import com.zoonza.pay.verification.internal.domain.VerificationRepository;

import java.util.ArrayList;
import java.util.List;

public class InMemoryVerificationRepository implements VerificationRepository {
    private final List<Verification> verifications = new ArrayList<>();

    @Override
    public void save(Verification verification) {
        verifications.add(verification);
    }

    public List<Verification> savedVerifications() {
        return List.copyOf(verifications);
    }
}
