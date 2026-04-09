package com.hiremate.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 题库分类响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private Long id;

    private Long parentId;

    private String name;

    private String description;

    private Integer sortOrder;

    private String type;

    private String typeLabel;

    private List<CategoryResponse> children;
}
