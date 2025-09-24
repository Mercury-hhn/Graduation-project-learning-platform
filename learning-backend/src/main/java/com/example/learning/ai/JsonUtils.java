package com.example.learning.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JSON 工具：用于解析 AI 返回的 JSON 字符串。
 */
public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Map<String, Object> parseJson(String json) {
        try {
            return MAPPER.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    public static List<Map<String, Object>> parseList(String json) {
        try {
            return MAPPER.readValue(json, List.class);
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}