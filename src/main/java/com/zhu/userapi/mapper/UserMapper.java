package com.zhu.userapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhu.apicommon.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @author ZHU
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2023-02-02 15:39:47
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




