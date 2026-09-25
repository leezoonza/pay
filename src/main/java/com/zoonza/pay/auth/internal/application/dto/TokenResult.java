package com.zoonza.pay.auth.internal.application.dto;

public record TokenResult(
        AccessToken accessToken,
        RefreshToken refreshToken
) {
}
