package com.zoonza.pay.customer.application.service;

import com.zoonza.pay.customer.application.dto.RegisterCustomerCommand;
import com.zoonza.pay.customer.application.port.in.CustomerCommandUseCase;
import com.zoonza.pay.customer.domain.Customer;
import com.zoonza.pay.customer.domain.CustomerErrorCode;
import com.zoonza.pay.customer.domain.CustomerRepository;
import com.zoonza.pay.shared.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerCommandService implements CustomerCommandUseCase {
    private final CustomerRepository customerRepository;

    @Override
    public void register(RegisterCustomerCommand command) {
        if (customerRepository.existsByPhoneNumber(command.phoneNumber())) {
            throw new BusinessException(CustomerErrorCode.ALREADY_REGISTERED);
        }

        Customer customer = Customer.register(command.name(), command.phoneNumber());
        customerRepository.save(customer);
    }
}
