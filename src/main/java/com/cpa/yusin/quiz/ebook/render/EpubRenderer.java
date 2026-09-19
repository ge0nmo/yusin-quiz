package com.cpa.yusin.quiz.ebook.render;

import com.cpa.yusin.quiz.ebook.model.BookComposer.*;
import com.cpa.yusin.quiz.ebook.model.BookModel.*;
import com.cpa.yusin.quiz.ebook.model.BookSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.zip.*;

/**
 * EPUB 3 컨테이너를 생성하는 출력 어댑터입니다. 외부 네트워크/스크립트 없이 읽을 수 있습니다.
 * 원문은 모두 XML 이스케이프하며 HTML 문자열을 원문 데이터로 취급하지 않습니다.
 */
@Component
@RequiredArgsConstructor
public class EpubRenderer implements BookRenderer {
    private final ObjectMapper mapper;
    // 스타일은 별도 리소스로 관리하여 콘텐츠/ZIP 로직을 바꾸지 않고 편집 디자인을 조정합니다.
    private static final String CSS = loadStyles();

    private static String loadStyles() {
        try (InputStream input = EpubRenderer.class.getResourceAsStream("/ebook/reading.css")) {
            if (input == null) throw new IllegalStateException("Missing ebook/reading.css");
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    @Override
    public byte[] render(Composition composition, BookSettings settings) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(output)) {
            // OCF 규격상 mimetype은 첫 엔트리이며 압축하지 않아야 합니다.
            put(zip, "mimetype", "application/epub+zip", true);
            put(zip, "META-INF/container.xml", """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
                    <rootfiles><rootfile full-path="EPUB/package.opf" media-type="application/oebps-package+xml"/></rootfiles>
                    </container>
                    """, false);
            put(zip, "EPUB/styles.css", CSS, false);
            put(zip, "EPUB/cover.svg", EpubCover.render(composition.book()), false);
            put(zip, "EPUB/title.xhtml", document(composition.book().title(),
                    "<section class=\"cover\" epub:type=\"cover\"><img src=\"cover.svg\" alt=\"" +
                    xml(composition.book().title()) + "\"/></section>"), false);
            for (Chapter chapter : composition.chapters()) {
                StringBuilder body = new StringBuilder("<section class=\"chapter\"><h1 class=\"chapter-title\">" + xml(chapter.title()) + "</h1>");
                // 같은 문제의 연속 항목만 한 묶음으로 만듭니다. 순서/배치 정책은 Composer가 소유합니다.
                // 긴 문제 전체에 break-inside:avoid를 걸면 큰 글씨에서 빈 페이지가 생기므로 제목만 보호합니다.
                Long previousId = null;

                for (Entry entry : chapter.entries()) {
                    boolean startsGroup = !Objects.equals(previousId, entry.question().id());
                    if (startsGroup) {
                        if (previousId != null) body.append("</section>");
                        body.append("<section class=\"problem-group\">");
                    }
                    body.append(entry(entry, !startsGroup));
                    previousId = entry.question().id();
                }
                if (previousId != null) body.append("</section>");
                body.append("</section>");
                put(zip, "EPUB/" + chapter.id() + ".xhtml", document(chapter.title(), body.toString()), false);
            }
            put(zip, "EPUB/nav.xhtml", navigation(composition), false);
            put(zip, "EPUB/package.opf", packageDocument(composition), false);
            // 재생성 시 사용한 범위/정책을 확인할 수 있는 비밀정보 없는 제작 기록입니다.
            put(zip, "META-INF/generation.json", mapper.writeValueAsString(Map.of(
                    "schemaVersion", 1, "identifier", composition.book().identifier(),
                    "capturedAt", composition.book().capturedAt().toString(),
                    "qualificationCode", composition.book().qualificationCode(), "settings", settings,
                    "questionIds", composition.book().questions().stream().map(Question::id).sorted().toList(),
                    "subjectIds", composition.book().questions().stream().map(Question::subjectId).distinct().sorted().toList(),
                    "years", composition.book().questions().stream().map(Question::year).distinct().sorted().toList()
            )), false);
            zip.finish();
            return output.toByteArray();
        } catch (IOException e) { throw new UncheckedIOException("EPUB 파일 생성에 실패했습니다.", e); }
    }

