package com.zhu.userapi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhu.apicommon.model.entity.InterfaceInfo;
import com.zhu.userapi.common.ErrorCode;
import com.zhu.userapi.exception.BusinessException;
import com.zhu.userapi.mapper.InterfaceInfoMapper;
import com.zhu.userapi.service.InterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author ZHU
* @description 针对表【interface_info(接口信息)】的数据库操作Service实现
* @createDate 2023-09-04 09:45:45
*/
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
    implements InterfaceInfoService {

    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = interfaceInfo.getName();
        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(name)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (StringUtils.isNotBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }
        //校验url
        if (StringUtils.isNotBlank(interfaceInfo.getUrl())){
            Pattern pattern = Pattern.compile(
                    "(http|https|ftp)://((((25[0-5])|(2[0-4]\\d)|(1\\d{2})|([1-9]?\\d)\\.){3}" +
                            "((25[0-5])|(2[0-4]\\d)|(1\\d{2})|([1-9]?\\d)))|(([\\w-]+\\.)+" +
                            "(net|com|org|gov|edu|mil|info|travel|pro|museum|biz|[a-z]{2})))(/[\\w\\-~#]+)" +
                            "*(/[\\w-]+\\.[\\w]{2,4})?([\\?=&%_]?[\\w-]+)*\n");
            Matcher matcher = pattern.matcher(interfaceInfo.getUrl());
            if(!matcher.matches()){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "url不合法");
            }
        }
        if (StringUtils.isNotBlank(interfaceInfo.getMethod())){
            String[] method = {"get","post","delete","put"};
            String methodType = interfaceInfo.getMethod();
            Arrays.stream(method).map(item->{
                if (item.compareToIgnoreCase(methodType)==0){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求方式不合法");
                }
                return null;
            }).close();
            
        }
    }
}




