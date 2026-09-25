package com.zoonza.pay.verification.internal.domain;

import java.security.SecureRandom;
import java.util.regex.Pattern;

public record VerificationCode(String value) {
    private static final Pattern SIX_DIGITS_PATTERN = Pattern.compile("\\d{6}");
    private static final SecureRandom RANDOM = new SecureRandom();

    public VerificationCode {
        if (value == null || !SIX_DIGITS_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("인증번호는 6자리 숫자여야 합니다.");
        }
    }

    public static VerificationCode generate() {
        return new VerificationCode("%06d".formatted(RANDOM.nextInt(1_000_000)));
    }
}
