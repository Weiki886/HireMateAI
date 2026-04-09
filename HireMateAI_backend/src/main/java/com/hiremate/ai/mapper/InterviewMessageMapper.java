package com.hiremate.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hiremate.ai.entity.InterviewMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试消息 Mapper
 */
@Mapper
public interface InterviewMessageMapper extends BaseMapper<InterviewMessage> {
}
