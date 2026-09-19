package com.cpa.yusin.quiz.ebook;

import com.adobe.epubcheck.api.EpubCheck;
import com.cpa.yusin.quiz.ebook.model.*;
import com.cpa.yusin.quiz.ebook.model.BookModel.*;
import com.cpa.yusin.quiz.ebook.render.EpubRenderer;
import com.cpa.yusin.quiz.ebook.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import javax.xml.parsers.DocumentBuilderFactory;
import static org.assertj.core.api.Assertions.*;

class EpubRendererTest {
    @TempDir Path temporary;
    private final BlockNormalizer normalizer = new BlockNormalizer();
    private final BookComposer composer = new BookComposer();
    private final EpubRenderer renderer = new EpubRenderer(new ObjectMapper());

    @Test
    void allNinePlacementCombinationsPassEpubcheckAndKeepEveryLinkResolvable() throws Exception {
        for (var answer : BookSettings.Placement.values()) {
            for (var explanation : BookSettings.Placement.values()) {
                var settings = new BookSettings(BookSettings.YearOrder.NEWEST_FIRST, answer, explanation);
                byte[] data = renderer.render(composer.compose(sampleBook(), settings), settings);
                Path path = temporary.resolve(answer + "-" + explanation + ".epub");
                Files.write(path, data);
                assertThat(new EpubCheck(path.toFile()).doValidate()).as(path.toString()).isZero();
                Map<String, String> files = unzip(data);
                assertThat(files.get("EPUB/year-2025.xhtml")).contains("&lt;원문&gt;", "(가)", "첫 항목", "민법");
                String combined = String.join("", files.values());
                assertThat(combined).contains("정답: 1번, 2번", "보기별 해설", "전체 해설");
                assertThat(combined).doesNotContain("explanation-12\""); // 해설 없는 문제에는 빈 해설을 만들지 않습니다.
                assertLinks(files);
                assertThat(combined).doesNotContain("문제로 돌아가기", "정답 확인", "class=\"return\"");
                assertThat(files.get("EPUB/package.opf")).contains("properties=\"cover-image\"", "rendition:spread");
                assertThat(files.get("EPUB/cover.svg")).contains("전자책", "총 3문항");
                // 본문에는 조작 버튼/링크가 없고, 독서 위치 이동은 목차와 리더가 담당합니다.
                for (var file : files.entrySet()) {
                    if (file.getKey().matches("EPUB/(year|solutions)-.*\\.xhtml")) {
                        assertThat(file.getValue()).doesNotContain("<a ", "<button", "<script");
                    }
                }
                if (answer == BookSettings.Placement.AFTER_QUESTION) {
                    assertThat(files.get("EPUB/year-2025.xhtml")).doesNotContain("1번 정답</h2>");
                }
            }
        }
    }

