package com.zoonza.pay.auth.internal.adapter.out.token;

import com.zoonza.pay.auth.internal.application.dto.AccessToken;
import com.zoonza.pay.auth.internal.application.port.out.AccessTokenIssuer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAccessTokenIssuer implements AccessTokenIssuer {
    private final JwtEncoder jwtEncoder;
    private final TokenProperties tokenProperties;

    @Override
    public AccessToken issue(Long customerId, Instant now) {
        Instant expiresAt = now.plus(tokenProperties.accessToken().accessTokenTtl());

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .subject(customerId.toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .build();

        String value = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new AccessToken(value);
    }
}
