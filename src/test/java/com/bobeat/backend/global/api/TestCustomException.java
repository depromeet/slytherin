package com.bobeat.backend.global.api;

import com.bobeat.backend.global.exception.CustomException;
import com.bobeat.backend.global.exception.ErrorCode;

public class TestCustomException extends CustomException {
    public TestCustomException() {
        super(ErrorCode.INTERNAL_SERVER);
    }

    public TestCustomException(String message) {
        super(message,ErrorCode.INTERNAL_SERVER);
    }
}
