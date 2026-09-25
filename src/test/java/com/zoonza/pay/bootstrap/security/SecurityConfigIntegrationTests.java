package com.zoonza.pay.bootstrap.security;

import com.zoonza.pay.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SecurityConfigIntegrationTests {
    private static final String PROTECTED_URL = "/api/protected";
    private static final String PUBLIC_URL = "/api/auth/logout";

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    @DisplayName("보호된 API에 토큰 없이 요청하면 401을 반환한다")
    void rejectsRequestWithoutToken() {
        MvcTestResult result = mockMvc.get()
                .uri(PROTECTED_URL)
                .exchange();

        assertUnauthorized(result);
    }

    @Test
    @DisplayName("보호된 API에 유효한 토큰으로 요청하면 인증을 통과한다")
    void authenticatesValidToken() {
        MvcTestResult result = mockMvc.get()
                .uri(PROTECTED_URL)
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenExpiringAt(Instant.now().plus(Duration.ofMinutes(15)))))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("보호된 API에 만료된 토큰으로 요청하면 401을 반환한다")
    void rejectsExpiredToken() {
        MvcTestResult result = mockMvc.get()
                .uri(PROTECTED_URL)
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenExpiringAt(Instant.now().minus(Duration.ofMinutes(1)))))
                .exchange();

        assertUnauthorized(result);
    }

    @Test
    @DisplayName("공개 API는 토큰 없이 요청할 수 있다")
    void permitsPublicEndpointWithoutToken() {
        MvcTestResult result = mockMvc.post()
                .uri(PUBLIC_URL)
                .exchange();

        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("공개 API는 만료된 토큰이 함께 와도 토큰을 검증하지 않고 처리한다")
    void ignoresExpiredTokenOnPublicEndpoint() {
        MvcTestResult result = mockMvc.post()
                .uri(PUBLIC_URL)
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenExpiringAt(Instant.now().minus(Duration.ofMinutes(1)))))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
    }

    private static void assertUnauthorized(MvcTestResult result) {
        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(result).hasContentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("COMMON-003");
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("인증이 필요합니다.");
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(401);
        assertThat(result).bodyJson().extractingPath("$.instance").isEqualTo(PROTECTED_URL);
    }

    private String tokenExpiringAt(Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("1")
                .issuedAt(expiresAt.minus(Duration.ofMinutes(15)))
                .expiresAt(expiresAt)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
