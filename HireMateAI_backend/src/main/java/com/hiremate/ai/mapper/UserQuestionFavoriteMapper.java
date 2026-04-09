package com.hiremate.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hiremate.ai.entity.UserQuestionFavorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收藏题目 Mapper
 */
@Mapper
public interface UserQuestionFavoriteMapper extends BaseMapper<UserQuestionFavorite> {
}
