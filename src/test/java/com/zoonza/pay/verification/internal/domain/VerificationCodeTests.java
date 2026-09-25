package com.zoonza.pay.verification.internal.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerificationCodeTests {

    @RepeatedTest(20)
    @DisplayName("인증번호를 생성하면 6자리 숫자다")
    void generatesSixDigitCode() {
        assertThat(VerificationCode.generate().value()).matches("\\d{6}");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"12345", "1234567", "12345a"})
    @DisplayName("인증번호가 6자리 숫자가 아니면 생성할 수 없다")
    void rejectsInvalidCode(String value) {
        assertThatThrownBy(() -> new VerificationCode(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("인증번호는 6자리 숫자여야 합니다.");
    }
}
