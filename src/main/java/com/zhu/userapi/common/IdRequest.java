package com.zhu.userapi.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 删除请求
 *
 * @author zhu
 */
@Data
public class IdRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    @Serial
    private static final long serialVersionUID = 1L;
}