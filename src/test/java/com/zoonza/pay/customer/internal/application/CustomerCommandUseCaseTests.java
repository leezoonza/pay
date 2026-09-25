package com.zoonza.pay.customer.internal.application;

import com.zoonza.pay.customer.internal.application.dto.RegisterCustomerCommand;
import com.zoonza.pay.customer.internal.application.port.in.CustomerCommandUseCase;
import com.zoonza.pay.customer.internal.application.service.CustomerCommandService;
import com.zoonza.pay.customer.internal.domain.Customer;
import com.zoonza.pay.customer.internal.domain.CustomerErrorCode;
import com.zoonza.pay.customer.internal.domain.Name;
import com.zoonza.pay.customer.internal.domain.PhoneNumber;
import com.zoonza.pay.customer.internal.fixture.InMemoryCustomerRepository;
import com.zoonza.pay.shared.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerCommandUseCaseTests {
    private final InMemoryCustomerRepository customerRepository = new InMemoryCustomerRepository();
    private final CustomerCommandUseCase useCase = new CustomerCommandService(customerRepository);

    @Test
    @DisplayName("새 전화번호로 가입하면 회원을 저장한다")
    void registersNewCustomer() {
        Name name = new Name("김철수");
        PhoneNumber phoneNumber = new PhoneNumber("010-1234-5678");

        useCase.register(new RegisterCustomerCommand(name, phoneNumber));

        assertThat(customerRepository.savedCustomers()).hasSize(1);
        assertThat(customerRepository.savedCustomers().getFirst().getName()).isEqualTo(name);
        assertThat(customerRepository.savedCustomers().getFirst().getPhoneNumber()).isEqualTo(phoneNumber);
    }

    @Test
    @DisplayName("이미 가입된 전화번호로 가입하면 오류를 반환하고 새 회원을 저장하지 않는다")
    void rejectsAlreadyRegisteredPhoneNumberWithoutSavingAnotherCustomer() {
        PhoneNumber phoneNumber = new PhoneNumber("010-1234-5678");
        customerRepository.save(Customer.register(new Name("김철수"), phoneNumber));

        assertThatThrownBy(() -> useCase.register(
                new RegisterCustomerCommand(new Name("이영희"), phoneNumber)
        ))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CustomerErrorCode.ALREADY_REGISTERED));
        assertThat(customerRepository.savedCustomers()).hasSize(1);
    }
}
