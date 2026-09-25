package com.zoonza.pay.verification.internal.fixture;

import com.zoonza.pay.verification.internal.domain.Verification;
import com.zoonza.pay.verification.internal.domain.VerificationRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryVerificationRepository implements VerificationRepository {
    private final Map<String, Verification> verifications = new LinkedHashMap<>();

    @Override
    public void save(Verification verification) {
        verifications.put(verification.getId(), verification);
    }

    @Override
    public Optional<Verification> findById(String id) {
        return Optional.ofNullable(verifications.get(id));
    }

    public List<Verification> savedVerifications() {
        return List.copyOf(verifications.values());
    }
}
