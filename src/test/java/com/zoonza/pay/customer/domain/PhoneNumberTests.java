package com.zoonza.pay.customer.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumberTests {

    @Test
    @DisplayName("010-XXXX-XXXX 형식으로 입력하면 전화번호를 생성할 수 있다")
    void acceptsPhoneNumberInRequiredFormat() {
        assertThat(new PhoneNumber("010-1234-5678").value()).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("전화번호를 입력하지 않으면 생성할 수 없다")
    void rejectsNullPhoneNumber() {
        assertThatThrownBy(() -> new PhoneNumber(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("전화번호는 필수입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"011-1234-5678", "01012345678", "010-123-5678"})
    @DisplayName("전화번호가 010-XXXX-XXXX 형식이 아니면 생성할 수 없다")
    void rejectsPhoneNumbersOutsideRequiredFormat(String value) {
        assertThatThrownBy(() -> new PhoneNumber(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("전화번호는 010-XXXX-XXXX 형식이어야 합니다.");
    }
}
