package com.zoonza.pay.auth.internal.fixture;

import com.zoonza.pay.auth.internal.application.dto.RefreshToken;
import com.zoonza.pay.auth.internal.application.port.out.RefreshTokenStore;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InMemoryRefreshTokenStore implements RefreshTokenStore {
    private final Map<String, RefreshToken> refreshTokens = new LinkedHashMap<>();

    @Override
    public void save(RefreshToken refreshToken) {
        refreshTokens.put(refreshToken.value(), refreshToken);
    }

    @Override
    public void delete(String refreshTokenValue) {
        refreshTokens.remove(Objects.requireNonNull(refreshTokenValue));
    }

    public List<RefreshToken> savedRefreshTokens() {
        return List.copyOf(refreshTokens.values());
    }
}
