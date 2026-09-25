package com.zoonza.pay.customer.internal.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NameTests {

    @ParameterizedTest
    @ValueSource(ints = {2, 30})
    @DisplayName("이름이 2자 또는 30자이면 생성할 수 있다")
    void acceptsNamesAtMinimumAndMaximumLengths(int length) {
        String value = "가".repeat(length);

        assertThat(new Name(value).value()).isEqualTo(value);
    }

    @Test
    @DisplayName("이름을 입력하지 않으면 생성할 수 없다")
    void rejectsNullName() {
        assertThatThrownBy(() -> new Name(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름은 필수입니다.");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 31})
    @DisplayName("이름이 2~30자 범위를 벗어나면 생성할 수 없다")
    void rejectsNamesOutsideAllowedLength(int length) {
        assertThatThrownBy(() -> new Name("가".repeat(length)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름은 2자 이상 30자 이하여야 합니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"김 철", "김철1"})
    @DisplayName("이름에 공백이나 숫자가 있으면 생성할 수 없다")
    void rejectsNamesContainingSpacesOrDigits(String value) {
        assertThatThrownBy(() -> new Name(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름에는 문자만 사용할 수 있습니다.");
    }
}
