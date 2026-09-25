package com.zoonza.pay.auth.internal.domain;

import com.zoonza.pay.shared.error.ErrorCode;

public enum AuthErrorCode implements ErrorCode {
    CUSTOMER_NOT_FOUND("AUTH-001", "핸드폰 번호를 확인해 주세요.", 404);

    private final String code;
    private final String message;
    private final int status;

    AuthErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public int getStatus() {
        return this.status;
    }
}
