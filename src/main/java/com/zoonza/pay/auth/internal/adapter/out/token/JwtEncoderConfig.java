package com.zoonza.pay.auth.internal.adapter.out.token;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration(proxyBeanMethods = false)
public class JwtEncoderConfig {

    @Bean
    public JwtEncoder jwtEncoder(TokenProperties tokenProperties) {
        return NimbusJwtEncoder.withSecretKey(tokenProperties.accessToken().secretKey())
                .algorithm(MacAlgorithm.HS256)
                .build();
    }
}
