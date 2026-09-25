package com.zoonza.pay.auth.internal.fixture;

import com.zoonza.pay.auth.internal.application.dto.RefreshToken;
import com.zoonza.pay.auth.internal.application.port.out.RefreshTokenIssuer;

import java.time.Duration;
import java.time.Instant;

public class FixedRefreshTokenIssuer implements RefreshTokenIssuer {
    public static final Duration TIME_TO_LIVE = Duration.ofDays(14);

    @Override
    public RefreshToken issue(Long customerId, Instant now) {
        return new RefreshToken("refresh-token-" + customerId, customerId, now.plus(TIME_TO_LIVE));
    }
}
