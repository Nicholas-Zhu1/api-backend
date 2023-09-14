package com.zhu.userapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhu.apicommon.model.entity.UserInterfaceInfo;

import java.util.List;

/**
 * 用户接口信息 Mapper
 *
 * @author zhu
 */
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {

    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);
}