    private String entry(Entry entry, boolean continuation) {
        Question q = entry.question();
        String css = switch (entry.kind()) {
            case QUESTION -> "question";
            case ANSWER -> continuation ? "answer inline-answer" : "answer";
            case EXPLANATION -> "explanation";
        };
        StringBuilder html = new StringBuilder("<div id=\"" + entry.anchor() + "\" class=\"" + css + "\">");
        // 인접한 정답/해설에는 번호와 출처를 반복하지 않습니다. 별도 수록 시에는 출처를 유지합니다.
        if (!continuation) {
            String label = switch (entry.kind()) {
                case QUESTION -> "";
                case ANSWER -> "정답";
                case EXPLANATION -> "해설";
            };
            html.append("<header class=\"problem-heading\"><h2><span class=\"problem-number\">")
                    .append(q.number()).append("</span><span class=\"problem-unit\">번</span>");
            if (!label.isEmpty()) html.append(" <span class=\"heading-kind\">").append(label).append("</span>");
            html.append("</h2>").append(source(q)).append("</header>");
        }
        switch (entry.kind()) {
            case QUESTION -> {
                html.append(blocks(q.content())).append("<ol class=\"choices\">");
                q.choices().forEach(c -> html.append("<li value=\"").append(c.number())
                        .append("\"><p class=\"original\">").append(xml(c.text())).append("</p></li>"));
                html.append("</ol>");
            }
            case ANSWER -> html.append("<p class=\"answer-line\"><strong>정답: ")
                    .append(q.choices().stream().filter(Choice::correct).map(c -> c.number() + "번")
                            .collect(java.util.stream.Collectors.joining(", "))).append("</strong></p>");
            case EXPLANATION -> {
                // 생성한 해설 제목만 꾸밉니다. 원문에 저장된 소제목과 스타일이 섞이지 않게 합니다.
                if (continuation) html.append("<h3 class=\"explanation-heading\"><span class=\"explanation-label\">해설</span></h3>");
                html.append(blocks(q.explanation()));
                q.choices().stream().filter(c -> !c.explanation().isEmpty()).forEach(c -> html.append("<h3 class=\"choice-explanation-title\">")
                        .append(c.number()).append("번 보기 해설</h3>").append(blocks(c.explanation())));
            }
        }
        return html.append("</div>").toString();
    }

    private String source(Question q) {
        return "<p class=\"source\">" + q.year() + "년 · " + xml(q.examName()) + " · " + xml(q.subjectName()) + "</p>";
    }

    private String blocks(List<Block> blocks) {
        StringBuilder html = new StringBuilder();
        for (Block block : blocks) {
            switch (block) {
                case Text t -> {
                    // 문제 본문의 제목이 책/장 제목 위계보다 높아지지 않도록 h1~h3을 소제목으로 정규화합니다.
                    String tag = t.tag().startsWith("h") ? "h3" : t.tag();
                    html.append('<').append(tag).append(" class=\"original\" style=\"text-align:").append(t.align()).append("\">");
                    t.spans().forEach(s -> html.append(span(s)));
                    html.append("</").append(tag).append('>');
                }
                case Listing l -> {
                    String tag = l.ordered() ? "ol" : "ul";
                    html.append('<').append(tag).append('>');
                    l.items().forEach(i -> html.append("<li>").append(blocks(i)).append("</li>"));
                    html.append("</").append(tag).append('>');
                }
                case Statements s -> {
                    html.append("<dl class=\"statement\">");
                    // 항목별로 묶어 기호와 첫 문장을 같은 줄에 두고, 여러 문단/중첩 목록은 그대로 보존합니다.
                    s.items().forEach(i -> html.append("<div class=\"statement-item\"><dt>").append(xml(i.label())).append("</dt><dd>")
                            .append(blocks(i.content())).append("</dd></div>"));
                    html.append("</dl>");
                }
            }
        }
        return html.toString();
    }
    private String span(Span span) {
        String text = xml(span.text());
        if (span.bold()) text = "<strong>" + text + "</strong>";
        if (span.italic()) text = "<em>" + text + "</em>";
        if (span.underline()) text = "<span class=\"underline\">" + text + "</span>";
        if (span.strike()) text = "<span class=\"strike\">" + text + "</span>";
        String style = (span.color().isEmpty() ? "" : "color:" + span.color() + ";") +
                (span.backgroundColor().isEmpty() ? "" : "background-color:" + span.backgroundColor() + ";");
        return style.isEmpty() ? text : "<span style=\"" + xml(style) + "\">" + text + "</span>";
    }

