package com.zoonza.pay.customer.internal.application.service;

import com.zoonza.pay.customer.api.CustomerApi;
import com.zoonza.pay.customer.internal.domain.Customer;
import com.zoonza.pay.customer.internal.domain.CustomerRepository;
import com.zoonza.pay.shared.domain.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerQueryService implements CustomerApi {
    private final CustomerRepository customerRepository;

    @Override
    public Optional<Long> findIdByPhoneNumber(PhoneNumber phoneNumber) {
        return customerRepository.findByPhoneNumber(phoneNumber)
                .map(Customer::getId);
    }
}
