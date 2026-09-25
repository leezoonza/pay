package com.zoonza.pay.auth.internal.adapter.out.token;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
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
        public SecretKey secretKey() {
            return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        }
    }

    public record Refresh(
            Duration refreshTokenTtl
    ) {
    }
}
