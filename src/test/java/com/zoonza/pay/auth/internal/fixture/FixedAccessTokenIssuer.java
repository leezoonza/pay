package com.zoonza.pay.auth.internal.fixture;

import com.zoonza.pay.auth.internal.application.dto.AccessToken;
import com.zoonza.pay.auth.internal.application.port.out.AccessTokenIssuer;

import java.time.Instant;

public class FixedAccessTokenIssuer implements AccessTokenIssuer {
    @Override
    public AccessToken issue(Long customerId, Instant now) {
        return new AccessToken("access-token-" + customerId);
    }
}
