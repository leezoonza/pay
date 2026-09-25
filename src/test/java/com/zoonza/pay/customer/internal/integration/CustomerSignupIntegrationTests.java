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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Import(TestcontainersConfiguration.class)
class CustomerSignupIntegrationTests {
    private static final String SIGNUP_URL = "/api/customers/signup";

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerJpaRepository customerJpaRepository;

    @BeforeEach
    void cleanUp() {
        customerJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("회원 가입 요청이 성공하면 회원이 저장된다")
    void signsUpCustomer() {
        MvcTestResult result = signup(new SignupRequest("김철수", "010-1234-5678"));

        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(customerJpaRepository.findAll())
                .singleElement()
                .satisfies(customer -> {
                    assertThat(customer.getId()).isNotNull();
                    assertThat(customer.getName()).isEqualTo(new Name("김철수"));
                    assertThat(customer.getPhoneNumber()).isEqualTo(new PhoneNumber("010-1234-5678"));
                    assertThat(customer.getCreatedAt()).isNotNull();
                    assertThat(customer.getUpdatedAt()).isNotNull();
                });
    }

    @Test
    @DisplayName("이미 가입된 전화번호로 가입하면 409를 반환하고 회원을 추가로 저장하지 않는다")
    void rejectsAlreadyRegisteredPhoneNumber() {
        customerJpaRepository.save(Customer.register(new Name("김철수"), new PhoneNumber("010-1234-5678")));

        MvcTestResult result = signup(new SignupRequest("이영희", "010-1234-5678"));

        assertThat(result).hasStatus(HttpStatus.CONFLICT);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("CUSTOMER-001");
        assertThat(customerJpaRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("요청 값이 올바르지 않으면 400을 반환하고 회원을 저장하지 않는다")
    void rejectsInvalidRequest() {
        MvcTestResult result = signup(new SignupRequest("김철수1", "010-1234-5678"));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("이름에는 문자만 사용할 수 있습니다.");
        assertThat(customerJpaRepository.count()).isZero();
    }

    private MvcTestResult signup(SignupRequest request) {
        return mockMvc.post()
                .uri(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();
    }
}
