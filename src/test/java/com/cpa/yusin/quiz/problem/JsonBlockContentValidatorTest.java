package com.cpa.yusin.quiz.problem;

import com.cpa.yusin.quiz.global.exception.ContentException;
import com.cpa.yusin.quiz.problem.service.JsonBlockContentValidator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonBlockContentValidatorTest {
    private final JsonBlockContentValidator validator = new JsonBlockContentValidator();

    @Test
    void acceptsStatementItemsWithRichTextAndNestedImages() {
        List<Map<String, Object>> blocks = List.of(Map.of(
                "type", "statementGroup",
                "items", List.of(
                        Map.of("label", "(가)", "content", List.of(Map.of(
                                "type", "text", "spans", List.of(Map.of("text", "보고기간말 이전"))))),
                        Map.of("label", "ㄴ.", "content", List.of(Map.of(
                                "type", "list", "children", List.of(Map.of(
                                        "type", "listItem", "children", List.of(Map.of(
                                                "type", "image", "src", "https://example.com/condition.png"))))))))));

        assertThatCode(() -> validator.validate(blocks)).doesNotThrowAnyException();
    }

    @Test
    void acceptsNestedStatementGroupsAndLegacyText() {
        List<Map<String, Object>> blocks = List.of(Map.of(
                "type", "list",
                "children", List.of(Map.of(
                        "type", "statementGroup",
                        "items", List.of(Map.of(
                                "label", "A)",
                                "content", List.of(Map.of("type", "text", "text", "legacy content"))))))));

        assertThatCode(() -> validator.validate(blocks)).doesNotThrowAnyException();
    }

    @Test
    void rejectsMissingOrEmptyItems() {
        assertInvalid(List.of(Map.of("type", "statementGroup")));
        assertInvalid(List.of(Map.of("type", "statementGroup", "items", List.of())));
    }

    @Test
    void rejectsBlankOrLongLabels() {
        Map<String, Object> content = Map.of("type", "text", "spans", List.of(Map.of("text", "내용")));

        assertInvalid(statementGroup(Map.of("label", "  ", "content", List.of(content))));
        assertInvalid(statementGroup(Map.of("label", "123456789012345678901", "content", List.of(content))));
    }

    @Test
    void rejectsPartialAndMalformedItems() {
        assertInvalid(statementGroup(Map.of("label", "(가)", "content", List.of())));
        assertInvalid(statementGroup(Map.of(
                "label", "(가)",
                "content", List.of(Map.of("type", "text", "spans", List.of(Map.of("text", "  ")))))));
        assertInvalid(List.of(Map.of("type", "statementGroup", "items", List.of("not-an-item"))));
    }

    @Test
    void acceptsTablesWithHeadersMergedCellsAndEmptyCells() {
        List<Map<String, Object>> blocks = List.of(table(
                List.of(cell(Map.of("header", true, "colspan", 2)), cell(Map.of("header", true))),
                List.of(cell(Map.of("rowspan", 2)), Map.of("content", List.of()), cell(Map.of())),
                List.of(cell(Map.of()), cell(Map.of()))));

        assertThatCode(() -> validator.validate(blocks)).doesNotThrowAnyException();
    }

    @Test
    void acceptsTablesWithColumnWidths() {
        Map<String, Object> table = new LinkedHashMap<>(table(List.of(cell(Map.of()), cell(Map.of()))));
        table.put("columnWidths", List.of(120, 80));

        assertThatCode(() -> validator.validate(List.of(table))).doesNotThrowAnyException();
    }

    @Test
    void rejectsMalformedColumnWidths() {
        for (Object widths : List.of(List.of(), List.of(0), List.of(80, -1), List.of(80.5), List.of("80"),
                List.of(10001), "80", Map.of("width", 80))) {
            Map<String, Object> table = new LinkedHashMap<>(table(List.of(cell(Map.of()))));
            table.put("columnWidths", widths);
            assertInvalidTable(List.of(table));
        }
    }

    @Test
    void acceptsTablesNestedInStatementGroupItems() {
        List<Map<String, Object>> blocks = statementGroup(Map.of(
                "label", "(가)", "content", List.of(table(List.of(cell(Map.of()))))));

        assertThatCode(() -> validator.validate(blocks)).doesNotThrowAnyException();
    }

    @Test
    void rejectsMalformedTables() {
        assertInvalidTable(List.of(Map.of("type", "table")));
        assertInvalidTable(List.of(Map.of("type", "table", "rows", List.of())));
        assertInvalidTable(List.of(table(List.of())));
        assertInvalidTable(List.of(Map.of("type", "table", "rows", List.of(Map.of("cells", "cell")))));
        assertInvalidTable(List.of(table(List.of(Map.of("header", true)))));
        assertInvalidTable(List.of(table(List.of(Map.of("content", List.of("text"))))));
        assertInvalidTable(List.of(table(List.of(cell(Map.of("header", "true"))))));
        assertInvalidTable(List.of(table(List.of(cell(Map.of("colspan", 0))))));
        assertInvalidTable(List.of(table(List.of(cell(Map.of("rowspan", "2"))))));
        assertInvalidTable(List.of(table(List.of(cell(Map.of("colspan", 1.5))))));
        assertInvalidTable(List.of(table(List.of(cell(Map.of("rowspan", 1001))))));
    }

    @Test
    void acceptsInlineMathSpans() {
        List<Map<String, Object>> blocks = List.of(Map.of("type", "text", "spans", List.of(
                Map.of("text", "넓이는 "),
                Map.of("text", "\\frac{a}{b}", "math", true),
                Map.of("text", "이다.", "math", false))));

        assertThatCode(() -> validator.validate(blocks)).doesNotThrowAnyException();
    }

    @Test
    void rejectsBlankOrMalformedMathSpans() {
        assertInvalidMath(mathText(Map.of("text", "  ", "math", true)));
        assertInvalidMath(mathText(Map.of("math", true)));
        assertInvalidMath(mathText(Map.of("text", "x^2", "math", "true")));
        assertInvalidMath(List.of(table(List.of(Map.of("content", mathText(Map.of("text", "", "math", true)))))));
    }

    private Map<String, Object> table(List<?>... rows) {
        return Map.of("type", "table", "rows", Arrays.stream(rows)
                .map(cells -> Map.of("cells", cells))
                .toList());
    }

    private Map<String, Object> cell(Map<String, Object> attributes) {
        Map<String, Object> cell = new LinkedHashMap<>(attributes);
        cell.put("content", List.of(Map.of("type", "text", "spans", List.of(Map.of("text", "셀")))));
        return cell;
    }

    private List<Map<String, Object>> mathText(Map<String, Object> span) {
        return List.of(Map.of("type", "text", "spans", List.of(span)));
    }

    private void assertInvalidTable(List<Map<String, Object>> blocks) {
        assertThatThrownBy(() -> validator.validate(blocks))
                .isInstanceOf(ContentException.class)
                .hasMessageContaining("표의");
    }

    private void assertInvalidMath(List<Map<String, Object>> blocks) {
        assertThatThrownBy(() -> validator.validate(blocks))
                .isInstanceOf(ContentException.class)
                .hasMessageContaining("수식");
    }

    private List<Map<String, Object>> statementGroup(Map<String, Object> item) {
        return List.of(Map.of("type", "statementGroup", "items", List.of(item)));
    }

    private void assertInvalid(List<Map<String, Object>> blocks) {
        assertThatThrownBy(() -> validator.validate(blocks))
                .isInstanceOf(ContentException.class)
                .hasMessageContaining("지문 묶음");
    }
}
