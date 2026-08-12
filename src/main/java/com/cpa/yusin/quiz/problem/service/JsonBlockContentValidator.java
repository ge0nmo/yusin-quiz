package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.global.exception.ContentException;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class JsonBlockContentValidator {
    private static final String STATEMENT_GROUP_TYPE = "statementGroup";
    private static final int MAX_LABEL_LENGTH = 20;

    public void validate(List<Map<String, Object>> blocks) {
        if (blocks != null) {
            validateNestedValue(blocks);
        }
    }

    private void validateNestedValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            if (STATEMENT_GROUP_TYPE.equals(map.get("type"))) {
                validateStatementGroup(map);
            }
            map.values().forEach(this::validateNestedValue);
        } else if (value instanceof List<?> list) {
            list.forEach(this::validateNestedValue);
        }
    }

    private void validateStatementGroup(Map<?, ?> block) {
        Object itemsValue = block.get("items");
        if (!(itemsValue instanceof List<?> items) || items.isEmpty()) {
            throw invalidStatementGroup();
        }

        for (Object itemValue : items) {
            if (!(itemValue instanceof Map<?, ?> item)) {
                throw invalidStatementGroup();
            }

            Object labelValue = item.get("label");
            if (!(labelValue instanceof String label)
                    || label.strip().isEmpty()
                    || label.strip().length() > MAX_LABEL_LENGTH) {
                throw invalidStatementGroup();
            }

            Object contentValue = item.get("content");
            if (!(contentValue instanceof List<?> content)
                    || content.isEmpty()
                    || content.stream().anyMatch(entry -> !(entry instanceof Map<?, ?>))
                    || !containsMeaningfulContent(content)) {
                throw invalidStatementGroup();
            }
        }
    }

    private boolean containsMeaningfulContent(Object value) {
        if (value instanceof Map<?, ?> map) {
            Object type = map.get("type");
            if ("text".equals(type) && containsMeaningfulText(map)) {
                return true;
            }
            if ("image".equals(type) && map.get("src") instanceof String src && !src.isBlank()) {
                return true;
            }
            return map.values().stream().anyMatch(this::containsMeaningfulContent);
        }
        if (value instanceof List<?> list) {
            return list.stream().anyMatch(this::containsMeaningfulContent);
        }
        return false;
    }

    private boolean containsMeaningfulText(Map<?, ?> block) {
        if (block.get("text") instanceof String text && !text.isBlank()) {
            return true;
        }
        if (!(block.get("spans") instanceof List<?> spans)) {
            return false;
        }
        return spans.stream().anyMatch(span -> span instanceof Map<?, ?> spanMap
                && spanMap.get("text") instanceof String text
                && !text.isBlank());
    }

    private ContentException invalidStatementGroup() {
        return new ContentException(ExceptionMessage.INVALID_STATEMENT_GROUP);
    }
}
