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
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final String PATH = "/api/auth";

    private final RefreshTokenCookieProperties properties;

    public ResponseCookie createRefreshTokenCookie(RefreshToken refreshToken) {
        return createRefreshTokenCookie(refreshToken.value(), Duration.between(Instant.now(), refreshToken.expiresAt()));
    }

    public ResponseCookie expiredRefreshTokenCookie() {
        return createRefreshTokenCookie("", Duration.ZERO);
    }

    private ResponseCookie createRefreshTokenCookie(String value, Duration maxAge) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, value)
                .httpOnly(true)
                .secure(properties.secure())
                .sameSite("Strict")
                .path(PATH)
                .maxAge(maxAge)
                .build();
    }
}
