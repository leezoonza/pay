package com.zoonza.pay.auth.internal.application.port.out;

import com.zoonza.pay.auth.internal.application.dto.RefreshToken;

public interface RefreshTokenStore {
    void save(RefreshToken refreshToken);
}
