package com.zoonza.pay.bootstrap.web;

import com.zoonza.pay.shared.error.ErrorCode;

public enum CommonErrorCode implements ErrorCode {
    INVALID_REQUEST("COMMON-001", "요청 값이 올바르지 않습니다.", 400),
    INTERNAL_SERVER_ERROR("COMMON-002", "서버 내부 오류가 발생했습니다.", 500),
    UNAUTHORIZED("COMMON-003", "인증이 필요합니다.", 401),
    ;

    private final String code;
    private final String message;
    private final int status;

    CommonErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getStatus() {
        return status;
    }
}