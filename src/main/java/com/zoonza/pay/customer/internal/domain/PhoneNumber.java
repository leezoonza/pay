package com.zoonza.pay.customer.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.regex.Pattern;

@Embeddable
public record PhoneNumber(
        @Column(name = "phone_number", unique = true, nullable = false)
        String value
) {
    private static final Pattern KOREAN_MOBILE_NUMBER_PATTERN = Pattern.compile("010-\\d{4}-\\d{4}");

    public PhoneNumber {
        if (value == null) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }

        if (!KOREAN_MOBILE_NUMBER_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("전화번호는 010-XXXX-XXXX 형식이어야 합니다.");
        }
    }
}
