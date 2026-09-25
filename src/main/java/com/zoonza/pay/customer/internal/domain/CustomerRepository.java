package com.zoonza.pay.customer.internal.domain;

import com.zoonza.pay.shared.domain.PhoneNumber;

import java.util.Optional;

public interface CustomerRepository {
    boolean existsByPhoneNumber(PhoneNumber phoneNumber);
    Customer save(Customer customer);

    Optional<Customer> findByPhoneNumber(PhoneNumber phoneNumber);
}
