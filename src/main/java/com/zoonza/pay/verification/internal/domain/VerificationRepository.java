package com.zoonza.pay.verification.internal.domain;

import java.util.Optional;

public interface VerificationRepository {
    void save(Verification verification);

    Optional<Verification> findById(String id);
}