    @Test
    void longCoverTitlesRemainCompleteEscapedAndWithinArtwork() throws Exception {
        var original = sampleBook();
        for (String title : List.of("가".repeat(160), "W".repeat(160), "제목 <원문> & \n 줄바꿈")) {
            var book = new BookModel(original.identifier(), title, original.qualificationCode(),
                    original.capturedAt(), original.questions());
            var settings = new BookSettings(BookSettings.YearOrder.NEWEST_FIRST,
                    BookSettings.Placement.AFTER_QUESTION, BookSettings.Placement.YEAR_END);
            var files = unzip(renderer.render(composer.compose(book, settings), settings));
            var xml = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(files.get("EPUB/cover.svg").getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            var labels = xml.getElementsByTagName("text");
            StringBuilder reconstructed = new StringBuilder();
            for (int i = 0; i < labels.getLength() - 2; i++) {
                var label = (org.w3c.dom.Element) labels.item(i);
                reconstructed.append(label.getTextContent());
                assertThat(Integer.parseInt(label.getAttribute("font-size"))).isPositive();
                assertThat(Integer.parseInt(label.getAttribute("y"))).isLessThan(1010);
            }
            assertThat(reconstructed.toString()).isEqualTo(title.replaceAll("\\s+", " ").strip());
        }
    }

    @Test
    void compositionKeepsYearThenOriginalNumberWithStableTieBreakers() {
        var book = sampleBook();
        var oldest = new BookSettings(BookSettings.YearOrder.OLDEST_FIRST,
                BookSettings.Placement.AFTER_QUESTION, BookSettings.Placement.YEAR_END);
        var composed = composer.compose(book, oldest);
        assertThat(composed.chapters().getFirst().id()).isEqualTo("year-2022");
        var ids = composed.chapters().stream().flatMap(ch -> ch.entries().stream())
                .filter(e -> e.kind() == BookComposer.Kind.QUESTION).map(e -> e.question().id()).toList();
        assertThat(ids).containsExactly(12L, 11L, 13L);
        assertThat(composed.chapters().stream().flatMap(ch -> ch.entries().stream()).map(BookComposer.Entry::anchor))
                .doesNotHaveDuplicates();
        assertThat(composed.chapters().getFirst().entries()).extracting(BookComposer.Entry::kind)
                .containsExactly(BookComposer.Kind.QUESTION, BookComposer.Kind.ANSWER);
    }

    @Test
    void unsupportedContentAndControlCharactersFailWithQuestionContext() {
        assertThatThrownBy(() -> normalizer.normalize(List.of(Map.of("type", "image", "src", "https://example.com/x")), "문제 9"))
                .isInstanceOf(EbookValidationException.class).hasMessageContaining("문제 9").hasMessageContaining("이미지");
        assertThatThrownBy(() -> normalizer.normalize(List.of(Map.of("type", "table")), "문제 10"))
                .hasMessageContaining("문제 10").hasMessageContaining("table");
        assertThatThrownBy(() -> BlockNormalizer.checkedText("문자\u0001"))
                .isInstanceOf(EbookValidationException.class);
        assertThatThrownBy(() -> normalizer.normalize(List.of(Map.of("type", "text", "spans", List.of(
                Map.of("text", "x", "color", "red; background:url(x)")))), "문제 11"))
                .hasMessageContaining("색상");
    }

    @Test
    void writesRepresentativeSampleForHumanReview() throws Exception {
        var settings = new BookSettings(BookSettings.YearOrder.NEWEST_FIRST,
                BookSettings.Placement.AFTER_QUESTION, BookSettings.Placement.YEAR_END);
        Path path = Path.of("build/ebook-samples/sample-review.epub");
        Files.createDirectories(path.getParent());
        var book = readingSampleBook();
        Files.write(path, renderer.render(composer.compose(book, settings), settings));
        // 같은 가상 원고로 분리형/연속형을 함께 검수합니다. 실제 기출이나 법률 해설이 아닙니다.
        var inline = new BookSettings(BookSettings.YearOrder.NEWEST_FIRST,
                BookSettings.Placement.AFTER_QUESTION, BookSettings.Placement.AFTER_QUESTION);
        Files.write(path.resolveSibling("sample-reading-inline.epub"),
                renderer.render(composer.compose(book, inline), inline));
    }

    private BookModel readingSampleBook() {
        var content = normalizer.normalize(List.of(
                Map.of("type", "text", "text", "다음은 전자책의 편집 원칙에 관한 설명이다. 제시된 원칙에 부합하는 보기를 모두 고르시오."),
                Map.of("type", "statementGroup", "items", List.of(
                        Map.of("label", "ㄱ.", "content", List.of(Map.of("type", "text", "text",
                                "독자는 기기의 방향과 글자 크기를 바꿀 수 있다. 본문은 화면의 너비에 맞춰 자연스럽게 이어져야 한다."))),
                        Map.of("label", "ㄴ.", "content", List.of(Map.of("type", "text", "text",
                                "문제와 해설의 경계는 색상뿐 아니라 제목의 굵기와 영역 사이의 간격으로도 구분한다."))),
                        Map.of("label", "ㄷ.", "content", List.of(Map.of("type", "text", "text",
                                "보기의 문장이 여러 줄로 이어질 때는 둘째 줄부터 본문의 시작 위치에 맞춰 읽을 수 있도록 한다.")))))) , "독서 샘플");
        var explanation = normalizer.normalize(List.of(
                Map.of("type", "text", "text", "이 문항은 실제 시험 문제가 아닌 디자인 검수용 가상 문항입니다. 제시문은 화면 크기가 달라져도 정보의 순서와 관계를 쉽게 파악할 수 있도록 하는 편집 원칙을 설명합니다."),
                Map.of("type", "text", "text", "문제 번호와 출처를 먼저 확인한 뒤 본문과 보기를 읽습니다. 정답과 해설은 제목의 굵기와 간격으로 구별합니다. 긴 해설은 다음 화면으로 이어져도 같은 읽기 흐름을 유지합니다.")), "독서 샘플");
        var choiceExplanation = normalizer.normalize(List.of(
                Map.of("type", "text", "text", "글자 크기를 키우면 한 줄에 들어가는 글자 수와 페이지 수가 달라집니다. 문장을 고정된 칸에 가두지 않고, 보기 번호와 본문의 들여쓰기 관계를 유지하는 것이 이 가상 문항의 편집 원칙에 부합합니다.")), "독서 샘플");
        var choices = List.of(
                new Choice(1, 1, "본문의 글자 크기를 바꾸더라도 문장과 보기의 순서가 유지되도록 한다.", true, choiceExplanation),
                new Choice(2, 2, "모든 화면에 같은 수의 문장을 표시하기 위해 글자 크기와 페이지 높이를 고정한다.", false, List.of()),
                new Choice(3, 3, "문제와 해설의 성격을 제목, 배경의 차이, 여백으로 함께 구분한다.", true, choiceExplanation),
                new Choice(4, 4, "출처를 본문보다 크게 표시하고 모든 문장을 굵게 처리하여 중요도를 동일하게 만든다.", false, List.of()),
                new Choice(5, 5, "문장이 길어지면 보기 번호 아래로 본문을 이어 쓰고 들여쓰기는 생략한다.", false, List.of()));
        return new BookModel("urn:uuid:abc45678-1234-4234-8234-123456789012", "독서 디자인 검수용 샘플 (가상 문항)", "APPRAISER",
                Instant.parse("2026-09-19T00:00:00Z"), List.of(
                new Question(71, 1, "편집 검수 · 가상 시험", 2025, 1, "독서 디자인", 1, 7, content, explanation, choices),
                new Question(81, 1, "편집 검수 · 가상 시험", 2025, 1, "독서 디자인", 1, 8,
                        normalizer.normalize(List.of(Map.of("type", "text", "text", "다음 보기 중 위의 편집 원칙에 부합하는 것을 모두 고르시오. 이 문항은 전체 해설 없이 보기별 해설만 있는 경우의 검수용입니다.")), "독서 샘플"),
                        List.of(), choices)));
    }

    private BookModel sampleBook() {
        List<Map<String, Object>> raw = List.of(
                Map.of("type", "text", "spans", List.of(Map.of("text", "다음 <원문> & 조건을 읽고 옳은 보기를 모두 고르시오.", "bold", true))),
                Map.of("type", "statementGroup", "items", List.of(Map.of("label", "(가)", "content", List.of(
                        Map.of("type", "text", "text", "제시문은 원래 표현과 줄바꿈을 유지합니다.\n두 번째 줄입니다."))))),
                Map.of("type", "list", "ordered", true, "children", List.of(Map.of("type", "listItem", "children", List.of(
                        Map.of("type", "text", "text", "첫 항목"))))),
                Map.of("type", "text", "listing", "ordered", "text", "구형 목록 1"),
                Map.of("type", "text", "listing", "ordered", "text", "구형 목록 2"));
        var content = normalizer.normalize(raw, "샘플");
        var explanation = normalizer.normalize(List.of(Map.of("type", "text", "text", "전체 해설 — 검수용 가상 문항입니다.")), "샘플");
        var choiceExplanation = normalizer.normalize(List.of(Map.of("type", "text", "text", "보기별 해설")), "샘플");
        var choices = List.of(new Choice(1, 1, "첫 번째 보기", true, choiceExplanation),
                new Choice(2, 2, "두 번째 보기", true, List.of()), new Choice(3, 3, "세 번째 보기", false, List.of()));
        var noExplanationChoices = choices.stream().map(c -> new Choice(c.id(), c.number(), c.text(), c.correct(), List.of())).toList();
        return new BookModel("urn:uuid:12345678-1234-4234-8234-123456789012", "전자책 검수용 샘플 (가상 문항)", "APPRAISER",
                Instant.parse("2026-09-16T00:00:00Z"), List.of(
                new Question(13, 2, "1차", 2025, 2, "경제학", 2, 1, content, explanation, choices),
                new Question(12, 1, "1차", 2022, 1, "민법", 1, 2, content, List.of(), noExplanationChoices),
                new Question(11, 2, "1차", 2025, 1, "민법", 1, 1, content, explanation, choices)));
    }

    private Map<String, String> unzip(byte[] bytes) throws Exception {
        Map<String, String> files = new LinkedHashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry = zip.getNextEntry();
            assertThat(entry.getName()).isEqualTo("mimetype");
            assertThat(entry.getMethod()).isEqualTo(ZipEntry.STORED);
            do { files.put(entry.getName(), new String(zip.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)); }
            while ((entry = zip.getNextEntry()) != null);
        }
        return files;
    }

    private void assertLinks(Map<String, String> files) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Map<String, org.w3c.dom.Document> documents = new HashMap<>();
        for (var entry : files.entrySet()) if (entry.getKey().endsWith(".xhtml")) {
            documents.put(entry.getKey(), factory.newDocumentBuilder().parse(new ByteArrayInputStream(entry.getValue().getBytes(java.nio.charset.StandardCharsets.UTF_8))));
        }
        for (var document : documents.values()) {
            var links = document.getElementsByTagName("a");
            for (int i = 0; i < links.getLength(); i++) {
                String[] href = ((org.w3c.dom.Element) links.item(i)).getAttribute("href").split("#");
                assertThat(documents).containsKey("EPUB/" + href[0]);
                if (href.length > 1) {
                    var elements = documents.get("EPUB/" + href[0]).getElementsByTagName("*");
                    boolean found = false;
                    for (int j = 0; j < elements.getLength(); j++) if (href[1].equals(((org.w3c.dom.Element) elements.item(j)).getAttribute("id"))) found = true;
                    assertThat(found).as(href[1]).isTrue();
                }
            }
        }
    }
}
