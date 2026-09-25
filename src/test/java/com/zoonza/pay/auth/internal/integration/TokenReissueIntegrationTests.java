package com.zoonza.pay.auth.internal.integration;

import com.zoonza.pay.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Import(TestcontainersConfiguration.class)
class TokenReissueIntegrationTests {
    private static final String PHONE_NUMBER = "010-1234-5678";
    private static final Pattern REFRESH_TOKEN_COOKIE = Pattern.compile("refresh_token=([^;]+);");

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.update("delete from customer");
    }

    @Test
    @DisplayName("리프레시 토큰으로 재발급하면 기존 토큰을 Redis에서 삭제하고 새 토큰을 저장한다")
    void reissuesTokens() throws Exception {
        String oldRefreshToken = refreshTokenOf(logIn());

        MvcTestResult result = reissue(oldRefreshToken);

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(result).bodyJson().extractingPath("$.accessToken").isNotNull();
        String newRefreshToken = refreshTokenOf(result);
        assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken);
        assertThat(redisTemplate.hasKey(keyOf(oldRefreshToken))).isFalse();
        assertThat(redisTemplate.hasKey(keyOf(newRefreshToken))).isTrue();
    }

    @Test
    @DisplayName("이미 재발급에 사용한 리프레시 토큰으로 다시 재발급하면 401을 반환한다")
    void rejectsReusedRefreshToken() {
        String oldRefreshToken = refreshTokenOf(logIn());
        reissue(oldRefreshToken);

        MvcTestResult result = reissue(oldRefreshToken);

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("AUTH-002");
    }

    private MvcTestResult logIn() {
        post("/api/customers/signup",
                Map.of("name", "김철수", "phoneNumber", PHONE_NUMBER, "verificationId", verify("SIGNUP")));
        MvcTestResult result = post("/api/auth/login",
                Map.of("phoneNumber", PHONE_NUMBER, "verificationId", verify("LOGIN")));
        assertThat(result).hasStatus(HttpStatus.OK);
        return result;
    }

    private String verify(String purpose) {
        MvcTestResult requested = post("/api/verifications", Map.of("phoneNumber", PHONE_NUMBER, "purpose", purpose));
        String verificationId = objectMapper.readTree(requested.getResponse().getContentAsByteArray())
                .get("verificationId")
                .asString();
        String code = objectMapper.readTree(redisTemplate.opsForValue().get("verification:" + verificationId))
                .at("/code/value")
                .asString();
        post("/api/verifications/" + verificationId + "/confirm", Map.of("code", code));
        return verificationId;
    }

    private MvcTestResult reissue(String refreshToken) {
        return mockMvc.post()
                .uri("/api/auth/reissue")
                .cookie(new Cookie("refresh_token", refreshToken))
                .exchange();
    }

    private MvcTestResult post(String uri, Object body) {
        return mockMvc.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .exchange();
    }

    private static String refreshTokenOf(MvcTestResult result) {
        Matcher matcher = REFRESH_TOKEN_COOKIE.matcher(result.getResponse().getHeader(HttpHeaders.SET_COOKIE));
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    private static String keyOf(String refreshToken) throws Exception {
        return "refresh_token:" + HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(refreshToken.getBytes(StandardCharsets.UTF_8)));
    }
}
