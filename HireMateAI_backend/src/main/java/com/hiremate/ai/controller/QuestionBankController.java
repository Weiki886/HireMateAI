package com.hiremate.ai.controller;

import com.hiremate.ai.common.Result;
import com.hiremate.ai.dto.request.GenerateQuestionsRequest;
import com.hiremate.ai.dto.response.CategoryResponse;
import com.hiremate.ai.dto.response.FavoriteCategoryResponse;
import com.hiremate.ai.dto.response.PageResponse;
import com.hiremate.ai.dto.response.QuestionResponse;
import com.hiremate.ai.entity.User;
import com.hiremate.ai.mapper.UserMapper;
import com.hiremate.ai.service.QuestionBankService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/question-bank")
@RequiredArgsConstructor
@Tag(name = "面试题库接口", description = "面试题库分类、题目查询、AI生成、收藏管理")
public class QuestionBankController {

    private final QuestionBankService questionBankService;
    private final UserMapper userMapper;

    @GetMapping("/categories")
    @Operation(summary = "获取题库分类列表")
    public Result<List<CategoryResponse>> getCategories() {
        questionBankService.initDefaultCategories();
        List<CategoryResponse> categories = questionBankService.getCategories();
        return Result.success(categories);
    }

    @GetMapping("/questions")
    @Operation(summary = "分页获取面试题目列表")
    public Result<PageResponse<QuestionResponse>> getQuestions(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String interviewType,
            @RequestParam(required = false) String targetPosition,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        PageResponse<QuestionResponse> result = questionBankService.getQuestions(
                userId, categoryId, keyword, difficulty, interviewType, targetPosition, page, size);
        return Result.success(result);
    }

    @GetMapping("/questions/{id}")
    @Operation(summary = "获取题目详情")
    public Result<QuestionResponse> getQuestionDetail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        QuestionResponse question = questionBankService.getQuestionDetail(userId, id);
        return Result.success(question);
    }

    @PostMapping("/questions")
    @Operation(summary = "AI 生成面试题目")
    public Result<List<QuestionResponse>> generateQuestions(@Valid @RequestBody GenerateQuestionsRequest request) {
        Long userId = getCurrentUserId();
        List<QuestionResponse> questions = questionBankService.generateQuestions(userId, request);
        return Result.success("题目生成成功", questions);
    }

    @PostMapping("/questions/favorite")
    @Operation(summary = "收藏/取消收藏题目")
    public Result<Map<String, Object>> toggleFavorite(
            @RequestParam Long questionId,
            @RequestParam(required = false) String note) {
        Long userId = getCurrentUserId();
        boolean isFavorited = questionBankService.toggleFavorite(userId, questionId, note);
        return Result.success(isFavorited ? "已收藏" : "已取消收藏",
                Map.of("isFavorited", isFavorited));
    }

    @GetMapping("/favorites")
    @Operation(summary = "获取我的收藏")
    public Result<PageResponse<QuestionResponse>> getFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        PageResponse<QuestionResponse> result = questionBankService.getFavorites(userId, page, size);
        return Result.success(result);
    }

    @GetMapping("/favorites/categories")
    @Operation(summary = "获取收藏分类统计（按面试类型分组）")
    public Result<List<FavoriteCategoryResponse>> getFavoriteCategories() {
        Long userId = getCurrentUserId();
        List<FavoriteCategoryResponse> categories = questionBankService.getFavoriteCategories(userId);
        return Result.success(categories);
    }

    @GetMapping("/favorites/by-type")
    @Operation(summary = "按面试类型获取收藏题目")
    public Result<PageResponse<QuestionResponse>> getFavoritesByType(
            @RequestParam(required = false) String interviewType,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        PageResponse<QuestionResponse> result = questionBankService.getFavoritesByType(
                userId, interviewType, difficulty, page, size);
        return Result.success(result);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));
        return user.getId();
    }
}
