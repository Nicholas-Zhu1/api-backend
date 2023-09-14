package com.zhu.userapi.model.domain.dto.request;

import com.baomidou.mybatisplus.annotation.TableField;
import com.zhu.userapi.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class ListUserQuery extends PageRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 性别
     */
    private Integer gender;


    /**
     * 用户角色0-普通用户1-管理员
     */
    private String userRole;

    /**
     * 用户状态正常-0
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
