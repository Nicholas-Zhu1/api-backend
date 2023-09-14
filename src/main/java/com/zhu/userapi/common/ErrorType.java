package com.zhu.userapi.common;

public class ErrorType {
    public static ErrorCode getErrorType(Integer code){
        for (ErrorCode errocode:
             ErrorCode.values()) {
            if (code.equals(errocode.getCode())){
                return errocode;
            }
        }
        return null;
    }

}
