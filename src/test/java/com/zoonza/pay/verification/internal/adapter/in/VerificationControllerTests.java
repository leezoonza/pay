package com.zoonza.pay.verification.internal.adapter.in;

import com.zoonza.pay.shared.domain.PhoneNumber;
import com.zoonza.pay.shared.error.BusinessException;
import com.zoonza.pay.verification.api.VerificationPurpose;
import com.zoonza.pay.verification.internal.adapter.in.dto.ConfirmVerificationRequest;
import com.zoonza.pay.verification.internal.adapter.in.dto.RequestVerificationRequest;
import com.zoonza.pay.verification.internal.application.dto.ConfirmVerificationCommand;
import com.zoonza.pay.verification.internal.application.dto.RequestVerificationCommand;
import com.zoonza.pay.verification.internal.application.port.in.VerificationCommandUseCase;
import com.zoonza.pay.verification.internal.domain.VerificationCode;
import com.zoonza.pay.verification.internal.domain.VerificationErrorCode;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@WebMvcTest(VerificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class VerificationControllerTests {
    private static final String VERIFICATION_URL = "/api/verifications";

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VerificationCommandUseCase verificationCommandUseCase;

    @Test
    @DisplayName("올바른 요청으로 인증을 요청하면 201과 인증 ID를 반환한다")
    void requestsVerification() {
        given(verificationCommandUseCase.request(any())).willReturn("verification-id");

        MvcTestResult result = request(new RequestVerificationRequest("010-1234-5678", VerificationPurpose.SIGNUP));

        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result).bodyJson().extractingPath("$.verificationId").isEqualTo("verification-id");
        verify(verificationCommandUseCase).request(new RequestVerificationCommand(
                new PhoneNumber("010-1234-5678"),
                VerificationPurpose.SIGNUP
        ));
    }

    @Test
    @DisplayName("전화번호 형식이 올바르지 않으면 400과 검증 메시지를 반환하고 유즈케이스를 호출하지 않는다")
    void rejectsInvalidPhoneNumber() {
        MvcTestResult result = request(new RequestVerificationRequest("01012345678", VerificationPurpose.SIGNUP));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("COMMON-001");
        assertThat(result).bodyJson().extractingPath("$.detail")
                .isEqualTo("전화번호는 010-XXXX-XXXX 형식이어야 합니다.");
        verify(verificationCommandUseCase, never()).request(any());
    }

    @Test
    @DisplayName("인증 목적이 없으면 400과 검증 메시지를 반환하고 유즈케이스를 호출하지 않는다")
    void rejectsMissingPurpose() {
        MvcTestResult result = request(new RequestVerificationRequest("010-1234-5678", null));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("인증 목적은 필수입니다.");
        verify(verificationCommandUseCase, never()).request(any());
    }

    @Test
    @DisplayName("인증번호가 일치하면 200을 반환하고 확인 유즈케이스를 호출한다")
    void confirmsVerification() {
        MvcTestResult result = confirm("verification-id", new ConfirmVerificationRequest("123456"));

        assertThat(result).hasStatus(HttpStatus.OK);
        verify(verificationCommandUseCase).confirm(new ConfirmVerificationCommand(
                "verification-id",
                new VerificationCode("123456")
        ));
    }

    @Test
    @DisplayName("인증번호 형식이 올바르지 않으면 400과 검증 메시지를 반환하고 유즈케이스를 호출하지 않는다")
    void rejectsInvalidCodeFormat() {
        MvcTestResult result = confirm("verification-id", new ConfirmVerificationRequest("12345"));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("인증번호는 6자리 숫자여야 합니다.");
        verify(verificationCommandUseCase, never()).confirm(any());
    }

    @Test
    @DisplayName("인증번호가 일치하지 않으면 400과 오류 코드를 반환한다")
    void rejectsMismatchedCode() {
        willThrow(new BusinessException(VerificationErrorCode.CODE_MISMATCH))
                .given(verificationCommandUseCase).confirm(any());

        MvcTestResult result = confirm("verification-id", new ConfirmVerificationRequest("123456"));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VERIFICATION-003");
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("인증번호가 일치하지 않습니다.");
    }

    private MvcTestResult confirm(String verificationId, ConfirmVerificationRequest request) {
        return mockMvc.post()
                .uri(VERIFICATION_URL + "/{verificationId}/confirm", verificationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();
    }

    private MvcTestResult request(RequestVerificationRequest request) {
        return mockMvc.post()
                .uri(VERIFICATION_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();
    }
}
