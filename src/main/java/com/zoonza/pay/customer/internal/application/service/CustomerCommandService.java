package com.zoonza.pay.customer.internal.application.service;

import com.zoonza.pay.customer.internal.application.dto.RegisterCustomerCommand;
import com.zoonza.pay.customer.internal.application.port.in.CustomerCommandUseCase;
import com.zoonza.pay.customer.internal.domain.Customer;
import com.zoonza.pay.customer.internal.domain.CustomerErrorCode;
import com.zoonza.pay.customer.internal.domain.CustomerRepository;
import com.zoonza.pay.shared.error.BusinessException;
import com.zoonza.pay.verification.api.VerificationApi;
import com.zoonza.pay.verification.api.VerificationPurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerCommandService implements CustomerCommandUseCase {
    private final CustomerRepository customerRepository;
    private final VerificationApi verificationApi;

    @Override
    public void register(RegisterCustomerCommand command) {
        if (customerRepository.existsByPhoneNumber(command.phoneNumber())) {
            throw new BusinessException(CustomerErrorCode.ALREADY_REGISTERED);
        }

        verificationApi.consume(command.verificationId(), command.phoneNumber(), VerificationPurpose.SIGNUP);

        Customer customer = Customer.register(command.name(), command.phoneNumber());
        customerRepository.save(customer);
    }
}
