package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.global.exception.ContentException;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class JsonBlockContentValidator {
    private static final String STATEMENT_GROUP_TYPE = "statementGroup";
    private static final String TABLE_TYPE = "table";
    private static final String TEXT_TYPE = "text";
    private static final int MAX_LABEL_LENGTH = 20;
    private static final int MAX_TABLE_SPAN = 1000;
    private static final int MAX_COLUMN_WIDTH = 10000;
    private static final Set<String> TABLE_BORDERS = Set.of("all", "outer", "none");

    public void validate(List<Map<String, Object>> blocks) {
        if (blocks != null) {
            validateNestedValue(blocks);
        }
    }

    private void validateNestedValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Object type = map.get("type");
            if (STATEMENT_GROUP_TYPE.equals(type)) {
                validateStatementGroup(map);
            } else if (TABLE_TYPE.equals(type)) {
                validateTable(map);
            } else if (TEXT_TYPE.equals(type)) {
                validateMathSpans(map);
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

    /**
     * 병합으로 가려진 칸은 cells에 넣지 않으므로(HTML table 규칙) 행마다 칸 수가 다를 수 있고, 빈 행과 빈 칸도 허용한다.
     */
    private void validateTable(Map<?, ?> block) {
        if (!(block.get("rows") instanceof List<?> rows) || rows.isEmpty()) {
            throw invalidTable();
        }

        int cellCount = 0;
        for (Object rowValue : rows) {
            if (!(rowValue instanceof Map<?, ?> row) || !(row.get("cells") instanceof List<?> cells)) {
                throw invalidTable();
            }
            for (Object cellValue : cells) {
                if (!(cellValue instanceof Map<?, ?> cell)
                        || !(cell.get("content") instanceof List<?> content)
                        || content.stream().anyMatch(entry -> !(entry instanceof Map<?, ?>))
                        || !isOptionalBoolean(cell.get("header"))
                        || !isOptionalSpan(cell.get("colspan"))
                        || !isOptionalSpan(cell.get("rowspan"))) {
                    throw invalidTable();
                }
                cellCount++;
            }
        }
        if (cellCount == 0
                || !isOptionalColumnWidths(block.get("columnWidths"))
                || !isOptionalBorder(block.get("border"))) {
            throw invalidTable();
        }
    }

    /** 열 너비(px)는 열마다 하나씩 양의 정수다. 화면에서는 비율로만 쓰므로 열 수와의 일치는 소비자가 맞춘다. */
    private boolean isOptionalColumnWidths(Object value) {
        if (value == null) {
            return true;
        }
        return value instanceof List<?> widths
                && !widths.isEmpty()
                && widths.size() <= MAX_TABLE_SPAN
                && widths.stream().allMatch(width -> isIntegerBetween(width, 1, MAX_COLUMN_WIDTH));
    }

    /** 테두리는 all(격자, 기본값)·outer(바깥만)·none(없음) 중 하나다. 생략하면 소비자가 all로 그린다. */
    private boolean isOptionalBorder(Object value) {
        return value == null || (value instanceof String border && TABLE_BORDERS.contains(border));
    }

    /** 수식 span은 LaTeX 원문을 text에 둔다. math를 모르는 소비자도 원문을 표시하므로 빈 수식은 거부한다. */
    private void validateMathSpans(Map<?, ?> block) {
        if (!(block.get("spans") instanceof List<?> spans)) {
            return;
        }
        for (Object spanValue : spans) {
            if (!(spanValue instanceof Map<?, ?> span) || !isOptionalBoolean(span.get("math"))) {
                throw invalidMath();
            }
            if (Boolean.TRUE.equals(span.get("math"))
                    && !(span.get("text") instanceof String text && !text.isBlank())) {
                throw invalidMath();
            }
        }
    }

    private boolean isOptionalBoolean(Object value) {
        return value == null || value instanceof Boolean;
    }

    private boolean isOptionalSpan(Object value) {
        return value == null || isIntegerBetween(value, 1, MAX_TABLE_SPAN);
    }

    private boolean isIntegerBetween(Object value, long min, long max) {
        return (value instanceof Integer || value instanceof Long)
                && ((Number) value).longValue() >= min
                && ((Number) value).longValue() <= max;
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

    private ContentException invalidTable() {
        return new ContentException(ExceptionMessage.INVALID_TABLE);
    }

    private ContentException invalidMath() {
        return new ContentException(ExceptionMessage.INVALID_MATH);
    }
}
