package com.zoonza.pay.customer.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerTests {

    @Test
    @DisplayName("회원을 등록을 등록한다")
    void registerCustomer() {
        Name name = new Name("김철수");
        PhoneNumber phoneNumber = new PhoneNumber("010-1234-5678");
        Instant beforeRegistration = Instant.now();

        Customer customer = Customer.register(name, phoneNumber);

        Instant afterRegistration = Instant.now();
        assertThat(customer.getName()).isEqualTo(name);
        assertThat(customer.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(customer.getCreatedAt()).isBetween(beforeRegistration, afterRegistration);
        assertThat(customer.getUpdatedAt()).isEqualTo(customer.getCreatedAt());
    }
}
