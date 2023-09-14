package com.zhu.userapi.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用返回类
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 6290224296984944426L;
    private int code;
    private T data;
    private String msg;
    private String description;

    public BaseResponse(int code, T data, String msg, String description) {
        this.code = code;
        this.data = data;
        this.msg = msg;
        this.description = description;
    }

    public BaseResponse(int code, T data, String msg) {
        this(code,data,msg,"");
    }
    public BaseResponse(int code, T data) {
        this(code,data,"","");
    }
    public BaseResponse(int code) {
        this(code,null,"","");
    }
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(),null,errorCode.getMessage(),errorCode.getDescription());
    }
}
