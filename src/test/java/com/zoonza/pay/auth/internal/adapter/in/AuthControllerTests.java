package com.zoonza.pay.auth.internal.adapter.in;

import com.zoonza.pay.auth.internal.adapter.in.dto.LoginRequest;
import com.zoonza.pay.auth.internal.adapter.in.support.RefreshTokenCookieProperties;
import com.zoonza.pay.auth.internal.adapter.in.support.TokenCookieManager;
import com.zoonza.pay.auth.internal.application.dto.AccessToken;
import com.zoonza.pay.auth.internal.application.dto.LoginCommand;
import com.zoonza.pay.auth.internal.application.dto.LoginResult;
import com.zoonza.pay.auth.internal.application.dto.RefreshToken;
import com.zoonza.pay.auth.internal.application.port.in.AuthCommandUseCase;
import com.zoonza.pay.auth.internal.domain.AuthErrorCode;
import com.zoonza.pay.shared.domain.PhoneNumber;
import com.zoonza.pay.shared.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@WebMvcTest(
        value = AuthController.class,
        properties = "auth.cookie.secure=true"
)
@AutoConfigureMockMvc(addFilters = false)
@EnableConfigurationProperties(RefreshTokenCookieProperties.class)
@Import(TokenCookieManager.class)
class AuthControllerTests {
    private static final String LOGIN_URL = "/api/auth/login";

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthCommandUseCase authCommandUseCase;

    @Test
    @DisplayName("로그인하면 200과 액세스 토큰을 반환하고 리프레시 토큰을 HttpOnly 쿠키로 내려준다")
    void logsIn() {
        given(authCommandUseCase.login(any())).willReturn(new LoginResult(
                new AccessToken("access-token"),
                new RefreshToken("refresh-token", 1L, Instant.now().plus(Duration.ofDays(14)))
        ));

        MvcTestResult result = login(new LoginRequest("010-1234-5678", "verification-id"));

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(result).bodyJson().extractingPath("$.accessToken").isEqualTo("access-token");
        assertThat(result.getResponse().getHeader(HttpHeaders.SET_COOKIE))
                .startsWith("refresh_token=refresh-token;")
                .contains("Path=/api/auth", "Secure", "HttpOnly", "SameSite=Strict")
                .containsPattern("Max-Age=12095\\d\\d");
        verify(authCommandUseCase).login(new LoginCommand(new PhoneNumber("010-1234-5678"), "verification-id"));
    }

    @Test
    @DisplayName("인증 ID가 없으면 400과 검증 메시지를 반환하고 유즈케이스를 호출하지 않는다")
    void rejectsMissingVerificationId() {
        MvcTestResult result = login(new LoginRequest("010-1234-5678", " "));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("인증 ID는 필수입니다.");
        verify(authCommandUseCase, never()).login(any());
    }

    @Test
    @DisplayName("전화번호 형식이 올바르지 않으면 400과 검증 메시지를 반환하고 유즈케이스를 호출하지 않는다")
    void rejectsInvalidPhoneNumber() {
        MvcTestResult result = login(new LoginRequest("01012345678", "verification-id"));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.detail")
                .isEqualTo("전화번호는 010-XXXX-XXXX 형식이어야 합니다.");
        verify(authCommandUseCase, never()).login(any());
    }

    @Test
    @DisplayName("가입되지 않은 회원이면 404와 오류 코드를 반환하고 쿠키를 내려주지 않는다")
    void rejectsUnregisteredCustomer() {
        given(authCommandUseCase.login(any())).willThrow(new BusinessException(AuthErrorCode.CUSTOMER_NOT_FOUND));

        MvcTestResult result = login(new LoginRequest("010-1234-5678", "verification-id"));

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("AUTH-001");
        assertThat(result.getResponse().getHeader(HttpHeaders.SET_COOKIE)).isNull();
    }

    private MvcTestResult login(LoginRequest request) {
        return mockMvc.post()
                .uri(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .exchange();
    }
}
