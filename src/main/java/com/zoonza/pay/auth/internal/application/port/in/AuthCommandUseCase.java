package com.zoonza.pay.auth.internal.application.port.in;

import com.zoonza.pay.auth.internal.application.dto.LoginCommand;
import com.zoonza.pay.auth.internal.application.dto.TokenResult;

public interface AuthCommandUseCase {
    TokenResult login(LoginCommand command);

    TokenResult reissue(String refreshTokenValue);

    void logout(String refreshTokenValue);
}
