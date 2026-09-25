package com.zoonza.pay.auth.internal.application.port.out;

import com.zoonza.pay.auth.internal.application.dto.RefreshToken;

import java.time.Instant;

public interface RefreshTokenIssuer {
    RefreshToken issue(Long customerId, Instant now);
}
