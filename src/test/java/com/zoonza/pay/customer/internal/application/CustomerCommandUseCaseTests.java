package com.zoonza.pay.customer.internal.application;

import com.zoonza.pay.customer.internal.application.dto.RegisterCustomerCommand;
import com.zoonza.pay.customer.internal.application.port.in.CustomerCommandUseCase;
import com.zoonza.pay.customer.internal.application.service.CustomerCommandService;
import com.zoonza.pay.customer.internal.domain.Customer;
import com.zoonza.pay.customer.internal.domain.CustomerErrorCode;
import com.zoonza.pay.customer.internal.domain.Name;
import com.zoonza.pay.customer.internal.fixture.InMemoryCustomerRepository;
import com.zoonza.pay.customer.internal.fixture.RecordingVerificationApi;
import com.zoonza.pay.customer.internal.fixture.RecordingVerificationApi.ConsumedVerification;
import com.zoonza.pay.shared.domain.PhoneNumber;
import com.zoonza.pay.shared.error.BusinessException;
import com.zoonza.pay.shared.error.ErrorCode;
import com.zoonza.pay.verification.api.VerificationPurpose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerCommandUseCaseTests {
    private static final String VERIFICATION_ID = "verification-id";

    private final InMemoryCustomerRepository customerRepository = new InMemoryCustomerRepository();
    private final RecordingVerificationApi verificationApi = new RecordingVerificationApi();
    private final CustomerCommandUseCase useCase = new CustomerCommandService(customerRepository, verificationApi);

    @Test
    @DisplayName("새 전화번호로 가입하면 가입 인증을 사용 처리하고 회원을 저장한다")
    void registersNewCustomer() {
        Name name = new Name("김철수");
        PhoneNumber phoneNumber = new PhoneNumber("010-1234-5678");

        useCase.register(new RegisterCustomerCommand(name, phoneNumber, VERIFICATION_ID));

        assertThat(verificationApi.consumedVerifications()).containsExactly(
                new ConsumedVerification(VERIFICATION_ID, phoneNumber, VerificationPurpose.SIGNUP)
        );
        assertThat(customerRepository.savedCustomers()).hasSize(1);
        assertThat(customerRepository.savedCustomers().getFirst().getName()).isEqualTo(name);
        assertThat(customerRepository.savedCustomers().getFirst().getPhoneNumber()).isEqualTo(phoneNumber);
    }

    @Test
    @DisplayName("이미 가입된 전화번호로 가입하면 인증을 사용 처리하지 않고 오류를 반환한다")
    void rejectsAlreadyRegisteredPhoneNumberWithoutConsumingVerification() {
        PhoneNumber phoneNumber = new PhoneNumber("010-1234-5678");
        customerRepository.save(Customer.register(new Name("김철수"), phoneNumber));

        assertThatThrownBy(() -> useCase.register(
                new RegisterCustomerCommand(new Name("이영희"), phoneNumber, VERIFICATION_ID)
        ))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CustomerErrorCode.ALREADY_REGISTERED));
        assertThat(verificationApi.consumedVerifications()).isEmpty();
        assertThat(customerRepository.savedCustomers()).hasSize(1);
    }

    @Test
    @DisplayName("인증을 사용할 수 없으면 오류를 반환하고 회원을 저장하지 않는다")
    void rejectsWhenVerificationCannotBeConsumed() {
        BusinessException failure = new BusinessException(new TestErrorCode());
        verificationApi.failWith(failure);

        assertThatThrownBy(() -> useCase.register(
                new RegisterCustomerCommand(new Name("김철수"), new PhoneNumber("010-1234-5678"), VERIFICATION_ID)
        ))
                .isSameAs(failure);
        assertThat(customerRepository.savedCustomers()).isEmpty();
    }

    private record TestErrorCode() implements ErrorCode {
        @Override
        public String getCode() {
            return "TEST-001";
        }

        @Override
        public String getMessage() {
            return "인증을 사용할 수 없습니다.";
        }

        @Override
        public int getStatus() {
            return 400;
        }
    }
}