    private String navigation(Composition c) {
        StringBuilder body = new StringBuilder("<nav epub:type=\"toc\" id=\"toc\"><h1 class=\"chapter-title\">목차</h1><ol><li><a href=\"title.xhtml\">");
        body.append(xml(c.book().title())).append("</a></li>");
        c.chapters().forEach(ch -> body.append("<li><a href=\"").append(ch.id()).append(".xhtml\">")
                .append(xml(ch.title())).append("</a></li>"));
        return document("목차", body.append("</ol></nav>").toString());
    }
    private String packageDocument(Composition c) {
        StringBuilder opf = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8"?>
                <package xmlns="http://www.idpf.org/2007/opf" version="3.0" unique-identifier="book-id" xml:lang="ko">
                <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
                """);
        opf.append("<dc:identifier id=\"book-id\">").append(xml(c.book().identifier())).append("</dc:identifier>")
                .append("<dc:title>").append(xml(c.book().title())).append("</dc:title><dc:language>ko</dc:language>")
                .append("<meta property=\"dcterms:modified\">").append(c.book().capturedAt().truncatedTo(ChronoUnit.SECONDS))
                .append("</meta><meta property=\"rendition:layout\">reflowable</meta><meta property=\"rendition:spread\">auto</meta><meta name=\"cover\" content=\"cover-image\"/></metadata><manifest>")
                .append("<item id=\"style\" href=\"styles.css\" media-type=\"text/css\"/>")
                .append("<item id=\"cover-image\" href=\"cover.svg\" media-type=\"image/svg+xml\" properties=\"cover-image\"/>")
                .append("<item id=\"title\" href=\"title.xhtml\" media-type=\"application/xhtml+xml\"/>")
                .append("<item id=\"nav\" href=\"nav.xhtml\" media-type=\"application/xhtml+xml\" properties=\"nav\"/>");
        c.chapters().forEach(ch -> opf.append("<item id=\"").append(ch.id()).append("\" href=\"").append(ch.id())
                .append(".xhtml\" media-type=\"application/xhtml+xml\"/>"));
        opf.append("</manifest><spine><itemref idref=\"title\"/><itemref idref=\"nav\"/>");
        c.chapters().forEach(ch -> opf.append("<itemref idref=\"").append(ch.id()).append("\"/>"));
        return opf.append("</spine></package>").toString();
    }
    private String document(String title, String body) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\" lang=\"ko\" xml:lang=\"ko\">" +
                "<head><meta charset=\"utf-8\"/><title>" + xml(title) +
                "</title><link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\"/></head><body>" + body + "</body></html>";
    }
    static String xml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
    private void put(ZipOutputStream zip, String name, String text, boolean stored) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        ZipEntry entry = new ZipEntry(name);
        // ZIP 자체 시간은 고정하여 로컬 타임존에 따른 빌드 잡음을 줄입니다. 실제 생성 시각은 메타데이터에 있습니다.
        entry.setTimeLocal(java.time.LocalDateTime.of(2000, 1, 1, 0, 0));
        if (stored) {
            CRC32 crc = new CRC32(); crc.update(bytes);
            entry.setMethod(ZipEntry.STORED); entry.setSize(bytes.length); entry.setCrc(crc.getValue());
        }
        zip.putNextEntry(entry); zip.write(bytes); zip.closeEntry();
    }
}
