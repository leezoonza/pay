package com.zoonza.pay.auth.internal.fixture;

import com.zoonza.pay.customer.api.CustomerApi;
import com.zoonza.pay.shared.domain.PhoneNumber;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryCustomerApi implements CustomerApi {
    private final Map<PhoneNumber, Long> customerIds = new HashMap<>();

    @Override
    public Optional<Long> findIdByPhoneNumber(PhoneNumber phoneNumber) {
        return Optional.ofNullable(customerIds.get(phoneNumber));
    }

    public void register(PhoneNumber phoneNumber, Long customerId) {
        customerIds.put(phoneNumber, customerId);
    }
}
