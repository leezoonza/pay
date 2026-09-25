package com.zoonza.pay.customer.internal.adapter.out.persistence;

import com.zoonza.pay.customer.internal.domain.Customer;
import com.zoonza.pay.shared.domain.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerJpaRepository extends JpaRepository<Customer, Long> {
    boolean existsByPhoneNumber(PhoneNumber phoneNumber);
}
