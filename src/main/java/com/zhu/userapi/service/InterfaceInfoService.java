package com.zhu.userapi.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zhu.apicommon.model.entity.InterfaceInfo;
import org.springframework.stereotype.Service;

/**
 * 接口信息服务
 *
 * @author zhu
 */
@Service
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);
}
