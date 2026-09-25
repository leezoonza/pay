package com.zoonza.pay.auth.internal.application.port.out;

import com.zoonza.pay.auth.internal.application.dto.AccessToken;

import java.time.Instant;

public interface AccessTokenIssuer {
    AccessToken issue(Long customerId, Instant now);
}
