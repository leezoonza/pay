package com.zoonza.pay.customer.internal.adapter.out.persistence;

import com.zoonza.pay.customer.internal.domain.Customer;
import com.zoonza.pay.customer.internal.domain.CustomerRepository;
import com.zoonza.pay.shared.domain.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaCustomerRepositoryAdapter implements CustomerRepository {
    private final CustomerJpaRepository jpaRepository;

    @Override
    public boolean existsByPhoneNumber(PhoneNumber phoneNumber) {
        return jpaRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public Customer save(Customer customer) {
        return jpaRepository.save(customer);
    }
}
