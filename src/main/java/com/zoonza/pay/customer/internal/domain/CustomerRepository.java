package com.zoonza.pay.customer.internal.domain;

public interface CustomerRepository {
    boolean existsByPhoneNumber(PhoneNumber phoneNumber);
    Customer save(Customer customer);
}
