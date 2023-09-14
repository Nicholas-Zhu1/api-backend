package com.zhu.userapi.common;

import com.zhu.apicommon.model.entity.InterfaceInfo;
import com.zhu.apicommon.model.entity.User;
import com.zhu.userapi.exception.BusinessException;
import com.zhu.userapi.service.InterfaceInfoService;
import com.zhu.userapi.service.UserService;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckUser {

    public static final Integer RIGHT_CODE = 200;
    public static Integer check(String userAccount, String userPassword) {
        //校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ErrorCode.NULL_ACCOUNT_ERROR.getCode();
        }
        //校验账户昵称长度和密码长度
        if (userAccount.length() < 4) {
            return ErrorCode.ACCOUNT_LENGTH_SHORT.getCode();
        }
        if (userPassword.length() < 8) {
            return ErrorCode.PASSWORD_LENGTH_SHORT.getCode();
        }
        //正则表达式验证账号,包含特殊字符,应该返回false才符合正则表达式
        String validPattern = "[`~!@#$%^&()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return ErrorCode.ACCOUNT_INELIGIBLE.getCode();
        }
        return RIGHT_CODE;
    }
    public static Boolean check(String userAccount) {
        //校验
        if (StringUtils.isAnyBlank(userAccount)) {
            return false;
        }
        //校验账户昵称长度和密码长度
        if (userAccount.length() < 4) {
            return false;
        }
        //正则表达式验证账号,包含特殊字符,应该返回false才符合正则表达式
        String validPattern = "[`~!@#$%^&()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        return !matcher.find();
    }
    public static void check(UserService userService, InterfaceInfoService interfaceInfoService,HttpServletRequest request,long id,User user ) {
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }
}
