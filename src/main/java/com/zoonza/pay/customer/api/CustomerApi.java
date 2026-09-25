package com.zoonza.pay.customer.api;

import com.zoonza.pay.shared.domain.PhoneNumber;

import java.util.Optional;

public interface CustomerApi {
    Optional<Long> findIdByPhoneNumber(PhoneNumber phoneNumber);
}
