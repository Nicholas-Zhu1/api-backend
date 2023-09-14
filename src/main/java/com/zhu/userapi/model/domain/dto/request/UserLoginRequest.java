package com.zhu.userapi.model.domain.dto.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Data
public class UserLoginRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -4796326144129122935L;
    private String userAccount;
    private String userPassword;
}
