package com.zhu.userapi.common;

import com.zhu.apicommon.model.entity.InterfaceInfo;
import com.zhu.userapi.exception.BusinessException;
import com.zhu.userapi.service.InterfaceInfoService;


public class CheckInterfaceById {

    public static long checkId(IdRequest idRequest,InterfaceInfoService interfaceInfoService) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = idRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return id;
    }
}
