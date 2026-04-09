package com.hiremate.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hiremate.ai.entity.InterviewQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试题目 Mapper
 */
@Mapper
public interface InterviewQuestionMapper extends BaseMapper<InterviewQuestion> {
}
