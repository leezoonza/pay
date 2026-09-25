package com.zoonza.pay.auth.internal.fixture;

import com.zoonza.pay.auth.internal.application.dto.RefreshToken;
import com.zoonza.pay.auth.internal.application.port.out.RefreshTokenStore;

import java.util.ArrayList;
import java.util.List;

public class InMemoryRefreshTokenStore implements RefreshTokenStore {
    private final List<RefreshToken> refreshTokens = new ArrayList<>();

    @Override
    public void save(RefreshToken refreshToken) {
        refreshTokens.add(refreshToken);
    }

    public List<RefreshToken> savedRefreshTokens() {
        return List.copyOf(refreshTokens);
    }
}
