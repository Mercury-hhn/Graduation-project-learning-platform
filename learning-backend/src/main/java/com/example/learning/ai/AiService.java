package com.example.learning.ai;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 服务：封装 Spring AI 调用，可在 mock 模式下返回固定结果，保障系统可演示。
 */
@Service
public class AiService {

    private final ChatClient chatClient;
    private final boolean mock;

    public AiService(ChatClient chatClient, @Value("${ai.mock-enabled:true}") boolean mock) {
        this.chatClient = chatClient;
        this.mock = mock;
    }

    /**
     * 作业批改，返回得分与评语。
     */
    public GradeResult grade(String content, String rubric, int maxScore) {
        if (mock) {
            return new GradeResult(85, "Mock：论证清晰，建议补充示例。");
        }
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是严格但公平的阅卷老师，请返回 JSON：{\"score\":int,\"comment\":string}，评分范围 0-" + maxScore));
        messages.add(new UserMessage("评分标准：" + (rubric == null ? "无" : rubric) + "\n学生答案：" + content));
        String raw = chatClient.call(new Prompt(messages)).getResult().getOutput().getContent();
        Map<String, Object> map = JsonUtils.parseJson(raw);
        int score = ((Number) map.getOrDefault("score", 0)).intValue();
        String comment = map.getOrDefault("comment", "AI 无评语").toString();
        return new GradeResult(score, comment);
    }

    /**
     * 学习助手对话，传入历史对话以保持上下文。
     */
    public String assistantChat(Long courseId, String message, List<Map<String, String>> history) {
        if (mock) {
            return "Mock：这是关于课程" + courseId + "的学习建议。";
        }
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是在线学习助手，请使用中文回复，回答与课程相关的问题。"));
        if (history != null) {
            for (Map<String, String> item : history) {
                String role = item.getOrDefault("role", "user");
                String text = item.getOrDefault("text", "");
                if ("ai".equalsIgnoreCase(role) || "assistant".equalsIgnoreCase(role)) {
                    messages.add(new AssistantMessage(text));
                } else {
                    messages.add(new UserMessage(text));
                }
            }
        }
        messages.add(new UserMessage("课程ID:" + courseId + "\n问题:" + message));
        return chatClient.call(new Prompt(messages)).getResult().getOutput().getContent();
    }

    /**
     * AI 推荐重排。
     */
    public List<Map<String, Object>> rerank(List<Map<String, Object>> candidates) {
        if (mock) {
            return candidates;
        }
        // 简化处理：真实调用可根据分数排序，这里直接返回原始列表。
        return candidates;
    }

    /**
     * 题目生成，返回题目列表（兼容 Mock）。
     */
    public List<Map<String, Object>> generateQuiz(Long sectionId, List<String> types, int count) {
        if (mock) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                Map<String, Object> item = new HashMap<>();
                item.put("stem", "Mock 题目 " + i + "：请简述章节" + sectionId + "的核心概念。");
                item.put("type", types.isEmpty() ? "text" : types.get(0));
                item.put("options", List.of());
                item.put("answer", "参考答案");
                list.add(item);
            }
            return list;
        }
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你是专业命题老师，请输出 JSON 数组，每个元素包含 type, stem, options, answer。"));
        messages.add(new UserMessage("章节ID:" + sectionId + " 类型:" + types + " 数量:" + count));
        String raw = chatClient.call(new Prompt(messages)).getResult().getOutput().getContent();
        return JsonUtils.parseList(raw);
    }

    /**
     * 批改结果记录。
     */
    public record GradeResult(int score, String comment) {
    }
}