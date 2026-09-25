package com.zoonza.pay.customer.internal.domain;

import com.zoonza.pay.shared.domain.PhoneNumber;

public interface CustomerRepository {
    boolean existsByPhoneNumber(PhoneNumber phoneNumber);
    Customer save(Customer customer);
}
