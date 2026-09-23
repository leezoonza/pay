package com.zoonza.pay.customer.domain;

import com.zoonza.pay.shared.error.ErrorCode;

public enum CustomerErrorCode implements ErrorCode {
    ALREADY_REGISTERED("CUSTOMER-001", "이미 가입된 회원입니다.", 409);

    private final String code;
    private final String message;
    private final int status;

    CustomerErrorCode(String code, String message, int status) {
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
