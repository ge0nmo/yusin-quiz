package com.cpa.yusin.quiz.global.converter;

import com.cpa.yusin.quiz.problem.domain.block.Block;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Converter
public class BlockListConverter implements AttributeConverter<List<Block>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Block> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert Block list to JSON", e);
            return "[]";
        }
    }

    @Override
    public List<Block> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty() || dbData.equals("[]")) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(dbData, new TypeReference<List<Block>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to convert JSON to Block list: {}", dbData, e);
            return new ArrayList<>();
        }
    }
}