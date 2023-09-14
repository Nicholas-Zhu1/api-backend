package com.zhu.userapi.exception;

import com.zhu.userapi.common.ErrorCode;

/**
 * 异常处理
 */
public class BusinessException extends RuntimeException {
    private Integer code;
    private String description;

    public BusinessException(String message, Integer code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
