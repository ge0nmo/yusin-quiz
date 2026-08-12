package com.cpa.yusin.quiz.problem;

import com.cpa.yusin.quiz.global.exception.ContentException;
import com.cpa.yusin.quiz.problem.service.JsonBlockContentValidator;
import org.junit.jupiter.api.Test;

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

    private List<Map<String, Object>> statementGroup(Map<String, Object> item) {
        return List.of(Map.of("type", "statementGroup", "items", List.of(item)));
    }

    private void assertInvalid(List<Map<String, Object>> blocks) {
        assertThatThrownBy(() -> validator.validate(blocks))
                .isInstanceOf(ContentException.class)
                .hasMessageContaining("지문 묶음");
    }
}
