package com.zoonza.pay.shared.error;

public interface ErrorCode {
    String getCode();

    String getMessage();

    int getStatus();
}
