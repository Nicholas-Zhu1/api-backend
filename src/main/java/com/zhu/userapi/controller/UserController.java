package com.zhu.userapi.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.zhu.apicommon.model.entity.User;
import com.zhu.userapi.annotation.AuthCheck;
import com.zhu.userapi.common.*;
import com.zhu.userapi.contant.UserConstant;
import com.zhu.userapi.exception.BusinessException;
import com.zhu.userapi.model.domain.VO.UpLoadUserAvatar;
import com.zhu.userapi.model.domain.VO.UserVO;
import com.zhu.userapi.model.domain.dto.User.AddUser;
import com.zhu.userapi.model.domain.dto.User.DeleteUser;
import com.zhu.userapi.model.domain.dto.User.UpdateUser;
import com.zhu.userapi.model.domain.dto.request.ListUserQuery;
import com.zhu.userapi.model.domain.dto.request.UserLoginRequest;
import com.zhu.userapi.model.domain.dto.request.UserRegisterRequest;
import com.zhu.userapi.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

import static com.zhu.userapi.contant.UserConstant.DEFAULT_PASSWORD;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 用户Id
     */
    @PostMapping("/register")
    public BaseResponse<Long> UserRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            return ResultUtil.error(ErrorCode.NULL_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            if (userAccount == null||userPassword == null) {
                return ResultUtil.error(ErrorCode.NULL_ERROR,"账号或密码不能为空！");
            }
        }
        long checkUserRegisterId = userService.userRegister(userAccount, userPassword, checkPassword);
        ErrorCode errorCode = ErrorType.getErrorType((int) checkUserRegisterId);
        if (checkUserRegisterId == CheckUser.RIGHT_CODE) {
            return ResultUtil.success(checkUserRegisterId);
        } else {
            assert errorCode != null;
            return ResultUtil.error((int) checkUserRegisterId,errorCode.getMessage(),"注册失败！");
        }
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @param httpServletRequest 当前登录用户信息
     * @return 用户
     */
    @PostMapping("/login")
    public BaseResponse<User> UserLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest) {
        if (userLoginRequest == null) {
            return ResultUtil.error(ErrorCode.NULL_ERROR,"登录信息不能为空！");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtil.error(ErrorCode.NULL_ERROR,"账号或密码不能为空！");
        }
        User user = userService.userLogin(userAccount, userPassword, httpServletRequest);

        return user == null ? ResultUtil.error(ErrorCode.PARAMS_ERROR,"用户名或密码输入错误") : ResultUtil.success(user);
    }

    /**
     * 用户注销
     *
     * @param httpServletRequest 当前登录用户信息
     * @return 退出登录
     */
    @PostMapping("/outLogin")
    public BaseResponse<Integer> OutLogin(HttpServletRequest httpServletRequest) {
        if (httpServletRequest == null) {
            return ResultUtil.error(ErrorCode.NULL_ERROR,"退出登录失败！");
        }
        int result = userService.outLogin(httpServletRequest);
        return ResultUtil.success(result);
    }

    /**
     * 获取当前用户
     *
     * @param httpServletRequest 当前登录用户信息
     * @return 当前用户信息
     */
    @GetMapping("/currentUser")
    public BaseResponse<User> getCurrentUser(HttpServletRequest httpServletRequest) {
        Object userObject = httpServletRequest.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObject;
        if (currentUser == null) {
            return ResultUtil.error(ErrorCode.NULL_ERROR,"当前用户不存在！");
        }
        User user = userService.getById(currentUser.getId());
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtil.success(safetyUser);
    }
    /**
     * 获取用户列表(所有)
     *
     * @param httpServletRequest 当前用户信息
     * @return 所有用户列表
     */
    @GetMapping("/list")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<UserVO>> listUser(ListUserQuery listUserQuery,
                                               HttpServletRequest httpServletRequest) {
        User userQuery = new User();
        if (listUserQuery != null) {
            BeanUtils.copyProperties(listUserQuery, userQuery);
        }
        List<User> userList = userService.listUser(userQuery,httpServletRequest);
        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        return ResultUtil.success(userVOList);
    }


    /**
     * 获取用户列表（根据条件）
     *
     * @param httpServletRequest 当前用户信息
     * @return 所有用户列表
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list/page")
    public BaseResponse<Page<UserVO>> listUserByPage(ListUserQuery listUserQuery, HttpServletRequest httpServletRequest) {
        long current = 1;
        long size = 10;
        if (httpServletRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //先查缓存
        User loginUser = userService.getLoginUser(httpServletRequest);
        String redisKey = String.format("zhu:user:%s",loginUser.getId());
        ValueOperations<String,Object> valueOperation = redisTemplate.opsForValue();
        Page<UserVO> userList = (Page<UserVO>) valueOperation.get(redisKey);
        if (userList!=null){
            return ResultUtil.success(userList);
        }
        //再查数据库
        User userQuery = new User();
        if (listUserQuery != null) {
            BeanUtils.copyProperties(listUserQuery, userQuery);
            current = listUserQuery.getCurrent();
            size = listUserQuery.getPageSize();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        Page<User> userPage = userService.page(new Page<>(current, size), queryWrapper);
        Page<UserVO> userVOPage = new PageDTO<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> userVOList = userPage.getRecords().stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        userVOPage.setRecords(userVOList);

        //写入缓存
        try {
            valueOperation.set(redisKey,userPage);
        }catch (Exception e){
            log.error("redis set key error",e);
        }
        return ResultUtil.success(userVOPage);
    }


    /**
     * 保存用户信息
     * @param newUser 添加新用户信息
     * @param httpServletRequest 当前用户信息
     * @return 添加的结果
     */
    @AuthCheck(mustRole = "admin")
    @PostMapping("/add")
    public BaseResponse<Boolean> AddUser(@RequestBody AddUser newUser, HttpServletRequest httpServletRequest) {
        if (httpServletRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //把传入的添加用户的信息传到新建的User中
        User user1 = new User();
        BeanUtils.copyProperties(newUser, user1);
        if (StringUtils.isEmpty(user1.getUserPassword())) {
            user1.setUserPassword(DEFAULT_PASSWORD);
        }
        //用户信息校验成功
        boolean saveUser = userService.userSave(user1);

        return saveUser?ResultUtil.success(true):ResultUtil.error(ErrorCode.PARAMS_ERROR,"添加用户失败！");
    }

    /**
     * 更新用户信息
     *
     * @param updateUser  更新用户信息
     * @param httpServletRequest 当前用户信息
     * @return 结果
     */
    @AuthCheck(mustRole = "admin")
    @PostMapping("/update")
    public BaseResponse<Boolean> UpdateUser(@RequestBody UpdateUser updateUser, HttpServletRequest httpServletRequest) {
        if (httpServletRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(httpServletRequest);

        //把传入的添加用户的信息传到的User中
        User newUser = new User();
        BeanUtils.copyProperties(updateUser, newUser);
        //校验修改用户合法性
        if (!userService.validUser(newUser, false)) {
            return ResultUtil.error(ErrorCode.PARAMS_ERROR,"用户信息不合法，修改失败！");
        }
        //仅本人或管理员可以操作
        if (!loginUser.getId().equals(updateUser.getId()) && !userService.isAdmin(httpServletRequest)) {
            return ResultUtil.error(ErrorCode.NOT_MATCH_ACCOUNT,"无权限，无法修改该用户！");
        }

        //用户信息校验成功
        boolean saveUser = userService.userUpdate(updateUser);
        return ResultUtil.success(saveUser);
    }

    /**
     * 删除用户
     * @param deleteUser 删除用户信息
     * @param httpServletRequest 当前用户信息
     * @return 结果
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> UserDelete(@RequestBody DeleteUser deleteUser, HttpServletRequest httpServletRequest) {
        if (deleteUser == null || deleteUser.getId() <= 0) {
            return ResultUtil.error(ErrorCode.PARAMS_ERROR,"不存在该用户,无法进行删除操作！");
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        //仅本人或管理员可以操作
        if (!loginUser.getId().equals(deleteUser.getId()) && !userService.isAdmin(httpServletRequest)) {
            return ResultUtil.error(ErrorCode.NOT_MATCH_ACCOUNT,"无法删除该用户！");
        }
        boolean removeUser = userService.removeById(deleteUser.getId());
        return ResultUtil.success(removeUser);
    }
    /**
     * todo 上传用户头像功能
     * @param upLoadUserAvatar 用户信息
     * @param httpServletRequest 当前用户信息
     * @return 结果
     */
    @PostMapping("/upload")
    public BaseResponse<Boolean> UserDelete(@RequestBody UpLoadUserAvatar upLoadUserAvatar, HttpServletRequest httpServletRequest) {
        if (upLoadUserAvatar == null || upLoadUserAvatar.getId() <= 0) {
            return ResultUtil.error(ErrorCode.PARAMS_ERROR,"不存在该用户,无法进行上传操作！");
        }
        User loginUser = userService.getLoginUser(httpServletRequest);
        //仅本人可以操作
        if (!loginUser.getId().equals(upLoadUserAvatar.getId())) {
            return ResultUtil.error(ErrorCode.NOT_MATCH_ACCOUNT,"无权限更换头像！");
        }
        loginUser.setUserAvatar(upLoadUserAvatar.getUserAvatar());
        boolean updateUser = userService.updateById(loginUser);
        return ResultUtil.success(updateUser);
    }
}
