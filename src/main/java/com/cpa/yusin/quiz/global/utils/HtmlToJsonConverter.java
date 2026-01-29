package com.cpa.yusin.quiz.global.utils;

import com.cpa.yusin.quiz.problem.domain.block.Block;
import com.cpa.yusin.quiz.problem.domain.block.ImageBlock;
import com.cpa.yusin.quiz.problem.domain.block.TextBlock;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlToJsonConverter {

    public List<Block> convert(String html) {
        if (html == null || html.isBlank()) {
            return new ArrayList<>();
        }

        List<Block> blocks = new ArrayList<>();

        // 1. <br> 태그를 미리 특수 문자열로 치환 (Jsoup이 text() 추출 시 br을 날리는 것 방지)
        String processedHtml = html.replaceAll("(?i)<br[^>]*>", "[[BR]]");

        Document doc = Jsoup.parseBodyFragment(processedHtml);

        // Jsoup이 임의로 줄바꿈을 넣지 않도록 설정
        doc.outputSettings().prettyPrint(false);

        Element body = doc.body();

        traverseNodes(body, blocks);

        return blocks;
    }

    private void traverseNodes(Node node, List<Block> blocks) {
        for (Node child : node.childNodes()) {
            if (child instanceof Element) {
                Element element = (Element) child;

                // 이미지 처리
                if (element.tagName().equalsIgnoreCase("img")) {
                    blocks.add(ImageBlock.builder()
                            .type("image")
                            .src(element.attr("src"))
                            .alt(element.attr("data-filename"))
                            .build());
                }
                // 블록 레벨 태그(div, p, li 등)는 재귀 탐색 -> 결과적으로 블록 분리됨
                else {
                    traverseNodes(child, blocks);
                }
            } else if (child instanceof TextNode) {
                // 2. 텍스트 추출 후 [[BR]]을 다시 \n으로 복구
                String text = ((TextNode) child).getWholeText().trim();
                text = text.replace("[[BR]]", "\n");

                if (!text.isEmpty()) {
                    blocks.add(TextBlock.builder()
                            .type("text")
                            .text(text)
                            .build());
                }
            }
        }
    }
}