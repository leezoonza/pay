package com.zoonza.pay.auth.internal.application.dto;

import java.time.Instant;

public record RefreshToken(
        String value,
        Long customerId,
        Instant expiresAt
) {
}
