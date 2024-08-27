package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceCreateRequest;
import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemUpdateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemCreateResponse;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemServiceTest extends MockSetup
{

    @Test
    void save()
    {
        // given
        long examId = physicsExam2.getId();
        /**
         * problem1
         */
        ChoiceCreateRequest problem1Choice1 = ChoiceCreateRequest.builder()
                .content("problem1 choice1")
                .number(1)
                .isAnswer(false)
                .build();

        ChoiceCreateRequest problem1Choice2 = ChoiceCreateRequest.builder()
                .content("problem1 choice2")
                .number(2)
                .isAnswer(false)
                .build();

        ChoiceCreateRequest problem1Choice3 = ChoiceCreateRequest.builder()
                .content("problem1 choice3")
                .number(3)
                .isAnswer(true)
                .build();

        ChoiceCreateRequest problem1Choice4 = ChoiceCreateRequest.builder()
                .content("problem1 choice4")
                .number(4)
                .isAnswer(false)
                .build();

        ProblemCreateRequest problemRequest1 = ProblemCreateRequest.builder()
                .number(1)
                .content("physics problem1")
                .choiceCreateRequests(List.of(problem1Choice1, problem1Choice2, problem1Choice3, problem1Choice4))
                .build();

        /**
         * problem2
         */

        ChoiceCreateRequest problem2Choice1 = ChoiceCreateRequest.builder()
                .content("problem2 choice1")
                .number(1)
                .isAnswer(true)
                .build();

        ChoiceCreateRequest problem2Choice2 = ChoiceCreateRequest.builder()
                .content("problem2 choice2")
                .number(2)
                .isAnswer(false)
                .build();

        ChoiceCreateRequest problem2Choice3 = ChoiceCreateRequest.builder()
                .content("problem2 choice3")
                .number(3)
                .isAnswer(false)
                .build();

        ChoiceCreateRequest problem2Choice4 = ChoiceCreateRequest.builder()
                .content("problem2 choice4")
                .number(4)
                .isAnswer(false)
                .build();

        ProblemCreateRequest problemRequest2 = ProblemCreateRequest.builder()
                .number(2)
                .content("physics problem2")
                .choiceCreateRequests(List.of(problem2Choice1, problem2Choice2, problem2Choice3, problem2Choice4))
                .build();

        List<ProblemCreateRequest> request = List.of(problemRequest1, problemRequest2);

        // when
        List<ProblemCreateResponse> result = testContainer.problemService.save(examId, request);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void update_changeVariables()
    {
        // given
        long examId = physicsExam1.getId();

        testContainer.problemRepository.save(ProblemDomain.builder()
                .id(3L)
                .content("content 3")
                .number(3)
                .exam(physicsExam1)
                .build());

        ProblemUpdateRequest problemRequest1 = ProblemUpdateRequest.builder()
                .id(1L)
                .number(1)
                .content("updated content 1")
                .isDeleted(false)
                .choices(new ArrayList<>())
                .build();

        ProblemUpdateRequest problemRequest2 = ProblemUpdateRequest.builder()
                .id(2L)
                .number(2)
                .content("updated content 2")
                .isDeleted(false)
                .choices(new ArrayList<>())
                .build();

        ProblemUpdateRequest problemRequest3 = ProblemUpdateRequest.builder()
                .id(3L)
                .number(3)
                .content("updated content 3")
                .isDeleted(false)
                .choices(new ArrayList<>())
                .build();

        List<ProblemUpdateRequest> request = List.of(problemRequest1, problemRequest2, problemRequest3);

        // when
        testContainer.problemService.update(request);

        // then
        List<ProblemDomain> result = testContainer.problemRepository.findAllByExamId(examId).stream()
                .sorted(Comparator.comparing(ProblemDomain::getNumber))
                .toList();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getContent()).isEqualTo("updated content 1");
        assertThat(result.get(1).getContent()).isEqualTo("updated content 2");
        assertThat(result.get(2).getContent()).isEqualTo("updated content 3");

    }

    @Test
    void update_removeProblems()
    {
        // given
        long examId = physicsExam1.getId();

        testContainer.problemRepository.save(ProblemDomain.builder()
                .id(3L)
                .content("content 3")
                .number(3)
                .exam(physicsExam1)
                .build());

        ProblemUpdateRequest problemRequest1 = ProblemUpdateRequest.builder()
                .id(1L)
                .number(1)
                .content("updated content 1")
                .isDeleted(true)
                .choices(new ArrayList<>())
                .build();

        ProblemUpdateRequest problemRequest2 = ProblemUpdateRequest.builder()
                .id(2L)
                .number(2)
                .content("updated content 2")
                .isDeleted(true)
                .choices(new ArrayList<>())
                .build();

        ProblemUpdateRequest problemRequest3 = ProblemUpdateRequest.builder()
                .id(3L)
                .number(3)
                .content("updated content 3")
                .isDeleted(true)
                .choices(new ArrayList<>())
                .build();

        List<ProblemUpdateRequest> request = List.of(problemRequest1, problemRequest2, problemRequest3);
        List<ChoiceDomain> choiceDomains = testContainer.choiceRepository.findAllByProblemId(1L);
        assertThat(choiceDomains).hasSize(3);

        // when
        testContainer.problemService.update(request);

        // then
        List<ProblemDomain> result = testContainer.problemRepository.findAllByExamId(examId);
        choiceDomains = testContainer.choiceRepository.findAllByProblemId(1L);

        assertThat(result).isEmpty();
        assertThat(choiceDomains).isEmpty();
    }


    @Test
    void getById()
    {
        // given

        // when

        // then
    }

    @Test
    void findById()
    {
        // given

        // when

        // then
    }
}