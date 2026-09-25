package com.zoonza.pay.auth.internal.adapter.out.token;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("auth.token")
public record TokenProperties(
        Access accessToken,
        Refresh refreshToken
) {
    public record Access(
            String secret,
            Duration accessTokenTtl
    ) {
    }

    public record Refresh(
            Duration refreshTokenTtl
    ) {
    }
}
