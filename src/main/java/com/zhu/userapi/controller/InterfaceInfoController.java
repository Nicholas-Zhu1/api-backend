package com.zhu.userapi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.zhu.apicommon.model.entity.InterfaceInfo;
import com.zhu.apicommon.model.entity.User;
import com.zhu.apicommon.model.enums.InterfaceInfoStatusEnum;
import com.zhu.clientsdk.client.UserClient;
import com.zhu.userapi.annotation.AuthCheck;
import com.zhu.userapi.common.*;
import com.zhu.userapi.contant.CommonConstant;
import com.zhu.userapi.exception.BusinessException;
import com.zhu.userapi.model.domain.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.zhu.userapi.model.domain.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.zhu.userapi.model.domain.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.zhu.userapi.model.domain.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.zhu.userapi.service.InterfaceInfoService;
import com.zhu.userapi.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.zhu.userapi.contant.CommonConstant.ONLINE_STATUS;

/**
 * 接口管理
 *
 * @author zhu
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private UserClient userClient;

    // region 增删改查

    /**
     * 创建
     *
     * @param interfaceInfoAddRequest 接口添加
     * @param request 用户请求信息
     * @return 添加是否成功
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        // 校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
        boolean result = interfaceInfoService.save(interfaceInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtil.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest 删除接口
     * @param request 用户请求信息
     * @return 删除是否成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        CheckUser.check(userService, interfaceInfoService,request,id,user);
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtil.success(b);
    }

    /**
     * 更新
     *
     * @param interfaceInfoUpdateRequest 更新接口
     * @param request 用户信息
     * @return 更新是否成功
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest,
                                                     HttpServletRequest request) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        User user = userService.getLoginUser(request);
        long id = interfaceInfoUpdateRequest.getId();
        CheckUser.check(userService, interfaceInfoService,request,id,user);
        interfaceInfo.setId(id);
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtil.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id 接口id
     * @return 接口
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceInfo> getInterfaceInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        return ResultUtil.success(interfaceInfo);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param interfaceInfoQueryRequest 接口查询
     * @return 接口
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        if (interfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(queryWrapper);

        return ResultUtil.success(interfaceInfoList);
    }

    /**
     * 分页获取列表
     *
     * @param interfaceInfoQueryRequest 接口查询
     * @return 分页查询结果
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<InterfaceInfo>> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        String description = interfaceInfoQuery.getDescription();
        // description 需支持模糊搜索
        interfaceInfoQuery.setDescription(null);
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description).eq("status",ONLINE_STATUS);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size), queryWrapper);
        return ResultUtil.success(interfaceInfoPage);
    }

    // endregion

    /**
     * 发布
     *
     * @param idRequest 接口id
     * @return  返回是否上线成功
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        long id = CheckInterfaceById.checkId(idRequest,interfaceInfoService);
        // 判断该接口是否可以调用(测试接口是否可用)
        com.zhu.clientsdk.Model.User user = new com.zhu.clientsdk.Model.User();
        user.setName("test");
        String username = userClient.getNameByPost(user);
        if (!username.contains("test")) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口无法使用，验证失败");
        }
        // 仅管理员可修改
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtil.success(result);
    }

    /**
     * 下线
     *
     * @param idRequest 接口id
     * @return  返回是否下线成功
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        long id = CheckInterfaceById.checkId(idRequest,interfaceInfoService);
        // 仅管理员可修改
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtil.success(result);
    }

    /**
     * 测试调用
     *
     * @param interfaceInfoInvokeRequest  接口调用请求
     * @param request  请求
     * @return 返回该接口
     */
    @PostMapping("/invoke")
    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
                                                     HttpServletRequest request) {
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = interfaceInfoInvokeRequest.getId();
        String userRequestParams = interfaceInfoInvokeRequest.getUserRequestParams();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (oldInterfaceInfo.getStatus() == InterfaceInfoStatusEnum.OFFLINE.getValue()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口已关闭");
        }
        User loginUser = userService.getLoginUser(request);
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        UserClient tempClient = new UserClient(accessKey, secretKey);
        Gson gson = new Gson();
        com.zhu.clientsdk.Model.User user = gson.fromJson(userRequestParams, com.zhu.clientsdk.Model.User.class);

        String usernameByPost = tempClient.getNameByPost(user);
        return ResultUtil.success(usernameByPost);
    }

}
