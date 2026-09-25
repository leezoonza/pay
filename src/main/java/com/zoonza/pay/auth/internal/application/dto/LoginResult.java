package com.zoonza.pay.auth.internal.application.dto;

public record LoginResult(
        AccessToken accessToken,
        RefreshToken refreshToken
) {
}
