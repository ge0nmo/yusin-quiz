package com.cpa.yusin.quiz.ebook.render;

import com.cpa.yusin.quiz.ebook.model.BookModel;
import java.util.ArrayList;
import java.util.List;
import static com.cpa.yusin.quiz.ebook.render.EpubRenderer.xml;

/** 원문 이미지 없이 제목/연도/문항 수만으로 만드는 벡터 표지. 외부 폰트나 네트워크가 필요 없습니다. */
final class EpubCover {
    private EpubCover() {}

    static String render(BookModel book) {
        List<String> lines = wrap(book.title());
        // 긴 사용자 제목도 생략하지 않습니다. 최대 160자에서 표지 중간 영역 안에 들어오도록 축소합니다.
        int fontSize = Math.min(54, 650 / Math.max(1, lines.size()) - 14);
        int lineHeight = fontSize + 14;
        StringBuilder svg = new StringBuilder("""
                <svg xmlns="http://www.w3.org/2000/svg" width="900" height="1260" viewBox="0 0 900 1260">
                <rect width="900" height="1260" fill="#f4f1e8"/>
                <rect x="0" y="0" width="26" height="1260" fill="#254b3e"/>
                <path d="M88 116H812" stroke="#254b3e" stroke-width="3"/>
                <g fill="#203d33" font-family="sans-serif">
                """);
        int y = 254;
        for (String line : lines) {
            svg.append("<text x=\"88\" y=\"").append(y).append("\" font-size=\"").append(fontSize)
                    .append("\" font-weight=\"700\">").append(xml(line)).append("</text>");
            y += lineHeight;
        }
        String years = book.questions().stream().map(BookModel.Question::year).distinct().sorted()
                .map(year -> year + "년").collect(java.util.stream.Collectors.joining(" · "));
        // 많은 연도를 선택해도 표지 하단에서 넘치지 않도록 전체 범위로 표기합니다.
        if (years.length() > 32) {
            var range = book.questions().stream().mapToInt(BookModel.Question::year).summaryStatistics();
            years = range.getMin() + "–" + range.getMax() + "년";
        }
        svg.append("<path d=\"M88 1010H812\" stroke=\"#254b3e\" stroke-width=\"1\"/>")
                .append("<text x=\"88\" y=\"1078\" font-size=\"28\">").append(xml(years)).append("</text>")
                .append("<text x=\"88\" y=\"1136\" font-size=\"25\">총 ").append(book.questions().size())
                .append("문항</text></g></svg>");
        return svg.toString();
    }

    /** SVG는 자동 줄바꿈이 없어 보수적인 전각 폭으로 배치합니다. 넓은 영문자/이모지도 잘리지 않습니다. */
    private static List<String> wrap(String title) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        int width = 0;
        for (int codePoint : title.replaceAll("\\s+", " ").strip().codePoints().toArray()) {
            int advance = codePoint == ' ' ? 1 : 2;
            if (width + advance > 24) {
                lines.add(line.toString());
                line.setLength(0);
                width = 0;
            }
            line.appendCodePoint(codePoint);
            width += advance;
        }
        if (!line.isEmpty()) lines.add(line.toString());
        return lines;
    }
}
