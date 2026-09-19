package com.cpa.yusin.quiz.ebook.service;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.common.service.UuidHolder;
import com.cpa.yusin.quiz.ebook.controller.EbookDto.*;
import com.cpa.yusin.quiz.ebook.infrastructure.EbookRepository;
import com.cpa.yusin.quiz.ebook.model.BookModel;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class EbookSnapshotService {
    public static final int MAX_QUESTIONS = 5000;
    private static final long MAX_SOURCE_BYTES = 20_000_000;
    private final EbookRepository repository;
    private final BlockNormalizer normalizer;
    private final ClockHolder clock;
    private final UuidHolder uuids;
    private final ObjectMapper mapper;

    public EbookSnapshotService(EbookRepository repository, BlockNormalizer normalizer, ClockHolder clock,
                                @Qualifier("systemUuidHolder") UuidHolder uuids, ObjectMapper mapper) {
        this.repository = repository;
        this.normalizer = normalizer;
        this.clock = clock;
        this.uuids = uuids;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CatalogRow> catalog() {
        return repository.catalog().stream().map(r -> new CatalogRow(r.getQualificationCode(),
                r.getQualificationName(), r.getSubjectId(), r.getSubjectName(), r.getYear(), r.getQuestionCount())).toList();
    }

    /**
     * 같은 트랜잭션 스냅샷에서 수량 확인과 전체 fetch join을 수행합니다.
     * 렌더러에 엔티티를 전달하지 않으므로 DB 연결을 잡고 ZIP을 생성하지 않습니다.
     */
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public BookModel capture(GenerateRequest request) {
        long count = repository.countSelected(request.qualificationCode(), request.subjectIds(), request.years());
        if (count == 0) throw invalid("선택한 범위에 공개된 문제가 없습니다. 범위를 다시 선택해주세요.");
        if (count > MAX_QUESTIONS) throw invalid("한 번에 최대 5,000문항을 생성할 수 있습니다. 연도나 과목을 나눠주세요.");
        List<Problem> source = repository.snapshot(request.qualificationCode(), request.subjectIds(), request.years());
        if (source.size() != count) throw invalid("문제 수가 일치하지 않습니다. 다시 생성해주세요.");
        Set<Long> subjects = new HashSet<>();
        Set<Integer> years = new HashSet<>();
        long bytes = 0;
        List<BookModel.Question> questions = new ArrayList<>();
        for (Problem p : source) {
            try {
                // 전체 JSON을 장기간 복제하지 않고 한 문항씩 크기를 검사합니다.
                bytes += mapper.writeValueAsBytes(List.of(p.getContent(), p.getExplanation(),
                        p.getChoices().stream().map(c -> List.of(c.getContent(), c.getExplanation())).toList())).length;
            } catch (JsonProcessingException e) { throw invalid("문제 " + p.getId() + "의 JSON을 읽을 수 없습니다."); }
            if (bytes > MAX_SOURCE_BYTES) throw invalid("원문 용량이 20MB를 넘습니다. 수록 범위를 줄여주세요.");
            questions.add(question(p));
            subjects.add(p.getSubjectMapping().getSubject().getId());
            years.add(p.getExam().getYear());
        }
        // 잘못된 ID나 다른 자격시험의 과목을 보냈을 때 일부만 성공한 것처럼 반환하지 않습니다.
        if (!subjects.containsAll(request.subjectIds()) || !years.containsAll(request.years())) {
            throw invalid("선택한 과목 또는 연도 중 공개 문제가 없는 항목이 있습니다. 목록을 새로고침해주세요.");
        }
        return new BookModel("urn:uuid:" + uuids.getRandom(), BlockNormalizer.checkedText(request.title().strip()),
                request.qualificationCode().name(), Instant.ofEpochMilli(clock.getCurrentTime()), questions);
    }

    private BookModel.Question question(Problem p) {
        String context = "문제 ID " + p.getId() + " (" + p.getExam().getYear() + "년 " + p.getNumber() + "번)";
        var content = normalizer.normalize(p.getContent(), context + " 본문");
        if (!BlockNormalizer.hasText(content)) throw invalid(context + ": 본문이 비어 있습니다.");
        var choices = p.getChoices().stream().sorted(Comparator.comparingInt(c -> c.getNumber())).map(c -> {
            if (c.getContent() == null || c.getContent().isBlank()) throw invalid(context + ": 보기가 비어 있습니다.");
            return new BookModel.Choice(c.getId(), c.getNumber(), BlockNormalizer.checkedText(c.getContent()),
                    c.isAnswer(), explanation(c.getExplanation(), context + " 보기 " + c.getNumber()));
        }).toList();
        if (choices.size() < 2 || choices.size() > 5 || choices.stream().noneMatch(BookModel.Choice::correct)) {
            throw invalid(context + ": 보기(2~5개) 또는 정답을 확인해주세요.");
        }
        for (int i = 0; i < choices.size(); i++) {
            if (choices.get(i).number() != i + 1) throw invalid(context + ": 보기 번호가 연속되지 않습니다.");
        }
        return new BookModel.Question(p.getId(), p.getExam().getId(), BlockNormalizer.checkedText(p.getExam().getName()),
                p.getExam().getYear(), p.getSubjectMapping().getSubject().getId(),
                BlockNormalizer.checkedText(p.getSubjectMapping().getSubject().getName()), p.getSubjectMapping().getDisplayOrder(),
                p.getNumber(), content, explanation(p.getExplanation(), context + " 해설"), choices);
    }

    private List<BookModel.Block> explanation(List<Map<String, Object>> source, String context) {
        var blocks = normalizer.normalize(source, context);
        return BlockNormalizer.hasText(blocks) ? blocks : List.of();
    }
    private EbookValidationException invalid(String message) { return new EbookValidationException(message); }
}
