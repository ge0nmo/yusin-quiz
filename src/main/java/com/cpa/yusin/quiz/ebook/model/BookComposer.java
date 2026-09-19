package com.cpa.yusin.quiz.ebook.model;

import org.springframework.stereotype.Component;
import java.util.*;
import static com.cpa.yusin.quiz.ebook.model.BookSettings.Placement.*;

/** 순서와 배치 정책만 결정합니다. XHTML/ZIP/PDF를 알지 않는 순수 조판 계층입니다. */
@Component
public class BookComposer {
    public enum Kind { QUESTION, ANSWER, EXPLANATION }
    public record Entry(Kind kind, BookModel.Question question) {
        public String anchor() { return kind.name().toLowerCase(Locale.ROOT) + "-" + question.id(); }
    }
    public record Chapter(String id, String title, List<Entry> entries) {
        public Chapter { entries = List.copyOf(entries); }
    }
    public record Composition(BookModel book, List<Chapter> chapters) {
        public Composition { chapters = List.copyOf(chapters); }
    }

    public Composition compose(BookModel book, BookSettings settings) {
        Comparator<BookModel.Question> byYear = Comparator.comparingInt(BookModel.Question::year);
        if (settings.yearOrder() == BookSettings.YearOrder.NEWEST_FIRST) byYear = byYear.reversed();
        // 원래 번호가 과목마다 다시 시작할 수 있습니다. 번호 우선순위는 유지하고 동률만 안정적으로 구분합니다.
        var ordered = book.questions().stream().sorted(byYear.thenComparingInt(BookModel.Question::number)
                .thenComparingLong(BookModel.Question::examId)
                .thenComparingInt(BookModel.Question::subjectOrder)
                .thenComparingLong(BookModel.Question::subjectId)
                .thenComparingLong(BookModel.Question::id)).toList();
        Map<Integer, List<BookModel.Question>> years = new LinkedHashMap<>();
        ordered.forEach(q -> years.computeIfAbsent(q.year(), ignored -> new ArrayList<>()).add(q));
        List<Chapter> chapters = new ArrayList<>();
        years.forEach((year, questions) -> {
            List<Entry> entries = new ArrayList<>();
            questions.forEach(q -> {
                entries.add(new Entry(Kind.QUESTION, q));
                addSolutions(entries, q, settings, AFTER_QUESTION);
            });
            chapters.add(new Chapter("year-" + year, year + "년 문제", entries));
            List<Entry> solutions = new ArrayList<>();
            questions.forEach(q -> addSolutions(solutions, q, settings, YEAR_END));
            if (!solutions.isEmpty()) chapters.add(new Chapter("solutions-" + year, year + "년 정답·해설", solutions));
        });
        List<Entry> end = new ArrayList<>();
        ordered.forEach(q -> addSolutions(end, q, settings, BOOK_END));
        if (!end.isEmpty()) chapters.add(new Chapter("solutions-all", "정답·해설", end));
        return new Composition(book, chapters);
    }

    private void addSolutions(List<Entry> target, BookModel.Question q, BookSettings settings,
                              BookSettings.Placement placement) {
        if (settings.answerPlacement() == placement) target.add(new Entry(Kind.ANSWER, q));
        if (settings.explanationPlacement() == placement && q.hasExplanation()) {
            target.add(new Entry(Kind.EXPLANATION, q));
        }
    }
}
