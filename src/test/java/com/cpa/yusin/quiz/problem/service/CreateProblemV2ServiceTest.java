package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemLectureRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemSaveV2Request;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.domain.block.TextBlock;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreateProblemV2ServiceTest extends MockSetup {

    @Test
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
}
