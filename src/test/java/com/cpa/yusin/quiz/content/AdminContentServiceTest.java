package com.cpa.yusin.quiz.content;

import com.cpa.yusin.quiz.common.domain.ContentStatus;
import com.cpa.yusin.quiz.content.controller.dto.AdminContentDto.ChoiceRequest;
import com.cpa.yusin.quiz.content.controller.dto.AdminContentDto.ProblemRequest;
import com.cpa.yusin.quiz.content.service.AdminContentService;
import com.cpa.yusin.quiz.exam.infrastructure.ExamRepository;
import com.cpa.yusin.quiz.global.exception.ContentException;
import com.cpa.yusin.quiz.problem.infrastructure.ProblemRepository;
import com.cpa.yusin.quiz.problem.service.JsonBlockContentProcessor;
import com.cpa.yusin.quiz.problem.service.JsonBlockContentValidator;
import com.cpa.yusin.quiz.qualification.infrastructure.QualificationExamRepository;
import com.cpa.yusin.quiz.qualification.infrastructure.QualificationExamSubjectRepository;
import com.cpa.yusin.quiz.subject.infrastructure.SubjectRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class AdminContentServiceTest {
    private final AdminContentService service = new AdminContentService(
            mock(QualificationExamRepository.class), mock(QualificationExamSubjectRepository.class),
            mock(SubjectRepository.class), mock(ExamRepository.class), mock(ProblemRepository.class),
            mock(JsonBlockContentProcessor.class), mock(JsonBlockContentValidator.class));

    @Test
    void problemMustHaveAtLeastOneAnswer() {
        ProblemRequest request = new ProblemRequest(1L, 1L, 1, ContentStatus.PUBLISHED,
                List.of(Map.of("type", "text", "text", "문제")), List.of(), List.of(
                choice(1, false), choice(2, false), choice(3, false), choice(4, false), choice(5, false)
        ));

        assertThatThrownBy(() -> service.createProblem(request))
                .isInstanceOf(ContentException.class)
                .hasMessageContaining("정답은 최소 하나 이상");
    }

    @Test
    void problemChoicesMustBeSequentiallyNumbered() {
        ProblemRequest request = new ProblemRequest(1L, 1L, 1, ContentStatus.PUBLISHED,
                List.of(Map.of("type", "text", "text", "문제")), List.of(), List.of(
                choice(1, true), choice(3, false), choice(4, false), choice(5, false)
        ));

        assertThatThrownBy(() -> service.createProblem(request))
                .isInstanceOf(ContentException.class)
                .hasMessageContaining("보기는 1번부터 연속");
    }

    private ChoiceRequest choice(int number, boolean answer) {
        return new ChoiceRequest(null, number, "보기 " + number, answer, List.of());
    }
}
