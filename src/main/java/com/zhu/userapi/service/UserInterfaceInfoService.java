package com.zhu.userapi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhu.apicommon.model.entity.UserInterfaceInfo;

/**
 * 用户接口信息服务
 *
 * @author zhu
 */
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    /**
     * 调用接口统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);
}
