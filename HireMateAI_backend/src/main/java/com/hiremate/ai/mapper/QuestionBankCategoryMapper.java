package com.hiremate.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hiremate.ai.entity.QuestionBankCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 题库分类 Mapper
 */
@Mapper
public interface QuestionBankCategoryMapper extends BaseMapper<QuestionBankCategory> {

    @Select("SELECT * FROM question_bank_category ORDER BY sort_order ASC, id ASC")
    List<QuestionBankCategory> selectAllOrdered();
}
