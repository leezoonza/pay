package com.zoonza.pay.verification.internal.integration;

import com.zoonza.pay.TestcontainersConfiguration;
import com.zoonza.pay.verification.internal.adapter.in.dto.RequestVerificationRequest;
import com.zoonza.pay.verification.internal.domain.VerificationPurpose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class VerificationRequestIntegrationTests {
    private static final String VERIFICATION_URL = "/api/verifications";

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    @DisplayName("인증을 요청하면 만료 시간과 함께 Redis에 인증을 저장한다")
    void storesVerificationInRedis() {
        MvcTestResult result = mockMvc.post()
                .uri(VERIFICATION_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RequestVerificationRequest("010-1234-5678", VerificationPurpose.SIGNUP)
                ))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.CREATED);
        String verificationId = objectMapper.readTree(result.getResponse().getContentAsByteArray())
                .get("verificationId")
                .asString();
        String key = "verification:" + verificationId;

        JsonNode stored = objectMapper.readTree(redisTemplate.opsForValue().get(key));
        assertThat(stored.at("/phoneNumber/value").asString()).isEqualTo("010-1234-5678");
        assertThat(stored.get("purpose").asString()).isEqualTo("SIGNUP");
        assertThat(stored.get("status").asString()).isEqualTo("REQUESTED");
        assertThat(stored.at("/code/value").asString()).matches("\\d{6}");
        assertThat(redisTemplate.getExpire(key, TimeUnit.SECONDS)).isBetween(14L * 60, 15L * 60);
    }
}
