package com.zhu.userapi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhu.apicommon.model.entity.User;
import com.zhu.userapi.model.domain.dto.User.UpdateUser;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author ZHU
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2023-02-02 15:39:47
*/
@Service
public interface UserService extends IService<User> {
    long userRegister(String userAccount,String userPassword,String checkPassword);
    User userLogin(String userAccount, String userPassword, HttpServletRequest httpServletRequest);
    User getSafetyUser(User user);
    int outLogin(HttpServletRequest httpServletRequest);
    Boolean userSave(User newUser);
    Boolean userUpdate(UpdateUser newUser);
    boolean isAdmin(HttpServletRequest request);
    User getLoginUser(HttpServletRequest request);
    List<User> listUser(User user, HttpServletRequest httpServletRequest);
    Boolean validUser(User user, boolean add);
}
