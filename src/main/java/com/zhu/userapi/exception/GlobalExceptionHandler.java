package com.zhu.userapi.exception;

import com.zhu.userapi.common.BaseResponse;
import com.zhu.userapi.common.ErrorCode;
import com.zhu.userapi.common.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    public <T> BaseResponse<T> businessExceptionHandler(BusinessException e){
        log.error("businessException: "+ e.getMessage(),e);
        return ResultUtil.error(e.getCode(),e.getMessage(),e.getDescription());
    }
    public <T> BaseResponse<T> runtimeExceptionHandler(RuntimeException e){
        log.error("runtimeException:"+ e.getMessage(),e);
        return ResultUtil.error(ErrorCode.SYSTEM_ERROR);
    }
}
