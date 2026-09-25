package com.zoonza.pay.auth.internal.adapter.in.support;

import com.zoonza.pay.auth.internal.application.dto.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class TokenCookieManager {
    static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final String PATH = "/api/auth";

    private final RefreshTokenCookieProperties properties;

    public ResponseCookie refreshTokenCookie(RefreshToken refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken.value())
                .httpOnly(true)
                .secure(properties.secure())
                .sameSite("Strict")
                .path(PATH)
                .maxAge(Duration.between(Instant.now(), refreshToken.expiresAt()))
                .build();
    }
}
