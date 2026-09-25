package com.zoonza.pay.auth.internal.application.port.out;

import com.zoonza.pay.auth.internal.application.dto.RefreshToken;

import java.util.Optional;

public interface RefreshTokenStore {
    void save(RefreshToken refreshToken);

    Optional<Long> findCustomerIdAndDelete(String refreshTokenValue);

    void delete(String refreshTokenValue);
}
