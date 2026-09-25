package com.zoonza.pay.auth.internal.adapter.out.token;

import com.zoonza.pay.auth.internal.application.dto.RefreshToken;
import com.zoonza.pay.auth.internal.application.port.out.RefreshTokenIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class OpaqueRefreshTokenIssuer implements RefreshTokenIssuer {
    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TokenProperties tokenProperties;

    @Override
    public RefreshToken issue(Long customerId, Instant now) {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);

        return new RefreshToken(
                Base64.getUrlEncoder().withoutPadding().encodeToString(bytes),
                customerId,
                now.plus(tokenProperties.refreshToken().refreshTokenTtl())
        );
    }
}
