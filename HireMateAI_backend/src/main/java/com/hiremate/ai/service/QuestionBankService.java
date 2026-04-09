package com.hiremate.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiremate.ai.common.BusinessException;
import com.hiremate.ai.common.ResultCode;
import com.hiremate.ai.dto.request.GenerateQuestionsRequest;
import com.hiremate.ai.dto.response.CategoryResponse;
import com.hiremate.ai.dto.response.FavoriteCategoryResponse;
import com.hiremate.ai.dto.response.PageResponse;
import com.hiremate.ai.dto.response.QuestionResponse;
import com.hiremate.ai.entity.*;
import com.hiremate.ai.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionBankService {

    private final QuestionBankCategoryMapper categoryMapper;
    private final InterviewQuestionMapper questionMapper;
    private final UserQuestionFavoriteMapper favoriteMapper;
    private final UserMapper userMapper;
    private final AIChatService aiChatService;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Map<String, String> DIFFICULTY_LABELS = Map.of(
            "EASY", "简单", "MEDIUM", "中等", "HARD", "困难"
    );

    private static final Map<String, String> TYPE_LABELS = Map.of(
            "POSITION", "按岗位", "BEHAVIOR", "行为问题", "TECH", "技术问题", "HR", "HR问题"
    );

    private static final Map<String, String> INTERVIEW_TYPE_LABELS = Map.of(
            "综合面试", "综合面试",
            "技术面试", "技术面试",
            "HR面试", "HR面试",
            "行为面试", "行为面试",
            "终面", "终面"
    );

    private static final List<String> INTERVIEW_TYPE_ORDER = List.of(
            "技术面试", "行为面试", "HR面试", "综合面试", "终面"
    );

    public List<CategoryResponse> getCategories() {
        List<QuestionBankCategory> allCategories = categoryMapper.selectAllOrdered();
        return buildCategoryTree(allCategories);
    }

    @Transactional
    public void initDefaultCategories() {
        long count = categoryMapper.selectCount(null);
        if (count > 0) {
            return;
        }

        List<QuestionBankCategory> defaults = Arrays.asList(
                QuestionBankCategory.builder()
                        .parentId(0L).name("技术面试").type("TECH")
                        .sortOrder(1).description("编程语言、算法、系统设计等技术问题")
                        .createdAt(LocalDateTime.now()).build(),
                QuestionBankCategory.builder()
                        .parentId(0L).name("行为面试").type("BEHAVIOR")
                        .sortOrder(2).description("STAR法则、过往经历、团队合作等行为问题")
                        .createdAt(LocalDateTime.now()).build(),
                QuestionBankCategory.builder()
                        .parentId(0L).name("HR面试").type("HR")
                        .sortOrder(3).description("薪资期望、职业规划、离职原因等HR问题")
                        .createdAt(LocalDateTime.now()).build(),
                QuestionBankCategory.builder()
                        .parentId(0L).name("综合面试").type("POSITION")
                        .sortOrder(4).description("按岗位分类的综合面试题")
                        .createdAt(LocalDateTime.now()).build()
        );
        defaults.forEach(categoryMapper::insert);
    }

    public PageResponse<QuestionResponse> getQuestions(Long userId, Long categoryId, String keyword,
                                                       String difficulty, String interviewType,
                                                       String targetPosition, int page, int size) {
        initDefaultCategories();

        LambdaQueryWrapper<InterviewQuestion> qw = new LambdaQueryWrapper<>();
        qw.and(w -> w
                .eq(InterviewQuestion::getIsPublic, 1)
                .or()
                .eq(InterviewQuestion::getUserId, userId)
        );

        if (categoryId != null && categoryId > 0) {
            QuestionBankCategory root = findRootCategory(categoryId);
            Set<Long> categoryIds = resolveEquivalentCategoryIds(categoryId);

            if (root != null && categoryIds.size() == 1) {
                // 根分类：纳入等价 id（通常为1） 以及 category_id 为空但 interview_type 匹配的题目
                Long eqId = categoryIds.iterator().next();
                String typeForFallback = root.getType();
                qw.and(w -> w
                        .eq(InterviewQuestion::getCategoryId, eqId)
                        .or()
                        .isNull(InterviewQuestion::getCategoryId)
                        .eq(InterviewQuestion::getInterviewType, typeForFallback)
                );
            } else if (categoryIds.size() > 1) {
                qw.in(InterviewQuestion::getCategoryId, categoryIds);
            } else {
                qw.eq(InterviewQuestion::getCategoryId, categoryIds.iterator().next());
            }
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w
                    .like(InterviewQuestion::getQuestionText, keyword)
                    .or()
                    .like(InterviewQuestion::getTags, keyword)
            );
        }
        if (difficulty != null && !difficulty.isBlank()) {
            qw.eq(InterviewQuestion::getDifficulty, difficulty.toUpperCase());
        }
        if (interviewType != null && !interviewType.isBlank()) {
            qw.eq(InterviewQuestion::getInterviewType, interviewType);
        }
        if (targetPosition != null && !targetPosition.isBlank()) {
            qw.like(InterviewQuestion::getTargetPosition, targetPosition);
        }

        qw.orderByDesc(InterviewQuestion::getViewCount)
          .orderByDesc(InterviewQuestion::getCreatedAt);

        Page<InterviewQuestion> pageResult = questionMapper.selectPage(
                new Page<>(page, size), qw);

        // Get favorite question IDs for this user
        Set<Long> favoriteIds = getFavoriteIds(userId);

        // Build category map
        Map<Long, String> categoryNameMap = getCategoryNameMap();

        List<QuestionResponse> records = pageResult.getRecords().stream()
                .map(q -> buildQuestionResponse(q, favoriteIds, categoryNameMap))
                .collect(Collectors.toList());

        return PageResponse.<QuestionResponse>builder()
                .records(records)
                .total(pageResult.getTotal())
                .page((long) page)
                .size((long) size)
                .build();
    }

    @Transactional
    public List<QuestionResponse> generateQuestions(Long userId, GenerateQuestionsRequest request) {
        initDefaultCategories();

        String prompt = buildGeneratePrompt(request);
        List<AIChatService.ChatMessage> history = Collections.emptyList();
        String aiResponse = aiChatService.chat(history, prompt, null, null, null);

        try {
            String json = extractJson(aiResponse);
            JsonNode root = objectMapper.readTree(json);

            List<QuestionResponse> generated = new ArrayList<>();
            JsonNode questionsNode = root.has("questions") ? root.get("questions") : root;
            if (questionsNode.isArray()) {
                for (JsonNode qNode : questionsNode) {
                    String questionText = getText(qNode, "question");
                    if (questionText.isBlank()) continue;

                    InterviewQuestion question = InterviewQuestion.builder()
                            .categoryId(request.getCategoryId())
                            .questionText(questionText)
                            .answerPoints(objectMapper.writeValueAsString(getList(qNode, "answerPoints")))
                            .modelAnswer(getText(qNode, "modelAnswer"))
                            .difficulty(request.getDifficulty() != null ? request.getDifficulty().toUpperCase() : "MEDIUM")
                            .interviewType(request.getInterviewType())
                            .targetPosition(request.getJobPosition())
                            .tags(getText(qNode, "tags"))
                            .viewCount(0)
                            .userId(userId)
                            .isPublic(1)
                            .source("AI")
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    questionMapper.insert(question);

                    QuestionResponse qr = buildQuestionResponse(question, Collections.emptySet(), getCategoryNameMap());
                    generated.add(qr);
                }
            }

            return generated;

        } catch (Exception e) {
            log.error("Failed to parse AI generated questions: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "生成题目失败：" + e.getMessage());
        }
    }

    public QuestionResponse getQuestionDetail(Long userId, Long questionId) {
        InterviewQuestion question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "题目不存在");
        }

        // Check access
        if (question.getIsPublic() == 0 && !userId.equals(question.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问此题目");
        }

        // Increment view count
        question.setViewCount((question.getViewCount() == null ? 0 : question.getViewCount()) + 1);
        questionMapper.updateById(question);

        Set<Long> favoriteIds = getFavoriteIds(userId);
        Map<Long, String> categoryNameMap = getCategoryNameMap();
        return buildQuestionResponse(question, favoriteIds, categoryNameMap);
    }

    @Transactional
    public boolean toggleFavorite(Long userId, Long questionId, String note) {
        InterviewQuestion question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "题目不存在");
        }

        UserQuestionFavorite existing = favoriteMapper.selectOne(
                new LambdaQueryWrapper<UserQuestionFavorite>()
                        .eq(UserQuestionFavorite::getUserId, userId)
                        .eq(UserQuestionFavorite::getQuestionId, questionId));

        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return false;
        } else {
            UserQuestionFavorite favorite = UserQuestionFavorite.builder()
                    .userId(userId)
                    .questionId(questionId)
                    .note(note)
                    .createdAt(LocalDateTime.now())
                    .build();
            favoriteMapper.insert(favorite);
            return true;
        }
    }

    public PageResponse<QuestionResponse> getFavorites(Long userId, int page, int size) {
        Page<UserQuestionFavorite> favPage = favoriteMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<UserQuestionFavorite>()
                        .eq(UserQuestionFavorite::getUserId, userId)
                        .orderByDesc(UserQuestionFavorite::getCreatedAt));

        List<Long> questionIds = favPage.getRecords().stream()
                .map(UserQuestionFavorite::getQuestionId)
                .collect(Collectors.toList());

        Map<Long, String> categoryNameMap = getCategoryNameMap();

        if (questionIds.isEmpty()) {
            return PageResponse.<QuestionResponse>builder()
                    .records(Collections.emptyList())
                    .total(0L)
                    .page((long) page)
                    .size((long) size)
                    .build();
        }

        List<InterviewQuestion> questions = questionMapper.selectBatchIds(questionIds);
        Map<Long, InterviewQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(InterviewQuestion::getId, q -> q));

        List<QuestionResponse> records = questionIds.stream()
                .map(id -> {
                    InterviewQuestion q = questionMap.get(id);
                    if (q == null) return null;
                    return buildQuestionResponse(q, new java.util.HashSet<>(questionIds), categoryNameMap);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return PageResponse.<QuestionResponse>builder()
                .records(records)
                .total(favPage.getTotal())
                .page((long) page)
                .size((long) size)
                .build();
    }

    /**
     * 按面试类型获取用户收藏题目（支持难度筛选）
     */
    public PageResponse<QuestionResponse> getFavoritesByType(Long userId, String interviewType,
                                                               String difficulty, int page, int size) {
        // 查询用户的收藏题目ID
        List<UserQuestionFavorite> favorites = favoriteMapper.selectList(
                new LambdaQueryWrapper<UserQuestionFavorite>()
                        .eq(UserQuestionFavorite::getUserId, userId));

        if (favorites.isEmpty()) {
            return PageResponse.<QuestionResponse>builder()
                    .records(Collections.emptyList())
                    .total(0L)
                    .page((long) page)
                    .size((long) size)
                    .build();
        }

        List<Long> favoriteQuestionIds = favorites.stream()
                .map(UserQuestionFavorite::getQuestionId)
                .collect(Collectors.toList());

        // 查询符合条件的题目
        LambdaQueryWrapper<InterviewQuestion> qw = new LambdaQueryWrapper<>();
        qw.in(InterviewQuestion::getId, favoriteQuestionIds);

        if (interviewType != null && !interviewType.isBlank()) {
            qw.eq(InterviewQuestion::getInterviewType, interviewType);
        }

        if (difficulty != null && !difficulty.isBlank()) {
            qw.eq(InterviewQuestion::getDifficulty, difficulty.toUpperCase());
        }

        qw.orderByDesc(InterviewQuestion::getCreatedAt);

        Page<InterviewQuestion> pageResult = questionMapper.selectPage(
                new Page<>(page, size), qw);

        Set<Long> favoriteIds = getFavoriteIds(userId);
        Map<Long, String> categoryNameMap = getCategoryNameMap();

        List<QuestionResponse> records = pageResult.getRecords().stream()
                .map(q -> buildQuestionResponse(q, favoriteIds, categoryNameMap))
                .collect(Collectors.toList());

        return PageResponse.<QuestionResponse>builder()
                .records(records)
                .total(pageResult.getTotal())
                .page((long) page)
                .size((long) size)
                .build();
    }

    // --- Private helper methods ---

    private List<CategoryResponse> buildCategoryTree(List<QuestionBankCategory> all) {
        Map<Long, List<QuestionBankCategory>> childrenByParentId = all.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() > 0)
                .collect(Collectors.groupingBy(QuestionBankCategory::getParentId));

        Map<String, Long> canonicalRootIdByKey = canonicalRootIdByKey(all);
        Map<Long, List<QuestionBankCategory>> mergedChildrenByCanonical =
                mergeChildrenOfDuplicateRoots(all, childrenByParentId, canonicalRootIdByKey);

        return all.stream()
                .filter(c -> isRootCategory(c))
                .filter(c -> c.getId().equals(canonicalRootIdByKey.get(rootDedupeKey(c))))
                .sorted(Comparator
                        .comparing(QuestionBankCategory::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(QuestionBankCategory::getId))
                .map(c -> buildCategoryResponse(c, mergedChildrenByCanonical))
                .collect(Collectors.toList());
    }

    private boolean isRootCategory(QuestionBankCategory c) {
        return c.getParentId() == null || c.getParentId() == 0;
    }

    /** 根分类去重键：同 type + 同 name 视为重复（常见于多次初始化写入） */
    private static String rootDedupeKey(QuestionBankCategory c) {
        String type = c.getType() != null ? c.getType() : "";
        String name = c.getName() != null ? c.getName() : "";
        return type + "\0" + name;
    }

    /** 每组重复根分类保留 id 最小的一条作为展示与筛选入口 */
    private Map<String, Long> canonicalRootIdByKey(List<QuestionBankCategory> all) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (QuestionBankCategory c : all) {
            if (!isRootCategory(c)) {
                continue;
            }
            String key = rootDedupeKey(c);
            map.merge(key, c.getId(), (a, b) -> a <= b ? a : b);
        }
        return map;
    }

    private Map<Long, List<QuestionBankCategory>> mergeChildrenOfDuplicateRoots(
            List<QuestionBankCategory> all,
            Map<Long, List<QuestionBankCategory>> childrenByParentId,
            Map<String, Long> canonicalRootIdByKey) {
        Map<Long, List<QuestionBankCategory>> merged = new HashMap<>();
        for (QuestionBankCategory root : all) {
            if (!isRootCategory(root)) {
                continue;
            }
            Long canonicalId = canonicalRootIdByKey.get(rootDedupeKey(root));
            if (canonicalId == null) {
                continue;
            }
            merged.computeIfAbsent(canonicalId, k -> new ArrayList<>())
                    .addAll(childrenByParentId.getOrDefault(root.getId(), Collections.emptyList()));
        }
        return merged;
    }

    /**
     * 查找传入分类 id 对应的根分类（若本身是根分类则返回自身）。
     */
    private QuestionBankCategory findRootCategory(Long categoryId) {
        List<QuestionBankCategory> all = categoryMapper.selectAllOrdered();
        QuestionBankCategory selected = all.stream()
                .filter(c -> categoryId.equals(c.getId()))
                .findFirst()
                .orElse(null);
        if (selected == null) {
            return null;
        }
        if (isRootCategory(selected)) {
            return selected;
        }
        // 子分类向上找到根
        Long currentParentId = selected.getParentId();
        while (currentParentId != null && currentParentId != 0) {
            final Long pid = currentParentId;
            QuestionBankCategory parent = all.stream()
                    .filter(c -> pid.equals(c.getId()))
                    .findFirst()
                    .orElse(null);
            if (parent == null) {
                break;
            }
            if (isRootCategory(parent)) {
                return parent;
            }
            currentParentId = parent.getParentId();
        }
        return null;
    }

    /**
     * 与某分类 ID 等价的全部 category_id（用于题目筛选，避免重复根分类下题目分散在不同 id 上）。
     */
    private Set<Long> resolveEquivalentCategoryIds(Long categoryId) {
        List<QuestionBankCategory> all = categoryMapper.selectAllOrdered();
        QuestionBankCategory selected = all.stream()
                .filter(c -> categoryId.equals(c.getId()))
                .findFirst()
                .orElse(null);
        if (selected == null) {
            return Collections.singleton(categoryId);
        }
        if (isRootCategory(selected)) {
            String key = rootDedupeKey(selected);
            return all.stream()
                    .filter(this::isRootCategory)
                    .filter(c -> key.equals(rootDedupeKey(c)))
                    .map(QuestionBankCategory::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return Collections.singleton(categoryId);
    }

    private CategoryResponse buildCategoryResponse(QuestionBankCategory c,
                                                    Map<Long, List<QuestionBankCategory>> childrenMap) {
        List<CategoryResponse> children = childrenMap.getOrDefault(c.getId(), Collections.emptyList())
                .stream()
                .map(child -> buildCategoryResponse(child, childrenMap))
                .collect(Collectors.toList());

        return CategoryResponse.builder()
                .id(c.getId())
                .parentId(c.getParentId())
                .name(c.getName())
                .description(c.getDescription())
                .sortOrder(c.getSortOrder())
                .type(c.getType())
                .typeLabel(TYPE_LABELS.getOrDefault(c.getType(), c.getType()))
                .children(children.isEmpty() ? null : children)
                .build();
    }

    private QuestionResponse buildQuestionResponse(InterviewQuestion q, Set<Long> favoriteIds,
                                                    Map<Long, String> categoryNameMap) {
        List<String> answerPoints = parseJsonArray(q.getAnswerPoints());
        List<String> tags = parseJsonArray(q.getTags());

        return QuestionResponse.builder()
                .id(q.getId())
                .categoryId(q.getCategoryId())
                .categoryName(categoryNameMap.getOrDefault(q.getCategoryId(), ""))
                .questionText(q.getQuestionText())
                .answerPoints(answerPoints)
                .modelAnswer(q.getModelAnswer())
                .difficulty(q.getDifficulty())
                .difficultyLabel(DIFFICULTY_LABELS.getOrDefault(q.getDifficulty(), q.getDifficulty()))
                .interviewType(q.getInterviewType())
                .targetPosition(q.getTargetPosition())
                .targetIndustry(q.getTargetIndustry())
                .tags(tags)
                .viewCount(q.getViewCount())
                .source(q.getSource())
                .isFavorited(favoriteIds.contains(q.getId()))
                .createdAt(q.getCreatedAt() != null ? q.getCreatedAt().format(DATE_FORMATTER) : null)
                .build();
    }

    private Set<Long> getFavoriteIds(Long userId) {
        return favoriteMapper.selectList(
                new LambdaQueryWrapper<UserQuestionFavorite>()
                        .eq(UserQuestionFavorite::getUserId, userId))
                .stream()
                .map(UserQuestionFavorite::getQuestionId)
                .collect(Collectors.toSet());
    }

    private Map<Long, String> getCategoryNameMap() {
        return categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(QuestionBankCategory::getId, QuestionBankCategory::getName, (a, b) -> a));
    }

    private String buildGeneratePrompt(GenerateQuestionsRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业的面试题库专家，请根据以下要求生成面试题目。\n\n");
        if (request.getJobPosition() != null && !request.getJobPosition().isBlank()) {
            sb.append("目标岗位：").append(request.getJobPosition()).append("\n");
        }
        if (request.getInterviewType() != null && !request.getInterviewType().isBlank()) {
            sb.append("面试类型：").append(request.getInterviewType()).append("\n");
        }
        if (request.getDifficulty() != null && !request.getDifficulty().isBlank()) {
            sb.append("难度要求：").append(request.getDifficulty()).append("\n");
        } else {
            sb.append("难度要求：中等\n");
        }
        int count = request.getCount() != null ? request.getCount() : 5;
        sb.append("生成数量：").append(count).append("道题目\n\n");
        sb.append("请生成JSON格式的题目列表，格式如下：\n");
        sb.append("{\n");
        sb.append("  \"questions\": [\n");
        sb.append("    {\n");
        sb.append("      \"question\": \"题目内容\",\n");
        sb.append("      \"answerPoints\": [\"要点1\", \"要点2\"],\n");
        sb.append("      \"modelAnswer\": \"参考示范回答（100-300字）\",\n");
        sb.append("      \"tags\": \"标签1,标签2\"\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n\n");
        sb.append("注意：直接返回JSON，不要任何前缀说明文字。");
        return sb.toString();
    }

    private String extractJson(String text) {
        if (text == null || text.isBlank()) {
            return "{\"questions\":[]}";
        }
        text = text.trim();
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return "{\"questions\":[]}";
    }

    private String getText(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n != null && !n.isNull()) ? n.asText().trim() : "";
    }

    private List<String> getList(JsonNode node, String field) {
        JsonNode n = node.get(field);
        if (n == null || !n.isArray()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (JsonNode item : n) {
            if (item != null && !item.isNull()) {
                result.add(item.asText().trim());
            }
        }
        return result;
    }

    private List<String> parseJsonArray(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 获取用户收藏题目按面试类型分组统计
     */
    public List<FavoriteCategoryResponse> getFavoriteCategories(Long userId) {
        // 查询用户收藏的题目及其面试类型
        List<UserQuestionFavorite> favorites = favoriteMapper.selectList(
                new LambdaQueryWrapper<UserQuestionFavorite>()
                        .eq(UserQuestionFavorite::getUserId, userId));

        if (favorites.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = favorites.stream()
                .map(UserQuestionFavorite::getQuestionId)
                .collect(Collectors.toList());

        List<InterviewQuestion> questions = questionMapper.selectBatchIds(questionIds);

        // 按面试类型分组统计
        Map<String, Long> countMap = questions.stream()
                .filter(q -> q.getInterviewType() != null && !q.getInterviewType().isBlank())
                .collect(Collectors.groupingBy(
                        InterviewQuestion::getInterviewType,
                        Collectors.counting()
                ));

        // 构建响应列表，按固定顺序排列
        return INTERVIEW_TYPE_ORDER.stream()
                .filter(type -> countMap.containsKey(type) && countMap.get(type) > 0)
                .map(type -> FavoriteCategoryResponse.builder()
                        .interviewType(type)
                        .interviewTypeLabel(INTERVIEW_TYPE_LABELS.getOrDefault(type, type))
                        .count(countMap.get(type).intValue())
                        .build())
                .collect(Collectors.toList());
    }

}
