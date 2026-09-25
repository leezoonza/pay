package com.zoonza.pay.auth.internal.fixture;

import com.zoonza.pay.auth.internal.adapter.out.token.TokenProperties;

import java.time.Duration;

public final class TokenPropertiesFixture {
    public static final String SECRET = "test-access-token-secret-must-be-at-least-32-bytes";
    public static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);
    public static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(14);

    private TokenPropertiesFixture() {
    }

    public static TokenProperties tokenProperties() {
        return new TokenProperties(
                new TokenProperties.Access(SECRET, ACCESS_TOKEN_TTL),
                new TokenProperties.Refresh(REFRESH_TOKEN_TTL)
        );
    }
}
