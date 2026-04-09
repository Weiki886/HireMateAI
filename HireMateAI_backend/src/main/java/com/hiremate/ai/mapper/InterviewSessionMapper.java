package com.hiremate.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hiremate.ai.entity.InterviewSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 面试会话 Mapper
 */
@Mapper
public interface InterviewSessionMapper extends BaseMapper<InterviewSession> {

    @Select("SELECT COUNT(*) FROM interview_message WHERE session_id = #{sessionId}")
    int countMessagesBySessionId(@Param("sessionId") Long sessionId);
}
