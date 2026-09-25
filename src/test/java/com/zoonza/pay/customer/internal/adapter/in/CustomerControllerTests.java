package com.zoonza.pay.customer.internal.adapter.in;

import com.zoonza.pay.customer.internal.adapter.in.dto.SignupRequest;
import com.zoonza.pay.customer.internal.application.dto.RegisterCustomerCommand;
import com.zoonza.pay.customer.internal.application.port.in.CustomerCommandUseCase;
import com.zoonza.pay.customer.internal.domain.CustomerErrorCode;
import com.zoonza.pay.customer.internal.domain.Name;
import com.zoonza.pay.shared.domain.PhoneNumber;
import com.zoonza.pay.shared.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerControllerTests {
    private static final String SIGNUP_URL = "/api/customers/signup";

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerCommandUseCase customerCommandUseCase;

    @Test
    @DisplayName("올바른 요청으로 가입하면 201을 반환하고 가입 유즈케이스를 호출한다")
    void signsUpCustomer() {
        MvcTestResult result = signup(new SignupRequest("김철수", "010-1234-5678"));

        assertThat(result).hasStatus(HttpStatus.CREATED);
        verify(customerCommandUseCase).register(new RegisterCustomerCommand(
                new Name("김철수"),
                new PhoneNumber("010-1234-5678")
        ));
    }

    @Test
    @DisplayName("요청 값 형식이 올바르지 않으면 400과 검증 메시지를 반환하고 유즈케이스를 호출하지 않는다")
    void rejectsInvalidRequest() {
        MvcTestResult result = signup(new SignupRequest("김철수", "01012345678"));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("COMMON-001");
        assertThat(result).bodyJson().extractingPath("$.detail")
                .isEqualTo("전화번호는 010-XXXX-XXXX 형식이어야 합니다.");
        verify(customerCommandUseCase, never()).register(any());
    }

    @Test
    @DisplayName("필수 값이 없으면 400과 검증 메시지를 반환하고 유즈케이스를 호출하지 않는다")
    void rejectsMissingField() {
        MvcTestResult result = signup(new SignupRequest(null, "010-1234-5678"));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("COMMON-001");
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("이름은 필수입니다.");
        verify(customerCommandUseCase, never()).register(any());
    }

    @Test
    @DisplayName("이미 가입된 전화번호면 409와 오류 코드를 반환한다")
    void rejectsAlreadyRegisteredPhoneNumber() {
        willThrow(new BusinessException(CustomerErrorCode.ALREADY_REGISTERED))
                .given(customerCommandUseCase).register(any());

        MvcTestResult result = signup(new SignupRequest("김철수", "010-1234-5678"));

        assertThat(result).hasStatus(HttpStatus.CONFLICT);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("CUSTOMER-001");
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("이미 가입된 회원입니다.");
    }

    private MvcTestResult signup(SignupRequest request) {
        return mockMvc.post()
                .uri(SIGNUP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();
    }
}
