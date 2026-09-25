package com.zoonza.pay.bootstrap.web;

import com.zoonza.pay.shared.error.BusinessException;
import com.zoonza.pay.shared.error.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .filter(errorMessage -> errorMessage != null && !errorMessage.isBlank())
                .findFirst()
                .orElse(null);

        ProblemDetail problemDetail = getProblemDetail(CommonErrorCode.INVALID_REQUEST, message);

        return handleExceptionInternal(e, problemDetail, headers, status, request);
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException e) {
        return getProblemDetail(e.getErrorCode());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException e) {
        return getProblemDetail(CommonErrorCode.INVALID_REQUEST, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception e) {
        return getProblemDetail(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ProblemDetail getProblemDetail(ErrorCode errorCode) {
        return getProblemDetail(errorCode, errorCode.getMessage());
    }

    private ProblemDetail getProblemDetail(ErrorCode errorCode, String message) {
        String detail = message == null || message.isBlank()
                ? errorCode.getMessage()
                : message;

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatusCode.valueOf(errorCode.getStatus()),
                detail
        );

        problemDetail.setProperty("code", errorCode.getCode());

        return problemDetail;
    }
}
