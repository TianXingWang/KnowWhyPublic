package com.txwang.knowwhy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.txwang.knowwhy.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @author wangtianxing
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2025-03-04 00:13:34
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




