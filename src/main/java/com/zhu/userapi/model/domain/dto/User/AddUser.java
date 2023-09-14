package com.zhu.userapi.model.domain.dto.User;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户
 *
 * @TableName user
 */
@TableName(value = "user")
@Data
public class AddUser implements Serializable {
    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String userAvatar;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 性别
     */
    private Integer gender;


    /**
     * 用户角色user-普通用户admin-管理员
     */
    private String userRole;

    /**
     * 用户状态正常-0
     */
    private Integer userStatus;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}