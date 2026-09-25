package com.zoonza.pay.auth.internal.application.service;

import com.zoonza.pay.auth.internal.application.dto.AccessToken;
import com.zoonza.pay.auth.internal.application.dto.LoginCommand;
import com.zoonza.pay.auth.internal.application.dto.RefreshToken;
import com.zoonza.pay.auth.internal.application.dto.TokenResult;
import com.zoonza.pay.auth.internal.application.port.in.AuthCommandUseCase;
import com.zoonza.pay.auth.internal.application.port.out.AccessTokenIssuer;
import com.zoonza.pay.auth.internal.application.port.out.RefreshTokenIssuer;
import com.zoonza.pay.auth.internal.application.port.out.RefreshTokenStore;
import com.zoonza.pay.auth.internal.domain.AuthErrorCode;
import com.zoonza.pay.customer.api.CustomerApi;
import com.zoonza.pay.shared.error.BusinessException;
import com.zoonza.pay.verification.api.VerificationApi;
import com.zoonza.pay.verification.api.VerificationPurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthCommandService implements AuthCommandUseCase {
    private final CustomerApi customerApi;
    private final VerificationApi verificationApi;
    private final AccessTokenIssuer accessTokenIssuer;
    private final RefreshTokenIssuer refreshTokenIssuer;
    private final RefreshTokenStore refreshTokenStore;

    @Override
    public TokenResult login(LoginCommand command) {
        Long customerId = customerApi.findIdByPhoneNumber(command.phoneNumber())
                .orElseThrow(() -> new BusinessException(AuthErrorCode.CUSTOMER_NOT_FOUND));

        verificationApi.consume(command.verificationId(), command.phoneNumber(), VerificationPurpose.LOGIN);

        return issueTokens(customerId);
    }

    @Override
    public TokenResult reissue(String refreshTokenValue) {
        if (refreshTokenValue == null) {
            throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long customerId = refreshTokenStore.findCustomerIdAndDelete(refreshTokenValue)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        return issueTokens(customerId);
    }

    @Override
    public void logout(String refreshTokenValue) {
        if (refreshTokenValue == null) {
            return;
        }

        refreshTokenStore.delete(refreshTokenValue);
    }

    private TokenResult issueTokens(Long customerId) {
        Instant now = Instant.now();
        AccessToken accessToken = accessTokenIssuer.issue(customerId, now);
        RefreshToken refreshToken = refreshTokenIssuer.issue(customerId, now);
        refreshTokenStore.save(refreshToken);

        return new TokenResult(accessToken, refreshToken);
    }
}
