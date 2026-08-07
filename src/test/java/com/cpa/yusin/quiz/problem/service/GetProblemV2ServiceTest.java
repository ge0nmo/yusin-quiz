package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemV2Response;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.domain.block.TextBlock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetProblemV2ServiceTest extends MockSetup {

    @Test
    @DisplayName("V2 문제 조회 시 requiresCalculation 필드를 응답 DTO에 바르게 매핑한다")
    void getById_mapsRequiresCalculationToResponse() {
        Problem savedProblem = testContainer.problemRepository.save(Problem.builder()
                .id(100L)
                .number(10)
                .contentJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanationJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .requiresCalculation(true)
                .exam(biologyExam1)
                .build());

        ProblemV2Response response = testContainer.getProblemV2Service.getById(savedProblem.getId());

        assertThat(response.isRequiresCalculation()).isTrue();
    }
}
