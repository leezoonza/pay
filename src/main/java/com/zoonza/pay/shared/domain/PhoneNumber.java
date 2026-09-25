package com.zoonza.pay.shared.domain;

import jakarta.persistence.Embeddable;

import java.util.regex.Pattern;

@Embeddable
public record PhoneNumber(String value) {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("010-\\d{4}-\\d{4}");

    public PhoneNumber {
        if (value == null) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }

        if (!NUMBER_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("전화번호는 010-XXXX-XXXX 형식이어야 합니다.");
        }
    }
}
