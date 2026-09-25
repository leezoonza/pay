package com.zoonza.pay.auth.internal.adapter.out.token;

import com.zoonza.pay.auth.internal.application.dto.AccessToken;
import com.zoonza.pay.auth.internal.fixture.TokenPropertiesFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAccessTokenIssuerTests {
    private final TokenProperties tokenProperties = TokenPropertiesFixture.tokenProperties();
    private final JwtAccessTokenIssuer issuer = new JwtAccessTokenIssuer(
            new JwtEncoderConfig().jwtEncoder(tokenProperties),
            tokenProperties
    );

    @Test
    @DisplayName("회원 ID를 subject로 담고 설정한 유효 시간만큼 유효한 HS256 JWT를 발급한다")
    void issuesSignedJwt() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        AccessToken accessToken = issuer.issue(1L, now);

        Jwt jwt = decoder().decode(accessToken.value());
        assertThat(jwt.getHeaders()).containsEntry("alg", "HS256");
        assertThat(jwt.getSubject()).isEqualTo("1");
        assertThat(jwt.getIssuedAt()).isEqualTo(now);
        assertThat(jwt.getExpiresAt()).isEqualTo(now.plus(TokenPropertiesFixture.ACCESS_TOKEN_TTL));
    }

    private JwtDecoder decoder() {
        SecretKeySpec secretKey = new SecretKeySpec(
                TokenPropertiesFixture.SECRET.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
