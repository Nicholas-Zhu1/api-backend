package com.zhu.userapi.service.impl;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhu.apicommon.model.entity.User;
import com.zhu.userapi.common.CheckUser;
import com.zhu.userapi.common.ErrorCode;
import com.zhu.userapi.exception.BusinessException;
import com.zhu.userapi.mapper.UserMapper;
import com.zhu.userapi.model.domain.dto.User.UpdateUser;
import com.zhu.userapi.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zhu.userapi.contant.CommonConstant.SALT;
import static com.zhu.userapi.contant.UserConstant.USER_LOGIN_STATE;


/**
 * @author ZHU
 * description 针对表【user(用户)】的数据库操作Service实现
 * createDate
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 验证密码
     * @return 注册id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //校验账号和密码
        if (!CheckUser.check(userAccount, userPassword).equals(CheckUser.RIGHT_CODE)) {
            log.info("user register failed,userAccount or userPassword error");
            return CheckUser.check(userAccount, userPassword);
        }
        //校验密码与密码验证是否相同
        if (!userPassword.equals(checkPassword)) {
            return ErrorCode.PASSWORD_NOT_MATCH.getCode();
        }

        //查询数据库昵称是否重复（最后再查，节省资源占用）
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            return ErrorCode.ACCOUNT_EXISTED.getCode();
        }

        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        //自动生成签名认证
        String autoAccessKey = RandomUtil.randomNumbers(4) + userAccount + System.currentTimeMillis()/1000;
        String autoAccessKeyMd5 = MD5.create().digestHex(autoAccessKey);
        String autoSecretKey = RandomUtil.randomNumbers(4) + userAccount + System.currentTimeMillis()/1000 + SALT;
        String autoSecretKeyMd5 = MD5.create().digestHex(autoSecretKey);
        user.setAccessKey(autoAccessKeyMd5);
        user.setSecretKey(autoSecretKeyMd5);
        //初始用户名与用户账户名一致
        user.setUserName(userAccount);

        boolean saveResult = this.save(user);
        if (!saveResult) {
            return ErrorCode.SAVE_FAILED.getCode();
        }
        return CheckUser.RIGHT_CODE;
    }

    /**
     * @param userAccount        用户名
     * @param userPassword       密码
     * @param httpServletRequest 请求信息
     * @return 用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest httpServletRequest) {
        if (CheckUser.check(userAccount, userPassword)!=CheckUser.RIGHT_CODE) {
            return null;
        }
        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //查询账号是否存在（并且还要判断是否有效）
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (user == null) {
            log.info("user login failed,userAccount dose not match userPassword");
            return null;
        }
        //用户脱敏,隐藏敏感信息
        User safetyUser = this.getSafetyUser(user);
        //记录用户登录态
        httpServletRequest.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    /**
     * 新增用户
     *
     * @param newUser 新用户封装类
     * @return 是否添加成功
     */
    public Boolean userSave(User newUser) {
        if (!CheckUser.check(newUser.getUserAccount())) {
            log.info("user save failed,userAccount error");
            return false;
        }
        //查询账号是否存在（并且还要判断是否有效）
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", newUser.getUserAccount());
        Long count = userMapper.selectCount(queryWrapper);
        //用户已存在
        if (count > 0) {
            log.info("user has existed");
            return false;
        }
        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + newUser.getUserPassword()).getBytes());
        newUser.setUserPassword(encryptPassword);
        //自动生成签名认证
        String autoAccessKey = RandomUtil.randomNumbers(4) + newUser.getUserAccount() + System.currentTimeMillis()/1000;
        String autoAccessKeyMd5 = MD5.create().digestHex(autoAccessKey);
        String autoSecretKey = RandomUtil.randomNumbers(4) + newUser.getUserAccount() + System.currentTimeMillis()/1000 + SALT;
        String autoSecretKeyMd5 = MD5.create().digestHex(autoSecretKey);
        newUser.setAccessKey(autoAccessKeyMd5);
        newUser.setSecretKey(autoSecretKeyMd5);
        //初始用户名与用户账户名一致
        if (StringUtils.isBlank(newUser.getUserName())){
            newUser.setUserName(newUser.getUserAccount());
        }
        //保存用户
        return this.save(newUser);
    }

    @Override
    public Boolean userUpdate(UpdateUser newUser) {
        //根据用户id查询原来用户
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getId, newUser.getId());
        User oldUser = userMapper.selectOne(lambdaQueryWrapper);
        if (oldUser == null|| newUser.getUserAccount()==null) {
            return false;
        }
        BeanUtils.copyProperties(newUser,oldUser);
        int updateById = userMapper.updateById(oldUser);
        return updateById>0;
    }


    @Override
    public boolean isAdmin(HttpServletRequest httpServletRequest) {
        Object userObject = httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObject;
        return user != null && "admin".equals(user.getUserRole());
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        if (user == null || user.getId() <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = user.getId();
        user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;
    }

    /**
     * 用户查询
     * @param user
     * @param httpServletRequest
     * @return
     */
    @Override
    public List<User> listUser(User user, HttpServletRequest httpServletRequest) {
        if (httpServletRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //先查缓存
        User loginUser = this.getLoginUser(httpServletRequest);
        String redisKey = String.format("zhu:user:%s",loginUser.getId());
        ValueOperations<String,Object> valueOperation = redisTemplate.opsForValue();
        List<User> userList1 = (List<User>) valueOperation.get(redisKey);
        if (userList1!=null){
            return userList1;
        }
        //再查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(user);
        List<User> userList = this.list(queryWrapper);

        //写入缓存
        try {
            valueOperation.set(redisKey,userList,60000, TimeUnit.MILLISECONDS);
        }catch (Exception e){
            log.error("redis set key error",e);
        }
        return userList;
    }

    /**
     * 用户注销
     *
     * @param httpServletRequest 用户信息
     * @return 1 表示注销成功
     */
    @Override
    public int outLogin(HttpServletRequest httpServletRequest) {
        httpServletRequest.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 验证是否合法
     *
     * @param user 用户
     * @param add 验证用户是否已注册
     * @return 用户验证
     */
    @Override
    public Boolean validUser(User user, boolean add) {
        if (user == null) {
            return false;
        }
        // 创建时，所有参数必须非空
        if (add) {
            if (user.getId() <= 0) {
                return false;
            }
        }
        if (StringUtils.isEmpty(user.getUserPassword())) {
            return CheckUser.check(user.getUserAccount());
        }
        return CheckUser.check(user.getUserAccount(), user.getUserPassword()).equals(CheckUser.RIGHT_CODE);
    }

    @Override
    public User getSafetyUser(User user) {
        if (user == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUserName(user.getUserName());
        safetyUser.setUserAccount(user.getUserAccount());
        safetyUser.setUserAvatar(user.getUserAvatar());
        safetyUser.setGender(user.getGender());
        safetyUser.setUserStatus(user.getUserStatus());
        safetyUser.setUserRole(user.getUserRole());
        safetyUser.setCreateTime(user.getCreateTime());
        return safetyUser;
    }
}




