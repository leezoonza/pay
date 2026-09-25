package com.zoonza.pay.verification.internal.integration;

import com.zoonza.pay.TestcontainersConfiguration;
import com.zoonza.pay.verification.internal.adapter.in.dto.ConfirmVerificationRequest;
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
class VerificationConfirmIntegrationTests {
    private static final String VERIFICATION_URL = "/api/verifications";
    private static final String KEY_PREFIX = "verification:";

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    @DisplayName("발송된 인증번호로 확인하면 인증 완료 상태로 저장하고 만료 시간을 연장한다")
    void confirmsVerification() {
        String verificationId = requestVerification();
        String code = stored(verificationId).at("/code/value").asString();

        MvcTestResult result = confirm(verificationId, code);

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(stored(verificationId).get("status").asString()).isEqualTo("VERIFIED");
        assertThat(redisTemplate.getExpire(KEY_PREFIX + verificationId, TimeUnit.SECONDS))
                .isBetween(19L * 60, 20L * 60);
    }

    @Test
    @DisplayName("틀린 인증번호로 확인하면 400을 반환하고 실패 횟수를 저장한다")
    void rejectsMismatchedCode() {
        String verificationId = requestVerification();
        String code = stored(verificationId).at("/code/value").asString();
        String wrongCode = code.equals("000000") ? "111111" : "000000";

        MvcTestResult result = confirm(verificationId, wrongCode);

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VERIFICATION-003");
        assertThat(stored(verificationId).get("status").asString()).isEqualTo("REQUESTED");
        assertThat(stored(verificationId).get("failedAttempts").asInt()).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않는 인증으로 확인하면 404를 반환한다")
    void rejectsUnknownVerification() {
        MvcTestResult result = confirm("unknown", "123456");

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VERIFICATION-001");
    }

    private String requestVerification() {
        MvcTestResult result = mockMvc.post()
                .uri(VERIFICATION_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RequestVerificationRequest("010-1234-5678", VerificationPurpose.SIGNUP)
                ))
                .exchange();

        return objectMapper.readTree(result.getResponse().getContentAsByteArray())
                .get("verificationId")
                .asString();
    }

    private MvcTestResult confirm(String verificationId, String code) {
        return mockMvc.post()
                .uri(VERIFICATION_URL + "/{verificationId}/confirm", verificationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ConfirmVerificationRequest(code)))
                .exchange();
    }

    private JsonNode stored(String verificationId) {
        return objectMapper.readTree(redisTemplate.opsForValue().get(KEY_PREFIX + verificationId));
    }
}
