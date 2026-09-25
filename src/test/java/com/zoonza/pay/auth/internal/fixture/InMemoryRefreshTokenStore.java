package com.zoonza.pay.auth.internal.fixture;

import com.zoonza.pay.auth.internal.application.dto.RefreshToken;
import com.zoonza.pay.auth.internal.application.port.out.RefreshTokenStore;

import java.util.*;

public class InMemoryRefreshTokenStore implements RefreshTokenStore {
    private final Map<String, RefreshToken> refreshTokens = new LinkedHashMap<>();

    @Override
    public void save(RefreshToken refreshToken) {
        refreshTokens.put(refreshToken.value(), refreshToken);
    }

    @Override
    public Optional<Long> findCustomerIdAndDelete(String refreshTokenValue) {
        return Optional.ofNullable(refreshTokens.remove(Objects.requireNonNull(refreshTokenValue)))
                .map(RefreshToken::customerId);
    }

    @Override
    public void delete(String refreshTokenValue) {
        refreshTokens.remove(Objects.requireNonNull(refreshTokenValue));
    }

    public List<RefreshToken> savedRefreshTokens() {
        return List.copyOf(refreshTokens.values());
    }
}
