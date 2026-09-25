package com.zoonza.pay.auth.internal.application.port.in;

import com.zoonza.pay.auth.internal.application.dto.LoginCommand;
import com.zoonza.pay.auth.internal.application.dto.LoginResult;

public interface AuthCommandUseCase {
    LoginResult login(LoginCommand command);

    void logout(String refreshTokenValue);
}
