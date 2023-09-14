package com.zhu.userapi.common;

/**
 * 返回工具类
 * @author zhu
 */
public class ResultUtil {
    /**
     * 成功
     * @param data 数据
     */
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0,data,"","");
    }

    /**
     * 失败
     * @param code 失败码
     */
    public static <T> BaseResponse<T> error(int code){
        return new BaseResponse<>(code,null,"","");
    }
    public static <T> BaseResponse<T> error(int code,String msg){
        return new BaseResponse<>(code,null,msg,"");
    }
    public static <T> BaseResponse<T> error(int code,String msg,String description){
        return new BaseResponse<>(code,null,msg,description);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode.getCode(),null,errorCode.getMessage(),errorCode.getDescription());
    }
    public static <T> BaseResponse<T> error(ErrorCode errorCode,String description){
        return new BaseResponse<>(errorCode.getCode(),null,errorCode.getMessage(),description);
    }
}
