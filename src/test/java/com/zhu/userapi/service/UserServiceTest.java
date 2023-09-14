package com.zhu.userapi.service;

import java.util.Date;

import com.zhu.apicommon.model.entity.User;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;


@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testList() {
        for (int i = 0; i < 1000; i++) {
            User user = new User();
            user.setId(0L);
            user.setUserName("jiazhu");
            user.setUserAccount("jiazhu"+i);
            user.setUserPassword("12345678");
            user.setGender(0);
            user.setUserStatus(0);
            user.setCreateTime(new Date());
            user.setUpdateTime(new Date());
            user.setIsDelete(0);
            userService.userSave(user);
        }
    }

    @Test
    void testDigest() {
        System.out.println(DigestUtils.md5DigestAsHex("abd".getBytes()));

    }

    @Test
    void userRegister() {
        String userAccount="fasdf6";
        String userPassword="12345678";
        String checkPassword="12345678";
        long result=userService.userRegister(userAccount, userPassword, checkPassword);
        Assert.assertTrue(result>0);
    }
}