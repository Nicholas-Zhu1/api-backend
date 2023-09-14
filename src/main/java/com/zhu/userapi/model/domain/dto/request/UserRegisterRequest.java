package com.zhu.userapi.model.domain.dto.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Data
public class UserRegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 3710136981124230321L;
    private String userAccount;
    private String userPassword;
    private String checkPassword;
}
