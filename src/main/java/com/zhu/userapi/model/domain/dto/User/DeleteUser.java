package com.zhu.userapi.model.domain.dto.User;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class DeleteUser implements Serializable {
    /**
     * 用户id
     */
    @TableId
    private long id;


    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}