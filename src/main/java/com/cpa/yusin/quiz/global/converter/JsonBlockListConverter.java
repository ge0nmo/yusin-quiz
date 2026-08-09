package com.cpa.yusin.quiz.global.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Converter
public class JsonBlockListConverter implements AttributeConverter<List<Map<String, Object>>, String> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Map<String, Object>> blocks) {
        try {
            return OBJECT_MAPPER.writeValueAsString(blocks == null ? List.of() : blocks);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("블록 JSON을 저장할 수 없습니다.", exception);
        }
    }

    @Override
    public List<Map<String, Object>> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("저장된 블록 JSON을 읽을 수 없습니다.", exception);
        }
    }
}
