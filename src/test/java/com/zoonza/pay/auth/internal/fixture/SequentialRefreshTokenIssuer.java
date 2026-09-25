package com.zoonza.pay.auth.internal.fixture;

import com.zoonza.pay.auth.internal.application.dto.RefreshToken;
import com.zoonza.pay.auth.internal.application.port.out.RefreshTokenIssuer;

import java.time.Duration;
import java.time.Instant;

public class SequentialRefreshTokenIssuer implements RefreshTokenIssuer {
    public static final Duration TIME_TO_LIVE = Duration.ofDays(14);

    private int sequence;

    @Override
    public RefreshToken issue(Long customerId, Instant now) {
        sequence++;
        return new RefreshToken("refresh-token-" + customerId + "-" + sequence, customerId, now.plus(TIME_TO_LIVE));
    }
}
