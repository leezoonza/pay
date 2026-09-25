package com.zoonza.pay.customer.internal.integration;

import com.zoonza.pay.TestcontainersConfiguration;
import com.zoonza.pay.customer.internal.adapter.in.dto.SignupRequest;
import com.zoonza.pay.customer.internal.adapter.out.persistence.CustomerJpaRepository;
import com.zoonza.pay.customer.internal.domain.Customer;
import com.zoonza.pay.customer.internal.domain.Name;
import com.zoonza.pay.shared.domain.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
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
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Import(TestcontainersConfiguration.class)
class CustomerSignupIntegrationTests {
    private static final String SIGNUP_URL = "/api/customers/signup";
    private static final String VERIFICATION_URL = "/api/verifications";
    private static final String PHONE_NUMBER = "010-1234-5678";

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerJpaRepository customerJpaRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void cleanUp() {
        customerJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("본인인증을 마친 뒤 가입하면 회원이 저장된다")
    void signsUpCustomer() {
        String verificationId = verify(PHONE_NUMBER);

        MvcTestResult result = signup(new SignupRequest("김철수", PHONE_NUMBER, verificationId));

        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(customerJpaRepository.findAll())
                .singleElement()
                .satisfies(customer -> {
                    assertThat(customer.getId()).isNotNull();
                    assertThat(customer.getName()).isEqualTo(new Name("김철수"));
                    assertThat(customer.getPhoneNumber()).isEqualTo(new PhoneNumber(PHONE_NUMBER));
                    assertThat(customer.getCreatedAt()).isNotNull();
                    assertThat(customer.getUpdatedAt()).isNotNull();
                });
    }

    @Test
    @DisplayName("인증번호 확인을 마치지 않았으면 400을 반환하고 회원을 저장하지 않는다")
    void rejectsUnconfirmedVerification() {
        String verificationId = requestVerification(PHONE_NUMBER);

        MvcTestResult result = signup(new SignupRequest("김철수", PHONE_NUMBER, verificationId));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VERIFICATION-006");
        assertThat(customerJpaRepository.count()).isZero();
    }

    @Test
    @DisplayName("다른 전화번호로 받은 인증으로 가입하면 400을 반환하고 회원을 저장하지 않는다")
    void rejectsVerificationOfAnotherPhoneNumber() {
        String verificationId = verify("010-9999-9999");

        MvcTestResult result = signup(new SignupRequest("김철수", PHONE_NUMBER, verificationId));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VERIFICATION-008");
        assertThat(customerJpaRepository.count()).isZero();
    }

    @Test
    @DisplayName("이미 사용한 인증으로 다시 가입하면 409를 반환한다")
    void rejectsConsumedVerification() {
        String verificationId = verify(PHONE_NUMBER);
        signup(new SignupRequest("김철수", PHONE_NUMBER, verificationId));
        customerJpaRepository.deleteAllInBatch();

        MvcTestResult result = signup(new SignupRequest("김철수", PHONE_NUMBER, verificationId));

        assertThat(result).hasStatus(HttpStatus.CONFLICT);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VERIFICATION-007");
        assertThat(customerJpaRepository.count()).isZero();
    }

    @Test
    @DisplayName("이미 가입된 전화번호로 가입하면 409를 반환하고 회원을 추가로 저장하지 않는다")
    void rejectsAlreadyRegisteredPhoneNumber() {
        customerJpaRepository.save(Customer.register(new Name("김철수"), new PhoneNumber(PHONE_NUMBER)));
        String verificationId = verify(PHONE_NUMBER);

        MvcTestResult result = signup(new SignupRequest("이영희", PHONE_NUMBER, verificationId));

        assertThat(result).hasStatus(HttpStatus.CONFLICT);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("CUSTOMER-001");
        assertThat(customerJpaRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("요청 값이 올바르지 않으면 400을 반환하고 회원을 저장하지 않는다")
    void rejectsInvalidRequest() {
        MvcTestResult result = signup(new SignupRequest("김철수1", PHONE_NUMBER, "verification-id"));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("이름에는 문자만 사용할 수 있습니다.");
        assertThat(customerJpaRepository.count()).isZero();
    }

    private String verify(String phoneNumber) {
        String verificationId = requestVerification(phoneNumber);
        String code = objectMapper.readTree(redisTemplate.opsForValue().get("verification:" + verificationId))
                .at("/code/value")
                .asString();

        MvcTestResult result = post(VERIFICATION_URL + "/" + verificationId + "/confirm", Map.of("code", code));
        assertThat(result).hasStatus(HttpStatus.OK);

        return verificationId;
    }

    private String requestVerification(String phoneNumber) {
        MvcTestResult result = post(VERIFICATION_URL, Map.of("phoneNumber", phoneNumber, "purpose", "SIGNUP"));

        return objectMapper.readTree(result.getResponse().getContentAsByteArray())
                .get("verificationId")
                .asString();
    }

    private MvcTestResult signup(SignupRequest request) {
        return post(SIGNUP_URL, request);
    }

    private MvcTestResult post(String uri, Object body) {
        return mockMvc.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .exchange();
    }
}
