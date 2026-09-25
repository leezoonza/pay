package com.zoonza.pay.auth.internal.integration;

import com.zoonza.pay.TestcontainersConfiguration;
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
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Import(TestcontainersConfiguration.class)
class LoginIntegrationTests {
    private static final String PHONE_NUMBER = "010-1234-5678";
    private static final String ACCESS_TOKEN_SECRET = "test-access-token-secret-must-be-at-least-32-bytes";
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
    @DisplayName("가입한 회원이 로그인 인증을 마치고 로그인하면 액세스 토큰과 리프레시 토큰을 발급한다")
    void logsIn() throws Exception {
        signUp(PHONE_NUMBER);
        Long customerId = jdbcTemplate.queryForObject(
                "select id from customer where phone_number = ?", Long.class, PHONE_NUMBER);
        String verificationId = verify(PHONE_NUMBER, "LOGIN");

        MvcTestResult result = login(PHONE_NUMBER, verificationId);

        assertThat(result).hasStatus(HttpStatus.OK);
        String accessToken = objectMapper.readTree(result.getResponse().getContentAsByteArray())
                .get("accessToken")
                .asString();
        Jwt jwt = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(
                        ACCESS_TOKEN_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build()
                .decode(accessToken);
        assertThat(jwt.getSubject()).isEqualTo(customerId.toString());

        Matcher cookie = REFRESH_TOKEN_COOKIE.matcher(result.getResponse().getHeader(HttpHeaders.SET_COOKIE));
        assertThat(cookie.find()).isTrue();
        String key = "refresh_token:" + sha256(cookie.group(1));
        assertThat(redisTemplate.opsForValue().get(key)).isEqualTo(customerId.toString());
        assertThat(redisTemplate.getExpire(key, TimeUnit.DAYS)).isBetween(13L, 14L);
    }

    @Test
    @DisplayName("가입 목적으로 받은 인증으로 로그인하면 400을 반환한다")
    void rejectsVerificationOfAnotherPurpose() {
        signUp(PHONE_NUMBER);
        String verificationId = verify(PHONE_NUMBER, "SIGNUP");

        MvcTestResult result = login(PHONE_NUMBER, verificationId);

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VERIFICATION-008");
        assertThat(result.getResponse().getHeader(HttpHeaders.SET_COOKIE)).isNull();
    }

    @Test
    @DisplayName("가입되지 않은 전화번호로 로그인하면 404를 반환한다")
    void rejectsUnregisteredCustomer() {
        String verificationId = verify(PHONE_NUMBER, "LOGIN");

        MvcTestResult result = login(PHONE_NUMBER, verificationId);

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("AUTH-001");
    }

    private void signUp(String phoneNumber) {
        String verificationId = verify(phoneNumber, "SIGNUP");
        MvcTestResult result = post("/api/customers/signup",
                Map.of("name", "김철수", "phoneNumber", phoneNumber, "verificationId", verificationId));
        assertThat(result).hasStatus(HttpStatus.CREATED);
    }

    private String verify(String phoneNumber, String purpose) {
        MvcTestResult requested = post("/api/verifications", Map.of("phoneNumber", phoneNumber, "purpose", purpose));
        String verificationId = objectMapper.readTree(requested.getResponse().getContentAsByteArray())
                .get("verificationId")
                .asString();
        String code = objectMapper.readTree(redisTemplate.opsForValue().get("verification:" + verificationId))
                .at("/code/value")
                .asString();

        MvcTestResult confirmed = post("/api/verifications/" + verificationId + "/confirm", Map.of("code", code));
        assertThat(confirmed).hasStatus(HttpStatus.OK);

        return verificationId;
    }

    private MvcTestResult login(String phoneNumber, String verificationId) {
        return post("/api/auth/login", Map.of("phoneNumber", phoneNumber, "verificationId", verificationId));
    }

    private MvcTestResult post(String uri, Object body) {
        return mockMvc.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .exchange();
    }

    private static String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
