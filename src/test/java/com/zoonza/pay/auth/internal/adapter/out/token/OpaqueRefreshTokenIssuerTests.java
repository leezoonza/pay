package com.zoonza.pay.auth.internal.adapter.out.token;

import com.zoonza.pay.auth.internal.application.dto.RefreshToken;
import com.zoonza.pay.auth.internal.fixture.TokenPropertiesFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class OpaqueRefreshTokenIssuerTests {
    private final OpaqueRefreshTokenIssuer issuer = new OpaqueRefreshTokenIssuer(TokenPropertiesFixture.tokenProperties());

    @Test
    @DisplayName("리프레시 토큰을 발급하면 추측하기 어려운 값과 설정한 유효 시간만큼의 만료 시각을 가진다")
    void issuesRefreshToken() {
        Instant now = Instant.parse("2026-09-26T00:00:00Z");

        RefreshToken refreshToken = issuer.issue(1L, now);

        assertThat(refreshToken.value()).matches("[A-Za-z0-9_-]{43}");
        assertThat(refreshToken.customerId()).isEqualTo(1L);
        assertThat(refreshToken.expiresAt()).isEqualTo(now.plus(TokenPropertiesFixture.REFRESH_TOKEN_TTL));
    }

    @Test
    @DisplayName("리프레시 토큰을 발급할 때마다 서로 다른 값을 가진다")
    void issuesUniqueValues() {
        Instant now = Instant.now();

        assertThat(issuer.issue(1L, now).value()).isNotEqualTo(issuer.issue(1L, now).value());
    }
}
