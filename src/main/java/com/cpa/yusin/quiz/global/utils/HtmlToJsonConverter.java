package com.cpa.yusin.quiz.global.utils;

import com.cpa.yusin.quiz.problem.domain.block.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter; // Getter 추가
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class HtmlToJsonConverter {

    private static final Set<String> IGNORE_IDS = Set.of("hwpEditorBoardContent");
    private static final Set<String> BLOCK_TAGS = Set.of("p", "div", "h1", "h2", "h3", "br");

    public List<Block> convert(String html) {
        if (!StringUtils.hasText(html)) {
            return new ArrayList<>();
        }

        // 1. Jsoup 파싱 & Sanitization
        Document doc = Jsoup.parseBodyFragment(html);
        doc.outputSettings().prettyPrint(false);
        sanitize(doc.body());

        // 2. 컨텍스트 생성 (여기서 collectedBlocks를 관리)
        BlockBuilderContext context = new BlockBuilderContext();

        // 3. 순회
        for (Node node : doc.body().childNodes()) {
            processNode(node, StyleState.empty(), context);
        }

        // 4. [수정됨] 남은 텍스트 버퍼를 'context 내부의 리스트'에 최종 플러시
        context.flushTextBlockToInternalList();

        // 5. [수정됨] context가 모아온 모든 블록을 반환
        return context.getCollectedBlocks();
    }

    private void processNode(Node node, StyleState currentStyle, BlockBuilderContext context) {
        if (node instanceof TextNode) {
            String text = ((TextNode) node).getWholeText();
            text = text.replace('\u00A0', ' ');

            if (!text.isEmpty()) {
                context.appendSpan(text, currentStyle);
            }
        }
        else if (node instanceof Element) {
            Element element = (Element) node;
            String tagName = element.tagName().toLowerCase();

            // A. 이미지 처리
            if (tagName.equals("img")) {
                // [중요] 이미지는 블록을 나눔 -> 현재까지의 텍스트를 저장하고 이미지 추가
                context.addImageBlock(element.attr("src"), element.attr("alt"));
                return;
            }

            // B. 줄바꿈
            if (tagName.equals("br")) {
                context.appendSpan("\n", currentStyle);
                return;
            }

            // C. 블록 태그 처리
            boolean isBlock = BLOCK_TAGS.contains(tagName);
            if (isBlock && !context.isBufferEmpty()) {
                context.appendSpan("\n", currentStyle);
            }

            // D. 재귀 탐색
            StyleState newStyle = extractStyle(element, currentStyle);
            for (Node child : element.childNodes()) {
                processNode(child, newStyle, context);
            }
        }
    }

    // --- Inner Classes ---

    @Getter // 리스트 반환을 위해 Getter 추가
    private static class BlockBuilderContext {
        // 이 리스트 하나에 순서대로 다 담습니다.
        private final List<Block> collectedBlocks = new ArrayList<>();

        private final List<Span> currentSpans = new ArrayList<>();
        private StringBuilder pendingText = new StringBuilder();
        private StyleState pendingStyle = null;

        // 텍스트 쌓기
        void appendSpan(String text, StyleState style) {
            if (pendingStyle != null && pendingStyle.equals(style)) {
                pendingText.append(text);
            } else {
                flushPendingSpan();
                pendingStyle = style;
                pendingText.append(text);
            }
        }

        // [수정] 이미지 추가 로직 (텍스트 플러시 -> 이미지 추가)
        void addImageBlock(String src, String alt) {
            flushTextBlockToInternalList(); // 이미지 앞에 있던 텍스트 저장

            collectedBlocks.add(ImageBlock.builder()
                    .type("image")
                    .src(src)
                    .alt(alt)
                    .build());
        }

        // [수정] 현재 버퍼 내용을 collectedBlocks로 이동
        void flushTextBlockToInternalList() {
            flushPendingSpan(); // 작업 중이던 Span 마무리

            if (currentSpans.isEmpty()) return;

            // 공백만 있는지 체크 (옵션: 너무 빈 블록이 많으면 제거)
            boolean allEmpty = currentSpans.stream()
                    .allMatch(s -> s.getText().trim().isEmpty());

            if (!allEmpty) {
                collectedBlocks.add(TextBlock.builder()
                        .type("text")
                        .spans(new ArrayList<>(currentSpans))
                        .build());
            }
            currentSpans.clear();
        }

        boolean isBufferEmpty() {
            return currentSpans.isEmpty() && pendingText.length() == 0;
        }

        private void flushPendingSpan() {
            if (pendingText.length() == 0) return;

            String text = pendingText.toString();

            currentSpans.add(Span.builder()
                    .text(text)
                    .bold(pendingStyle != null && pendingStyle.isBold ? true : null)
                    .color(pendingStyle != null ? pendingStyle.color : null)
                    .build());

            pendingText.setLength(0);
            pendingStyle = null;
        }
    }

    @Data
    @Builder
    private static class StyleState {
        boolean isBold;
        String color;

        static StyleState empty() {
            return StyleState.builder().isBold(false).color(null).build();
        }
    }

    private StyleState extractStyle(Element element, StyleState parentStyle) {
        boolean isBold = parentStyle.isBold;
        String color = parentStyle.color;

        String tagName = element.tagName().toLowerCase();
        String styleAttr = element.attr("style");

        if (Set.of("b", "strong", "h1", "h2", "h3").contains(tagName)) {
            isBold = true;
        }

        if (StringUtils.hasText(styleAttr)) {
            if (styleAttr.contains("bold") || styleAttr.contains("700")) {
                isBold = true;
            }
            String parsedColor = parseColor(styleAttr);
            if (parsedColor != null) {
                color = parsedColor;
            }
        }

        return StyleState.builder().isBold(isBold).color(color).build();
    }

    private String parseColor(String style) {
        Pattern pattern = Pattern.compile("color\\s*:\\s*(#[0-9a-fA-F]{3,6}|[a-zA-Z]+)");
        Matcher matcher = pattern.matcher(style);
        if (matcher.find()) {
            String c = matcher.group(1).toLowerCase();
            if (Set.of("#000", "#000000", "black").contains(c)) return null;
            return c;
        }
        return null;
    }

    private void sanitize(Element body) {
        for (String id : IGNORE_IDS) body.select("#" + id).remove();
        body.select("script, style, meta, link").remove();
        removeComments(body);
    }

    private void removeComments(Node node) {
        for (int i = 0; i < node.childNodeSize(); i++) {
            Node child = node.childNode(i);
            if (child.nodeName().equals("#comment")) {
                child.remove();
                i--;
            } else {
                removeComments(child);
            }
        }
    }
}