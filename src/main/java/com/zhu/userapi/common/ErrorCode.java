package com.zhu.userapi.common;

public enum ErrorCode {
    SUCCESS(0,"ok",""),
    PARAMS_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(40001,"请求数据为空",""),
    NULL_ACCOUNT_ERROR(40011,"账号或密码为空",""),
    PASSWORD_NOT_MATCH(40002,"密码校验不匹配",""),
    ACCOUNT_LENGTH_SHORT(40003,"账号太短，请重新输入账号",""),
    PASSWORD_LENGTH_SHORT(40004,"密码太短，请重新输入密码",""),
    ACCOUNT_INELIGIBLE(40005,"账号不符合规定，请重新输入账号",""),
    ACCOUNT_EXISTED(40006,"账号已存在",""),
    SAVE_FAILED(40007,"保存失败",""),
    NOT_LOGIN_ERROR(40100, "未登录",""),
    NO_AUTH_ERROR(40101,"无管理员权限",""),
    FORBIDDEN_ERROR(40300, "禁止访问",""),
    NOT_FOUND_ERROR(40400, "请求数据不存在",""),
    SYSTEM_ERROR(50000,"系统内部异常",""),
    OPERATION_ERROR(50001,"操作失败" , ""),
    NOT_MATCH_ACCOUNT(50002,"用户账号不匹配",""),;

    private final Integer code;
    private final String message;
    private final String description;

    ErrorCode(Integer code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
