package com.zoonza.pay.auth.internal.application;

import com.zoonza.pay.auth.internal.application.dto.LoginCommand;
import com.zoonza.pay.auth.internal.application.dto.TokenResult;
import com.zoonza.pay.auth.internal.application.port.in.AuthCommandUseCase;
import com.zoonza.pay.auth.internal.application.service.AuthCommandService;
import com.zoonza.pay.auth.internal.domain.AuthErrorCode;
import com.zoonza.pay.auth.internal.fixture.*;
import com.zoonza.pay.auth.internal.fixture.RecordingVerificationApi.ConsumedVerification;
import com.zoonza.pay.shared.domain.PhoneNumber;
import com.zoonza.pay.shared.error.BusinessException;
import com.zoonza.pay.shared.error.ErrorCode;
import com.zoonza.pay.verification.api.VerificationPurpose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthCommandUseCaseTests {
    private static final PhoneNumber PHONE_NUMBER = new PhoneNumber("010-1234-5678");
    private static final Long CUSTOMER_ID = 1L;
    private static final String VERIFICATION_ID = "verification-id";

    private final InMemoryCustomerApi customerApi = new InMemoryCustomerApi();
    private final RecordingVerificationApi verificationApi = new RecordingVerificationApi();
    private final InMemoryRefreshTokenStore refreshTokenStore = new InMemoryRefreshTokenStore();
    private final AuthCommandUseCase useCase = new AuthCommandService(
            customerApi,
            verificationApi,
            new FixedAccessTokenIssuer(),
            new SequentialRefreshTokenIssuer(),
            refreshTokenStore
    );

    @Test
    @DisplayName("가입된 회원이 로그인 인증을 마치면 인증을 사용 처리하고 토큰을 발급한다")
    void logsIn() {
        customerApi.register(PHONE_NUMBER, CUSTOMER_ID);

        TokenResult result = useCase.login(new LoginCommand(PHONE_NUMBER, VERIFICATION_ID));

        assertThat(verificationApi.consumedVerifications()).containsExactly(
                new ConsumedVerification(VERIFICATION_ID, PHONE_NUMBER, VerificationPurpose.LOGIN)
        );
        assertThat(result.accessToken().value()).isEqualTo("access-token-1");
        assertThat(result.refreshToken().value()).isEqualTo("refresh-token-1-1");
        assertThat(refreshTokenStore.savedRefreshTokens()).containsExactly(result.refreshToken());
    }

    @Test
    @DisplayName("가입되지 않은 전화번호면 인증을 사용 처리하지 않고 오류를 반환한다")
    void rejectsUnregisteredCustomer() {
        assertThatThrownBy(() -> useCase.login(new LoginCommand(PHONE_NUMBER, VERIFICATION_ID)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.CUSTOMER_NOT_FOUND));
        assertThat(verificationApi.consumedVerifications()).isEmpty();
        assertThat(refreshTokenStore.savedRefreshTokens()).isEmpty();
    }

    @Test
    @DisplayName("인증을 사용할 수 없으면 오류를 반환하고 토큰을 발급하지 않는다")
    void rejectsWhenVerificationCannotBeConsumed() {
        customerApi.register(PHONE_NUMBER, CUSTOMER_ID);
        BusinessException failure = new BusinessException(new TestErrorCode());
        verificationApi.failWith(failure);

        assertThatThrownBy(() -> useCase.login(new LoginCommand(PHONE_NUMBER, VERIFICATION_ID)))
                .isSameAs(failure);
        assertThat(refreshTokenStore.savedRefreshTokens()).isEmpty();
    }

    @Test
    @DisplayName("리프레시 토큰으로 재발급하면 기존 토큰을 폐기하고 새 토큰을 발급한다")
    void reissuesTokens() {
        customerApi.register(PHONE_NUMBER, CUSTOMER_ID);
        TokenResult loggedIn = useCase.login(new LoginCommand(PHONE_NUMBER, VERIFICATION_ID));

        TokenResult reissued = useCase.reissue(loggedIn.refreshToken().value());

        assertThat(reissued.accessToken().value()).isEqualTo("access-token-1");
        assertThat(reissued.refreshToken().customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(reissued.refreshToken().value()).isNotEqualTo(loggedIn.refreshToken().value());
        assertThat(refreshTokenStore.savedRefreshTokens()).containsExactly(reissued.refreshToken());
    }

    @Test
    @DisplayName("이미 사용한 리프레시 토큰으로는 재발급할 수 없다")
    void rejectsReusedRefreshToken() {
        customerApi.register(PHONE_NUMBER, CUSTOMER_ID);
        TokenResult loggedIn = useCase.login(new LoginCommand(PHONE_NUMBER, VERIFICATION_ID));
        useCase.reissue(loggedIn.refreshToken().value());

        assertInvalidRefreshToken(loggedIn.refreshToken().value());
    }

    @Test
    @DisplayName("저장되지 않은 리프레시 토큰으로는 재발급할 수 없다")
    void rejectsUnknownRefreshToken() {
        assertInvalidRefreshToken("unknown");
        assertThat(refreshTokenStore.savedRefreshTokens()).isEmpty();
    }

    @Test
    @DisplayName("리프레시 토큰 없이는 재발급할 수 없다")
    void rejectsMissingRefreshToken() {
        assertInvalidRefreshToken(null);
        assertThat(refreshTokenStore.savedRefreshTokens()).isEmpty();
    }

    @Test
    @DisplayName("로그아웃하면 리프레시 토큰을 삭제한다")
    void logsOut() {
        customerApi.register(PHONE_NUMBER, CUSTOMER_ID);
        TokenResult loggedIn = useCase.login(new LoginCommand(PHONE_NUMBER, VERIFICATION_ID));

        useCase.logout(loggedIn.refreshToken().value());

        assertThat(refreshTokenStore.savedRefreshTokens()).isEmpty();
    }

    @Test
    @DisplayName("리프레시 토큰 없이 로그아웃하면 아무것도 삭제하지 않는다")
    void logsOutWithoutRefreshToken() {
        customerApi.register(PHONE_NUMBER, CUSTOMER_ID);
        TokenResult loggedIn = useCase.login(new LoginCommand(PHONE_NUMBER, VERIFICATION_ID));

        useCase.logout(null);

        assertThat(refreshTokenStore.savedRefreshTokens()).containsExactly(loggedIn.refreshToken());
    }

    private void assertInvalidRefreshToken(String refreshTokenValue) {
        assertThatThrownBy(() -> useCase.reissue(refreshTokenValue))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN));
    }

    private record TestErrorCode() implements ErrorCode {
        @Override
        public String getCode() {
            return "TEST-001";
        }

        @Override
        public String getMessage() {
            return "인증을 사용할 수 없습니다.";
        }

        @Override
        public int getStatus() {
            return 400;
        }
    }
}
