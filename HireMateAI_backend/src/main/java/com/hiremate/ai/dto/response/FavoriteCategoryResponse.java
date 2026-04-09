package com.hiremate.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收藏分类统计响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteCategoryResponse {

    private String interviewType;

    private String interviewTypeLabel;

    private Integer count;
}
