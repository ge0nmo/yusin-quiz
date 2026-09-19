package com.cpa.yusin.quiz.ebook.model;

import java.time.Instant;
import java.util.List;

/**
 * 출력 형식과 DB로부터 독립된 책 스냅샷입니다.
 * EPUB의 XHTML이나 PDF 페이지 번호를 넣지 않아 두 출력기가 같은 원문을 사용할 수 있습니다.
 */
public record BookModel(String identifier, String title, String qualificationCode, Instant capturedAt,
                        List<Question> questions) {
    public BookModel { questions = List.copyOf(questions); }

    public record Question(long id, long examId, String examName, int year, long subjectId,
                           String subjectName, int subjectOrder, int number, List<Block> content,
                           List<Block> explanation, List<Choice> choices) {
        public Question {
            content = List.copyOf(content);
            explanation = List.copyOf(explanation);
            choices = List.copyOf(choices);
        }
        public boolean hasExplanation() {
            return !explanation.isEmpty() || choices.stream().anyMatch(c -> !c.explanation().isEmpty());
        }
    }

    public record Choice(long id, int number, String text, boolean correct, List<Block> explanation) {
        public Choice { explanation = List.copyOf(explanation); }
    }

    public sealed interface Block permits Text, Listing, Statements {}
    public record Text(String tag, String align, List<Span> spans) implements Block {
        public Text { spans = List.copyOf(spans); }
    }
    public record Span(String text, boolean bold, boolean italic, boolean underline, boolean strike,
                       String color, String backgroundColor) {}
    public record Listing(boolean ordered, List<List<Block>> items) implements Block {
        public Listing { items = items.stream().map(List::copyOf).toList(); }
    }
    public record Statements(List<Statement> items) implements Block {
        public Statements { items = List.copyOf(items); }
    }
    public record Statement(String label, List<Block> content) {
        public Statement { content = List.copyOf(content); }
    }
}
