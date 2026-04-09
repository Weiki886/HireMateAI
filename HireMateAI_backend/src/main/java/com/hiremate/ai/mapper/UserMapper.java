package com.hiremate.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hiremate.ai.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
