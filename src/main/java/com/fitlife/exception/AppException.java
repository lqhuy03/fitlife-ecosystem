package com.fitlife.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        // Truyền message lên lớp cha (RuntimeException) để log nếu cần
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}