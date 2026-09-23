package com.zoonza.pay.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Name(
        @Column(name = "name", nullable = false)
        String value
) {
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 30;

    public Name {
        if (value == null) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }

        int length = value.codePointCount(0, value.length());

        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new IllegalArgumentException("이름은 2자 이상 30자 이하여야 합니다.");
        }

        if (!value.codePoints().allMatch(Character::isLetter)) {
            throw new IllegalArgumentException("이름에는 문자만 사용할 수 있습니다.");
        }
    }
}
