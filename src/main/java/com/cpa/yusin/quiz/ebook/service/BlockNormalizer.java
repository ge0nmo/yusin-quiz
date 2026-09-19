package com.cpa.yusin.quiz.ebook.service;

import com.cpa.yusin.quiz.ebook.model.BookModel.*;
import org.springframework.stereotype.Component;
import java.util.*;

/** JSON 블록을 허용된 의미 구조로 변환합니다. 모르는 블록을 조용히 누락시키지 않습니다. */
@Component
public class BlockNormalizer {
    private static final Set<String> TAGS = Set.of("p", "h1", "h2", "h3", "blockquote");
    private static final Set<String> ALIGNS = Set.of("left", "center", "right", "justify");

    public List<Block> normalize(List<Map<String, Object>> source, String context) {
        try { return blocks(source == null ? List.of() : source, 0); }
        catch (EbookValidationException e) { throw new EbookValidationException(context + ": " + e.getMessage()); }
    }

    private List<Block> blocks(List<?> source, int depth) {
        if (depth > 24) throw invalid("중첩이 너무 깊습니다 (최대 24단계).");
        List<Block> result = new ArrayList<>();
        for (int index = 0; index < source.size(); index++) {
            Map<?, ?> block = object(source.get(index));
            String type = string(block, "type", "");
            switch (type) {
                case "text" -> {
                    String listing = string(block, "listing", "");
                    if (listing.isEmpty()) result.add(text(block));
                    else {
                        if (!Set.of("ordered", "bullet").contains(listing)) throw invalid("지원하지 않는 목록입니다.");
                        // 구형 연속 text/listing 블록은 하나의 목록으로 묶어 원래 번호의 흐름을 보존합니다.
                        List<List<Block>> items = new ArrayList<>();
                        items.add(List.of(text(block)));
                        while (index + 1 < source.size()) {
                            Map<?, ?> next = object(source.get(index + 1));
                            if (!"text".equals(next.get("type")) || !listing.equals(next.get("listing"))) break;
                            items.add(List.of(text(next)));
                            index++;
                        }
                        result.add(new Listing("ordered".equals(listing), items));
                    }
                }
                case "list" -> {
                    List<List<Block>> items = new ArrayList<>();
                    for (Object child : array(block.get("children"))) {
                        Map<?, ?> item = object(child);
                        if (!"listItem".equals(item.get("type"))) throw invalid("목록 항목 구조가 올바르지 않습니다.");
                        items.add(blocks(array(item.get("children")), depth + 1));
                    }
                    result.add(new Listing(Boolean.TRUE.equals(block.get("ordered")), items));
                }
                case "statementGroup" -> {
                    List<Statement> items = new ArrayList<>();
                    for (Object child : array(block.get("items"))) {
                        Map<?, ?> item = object(child);
                        String label = string(item, "label", "");
                        List<Block> content = blocks(array(item.get("content")), depth + 1);
                        if (label.isBlank() || !hasText(content)) throw invalid("제시문 라벨/본문이 비어 있습니다.");
                        items.add(new Statement(label, content));
                    }
                    if (items.isEmpty()) throw invalid("제시문이 비어 있습니다.");
                    result.add(new Statements(items));
                }
                case "image" -> throw invalid("이미지가 포함되어 있습니다. 현재 전자책은 텍스트 문제만 지원합니다.");
                default -> throw invalid("지원하지 않는 블록 형식: " + type);
            }
        }
        return List.copyOf(result);
    }

    private Text text(Map<?, ?> source) {
        String tag = string(source, "tag", "p"), align = string(source, "align", "left");
        if (!TAGS.contains(tag) || !ALIGNS.contains(align)) throw invalid("텍스트 태그 또는 정렬이 올바르지 않습니다.");
        List<Span> spans = new ArrayList<>();
        if (source.get("spans") != null) {
            for (Object value : array(source.get("spans"))) {
                Map<?, ?> span = object(value);
                spans.add(new Span(string(span, "text", ""), flag(span, "bold"), flag(span, "italic"),
                        flag(span, "underline"), flag(span, "strikethrough"),
                        color(span.get("color")), color(span.get("backgroundColor"))));
            }
        } else spans.add(new Span(string(source, "text", ""), false, false, false, false, "", ""));
        return new Text(tag, align, spans);
    }

    public static boolean hasText(List<Block> blocks) {
        return blocks.stream().anyMatch(b -> switch (b) {
            case Text t -> t.spans().stream().anyMatch(s -> !s.text().isBlank());
            case Listing l -> l.items().stream().anyMatch(BlockNormalizer::hasText);
            case Statements s -> s.items().stream().anyMatch(i -> hasText(i.content()));
        });
    }

    /** XML에서 허용하지 않는 문자는 삭제하지 않고 실패시켜 원문 손실을 막습니다. */
    public static String checkedText(String value) {
        if (value == null) throw invalid("텍스트가 없습니다.");
        if (value.codePoints().anyMatch(c -> !(c == 9 || c == 10 || c == 13 ||
                c >= 0x20 && c <= 0xD7FF || c >= 0xE000 && c <= 0xFFFD || c >= 0x10000 && c <= 0x10FFFF))) {
            throw invalid("전자책에서 표현할 수 없는 제어 문자가 있습니다.");
        }
        return value;
    }

    private String color(Object value) {
        if (value == null || "".equals(value)) return "";
        // 편집기에 저장된 HEX/rgb 색상만 허용하여 임의 CSS/외부 URL이 들어가지 않게 합니다.
        if (value instanceof String s && s.matches("(?i)(#[0-9a-f]{3,8}|rgba?\\([0-9.,% ]+\\)|[a-z]{1,20})")) return s;
        throw invalid("지원하지 않는 텍스트 색상입니다.");
    }
    private boolean flag(Map<?, ?> map, String key) { return Boolean.TRUE.equals(map.get(key)); }
    private String string(Map<?, ?> map, String key, String fallback) {
        Object value = map.get(key);
        if (value == null) return fallback;
        if (!(value instanceof String s)) throw invalid(key + " 값이 문자열이 아닙니다.");
        return checkedText(s);
    }
    private Map<?, ?> object(Object value) {
        if (value instanceof Map<?, ?> map) return map;
        throw invalid("블록이 객체가 아닙니다.");
    }
    private List<?> array(Object value) {
        if (value instanceof List<?> list) return list;
        throw invalid("블록 목록이 올바르지 않습니다.");
    }
    private static EbookValidationException invalid(String message) { return new EbookValidationException(message); }
}
