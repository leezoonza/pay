package com.zoonza.pay.customer.internal.adapter.in.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class SignupRequestTests {

    private static final String VALID_NAME = "김철수";
    private static final String VALID_PHONE_NUMBER = "010-1234-5678";

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    @DisplayName("이름과 전화번호가 올바르면 검증을 통과한다")
    void acceptsValidRequest() {
        assertThat(validate(VALID_NAME, VALID_PHONE_NUMBER)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 30})
    @DisplayName("이름이 2자 또는 30자이면 검증을 통과한다")
    void acceptsNamesAtMinimumAndMaximumLengths(int length) {
        assertThat(validate("가".repeat(length), VALID_PHONE_NUMBER)).isEmpty();
    }

    @Test
    @DisplayName("이름을 입력하지 않으면 검증에 실패한다")
    void rejectsNullName() {
        assertThat(messagesOf(validate(null, VALID_PHONE_NUMBER)))
                .containsExactly("이름은 필수입니다.");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 31})
    @DisplayName("이름이 2~30자 범위를 벗어나면 검증에 실패한다")
    void rejectsNamesOutsideAllowedLength(int length) {
        assertThat(messagesOf(validate("가".repeat(length), VALID_PHONE_NUMBER)))
                .containsExactly("이름은 2자 이상 30자 이하여야 합니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"김 철", "김철1"})
    @DisplayName("이름에 공백이나 숫자가 있으면 검증에 실패한다")
    void rejectsNamesContainingSpacesOrDigits(String name) {
        assertThat(messagesOf(validate(name, VALID_PHONE_NUMBER)))
                .containsExactly("이름에는 문자만 사용할 수 있습니다.");
    }

    @Test
    @DisplayName("전화번호를 입력하지 않으면 검증에 실패한다")
    void rejectsNullPhoneNumber() {
        assertThat(messagesOf(validate(VALID_NAME, null)))
                .containsExactly("전화번호는 필수입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"01012345678", "011-1234-5678", "010-123-5678"})
    @DisplayName("전화번호가 010-XXXX-XXXX 형식이 아니면 검증에 실패한다")
    void rejectsInvalidPhoneNumberFormat(String phoneNumber) {
        assertThat(messagesOf(validate(VALID_NAME, phoneNumber)))
                .containsExactly("전화번호는 010-XXXX-XXXX 형식이어야 합니다.");
    }

    private Set<ConstraintViolation<SignupRequest>> validate(String name, String phoneNumber) {
        return validator.validate(new SignupRequest(name, phoneNumber));
    }

    private static Set<String> messagesOf(Set<ConstraintViolation<SignupRequest>> violations) {
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }
}
