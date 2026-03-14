package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.global.exception.ProblemException;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemLectureRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemSaveV2Request;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.domain.block.TextBlock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateProblemV2ServiceTest extends MockSetup {

    @Test
    @DisplayName("V2 생성은 lecture URL 을 canonical 형태로 저장한다")
    void saveOrUpdateV2_whenCreateWithLecture_thenStoresCanonicalYoutubeUrlAndStartTime() {
        ProblemSaveV2Request request = ProblemSaveV2Request.builder()
                .number(1)
                .content(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanation(List.of(TextBlock.builder().type("text").tag("p").build()))
                .lecture(ProblemLectureRequest.builder()
                        .youtubeUrl("https://youtu.be/abc123XYZ09?t=430")
                        .startTimeSecond(430)
                        .build())
                .choices(List.of())
                .build();

        testContainer.createProblemV2Service.saveOrUpdateV2(biologyExam2.getId(), request);

        Problem savedProblem = testContainer.problemRepository.findAllByExamId(biologyExam2.getId()).getFirst();
        assertThat(savedProblem.getLectureYoutubeUrl()).isEqualTo("https://www.youtube.com/watch?v=abc123XYZ09");
        assertThat(savedProblem.getLectureStartSecond()).isEqualTo(430);
    }

    @Test
    @DisplayName("V2 수정에서 lecture 가 null 이면 기존 lecture 를 제거한다")
    void saveOrUpdateV2_whenLectureIsNull_thenClearsExistingLecture() {
        Problem savedProblem = testContainer.problemRepository.save(Problem.builder()
                .id(20L)
                .number(1)
                .contentJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanationJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .lectureYoutubeUrl("https://www.youtube.com/watch?v=abc123XYZ09")
                .lectureStartSecond(120)
                .exam(biologyExam2)
                .build());

        ProblemSaveV2Request request = ProblemSaveV2Request.builder()
                .id(savedProblem.getId())
                .number(savedProblem.getNumber())
                .content(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanation(List.of(TextBlock.builder().type("text").tag("p").build()))
                .lecture(null)
                .choices(List.of())
                .build();

        testContainer.createProblemV2Service.saveOrUpdateV2(biologyExam2.getId(), request);

        Problem updatedProblem = testContainer.problemRepository.findById(savedProblem.getId()).orElseThrow();
        assertThat(updatedProblem.getLectureYoutubeUrl()).isNull();
        assertThat(updatedProblem.getLectureStartSecond()).isNull();
    }

    @Test
    @DisplayName("V2 생성은 같은 시험 안에서 문제 번호 충돌을 허용하지 않는다")
    void saveOrUpdateV2_whenCreateNumberCollides_thenThrow() {
        testContainer.problemRepository.save(Problem.builder()
                .id(30L)
                .number(3)
                .contentJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanationJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .exam(biologyExam2)
                .build());

        ProblemSaveV2Request request = ProblemSaveV2Request.builder()
                .number(3)
                .content(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanation(List.of(TextBlock.builder().type("text").tag("p").build()))
                .choices(List.of())
                .build();

        assertThatThrownBy(() -> testContainer.createProblemV2Service.saveOrUpdateV2(biologyExam2.getId(), request))
                .isInstanceOf(ProblemException.class);
    }

    @Test
    @DisplayName("V2 수정은 다른 시험에 속한 문제를 잘못된 examId 로 수정할 수 없다")
    void saveOrUpdateV2_whenProblemDoesNotBelongToExam_thenThrow() {
        Problem savedProblem = testContainer.problemRepository.save(Problem.builder()
                .id(40L)
                .number(4)
                .contentJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanationJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .exam(biologyExam1)
                .build());

        ProblemSaveV2Request request = ProblemSaveV2Request.builder()
                .id(savedProblem.getId())
                .number(4)
                .content(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanation(List.of(TextBlock.builder().type("text").tag("p").build()))
                .choices(List.of())
                .build();

        assertThatThrownBy(() -> testContainer.createProblemV2Service.saveOrUpdateV2(biologyExam2.getId(), request))
                .isInstanceOf(ProblemException.class);
    }
}
