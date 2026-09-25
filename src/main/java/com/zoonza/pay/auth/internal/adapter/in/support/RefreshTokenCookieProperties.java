package com.zoonza.pay.auth.internal.adapter.in.support;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("auth.cookie")
public record RefreshTokenCookieProperties(
        boolean secure
) {
}
