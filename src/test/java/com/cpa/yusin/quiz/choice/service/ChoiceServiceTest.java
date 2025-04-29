package com.cpa.yusin.quiz.choice.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.domain.Problem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ChoiceServiceTest extends MockSetup
{
    /**
     * Choice saveOrUpdate => 항상 DB에 저장된 Problem 객체를 받는다
     * Choice request => isNew() true => 새로운 choice 객체 생성
     *                           false => 기존에 저장된 choice 객체 업데이트
     */
    @Test
    void saveOrUpdate_whenIdIsNull_shouldSave()
    {
        // given
        List<ChoiceRequest> choiceRequests = List.of(
                ChoiceRequest.builder().content("보기1").number(1).isAnswer(true).build(),
                ChoiceRequest.builder().content("보기2").number(2).isAnswer(false).build(),
                ChoiceRequest.builder().content("보기3").number(3).isAnswer(false).build(),
                ChoiceRequest.builder().content("보기4").number(4).isAnswer(false).build(),
                ChoiceRequest.builder().content("보기5").number(5).isAnswer(false).build());

        Problem problem = Problem.builder().id(1L).build();

        // when
        List<Choice> result = testContainer.choiceService.saveOrUpdate(choiceRequests, problem);

        // then
        assertThat(result.size()).isEqualTo(5);
        assertThat(result.getFirst().getIsAnswer()).isTrue();
        assertThat(result.getLast().getIsAnswer()).isFalse();
    }

    /**
     * removeYn => true => should delete
     * id => null or -1 => should create new data
     */
    @Test
    void saveOrUpdate_whenIdExists_shouldUpdate()
    {
        // given
        testContainer.choiceRepository.saveAll(List.of(
                Choice.builder().id(1L).content("보기1").number(1).isAnswer(true).build(),
                Choice.builder().id(2L).content("보기2").number(2).isAnswer(false).build(),
                Choice.builder().id(3L).content("보기3").number(3).isAnswer(false).build(),
                Choice.builder().id(4L).content("보기4").number(4).isAnswer(false).build(),
                Choice.builder().id(5L).content("보기5").number(5).isAnswer(false).build()
        ));

        List<ChoiceRequest> choiceRequests = List.of(
                ChoiceRequest.builder().id(1L).content("보기1").number(1).isAnswer(false).build(),
                ChoiceRequest.builder().id(2L).content("보기2").number(2).isAnswer(false).build(),
                ChoiceRequest.builder().id(3L).content("보기3").number(3).isAnswer(false).build(),
                ChoiceRequest.builder().id(4L).content("보기4").number(4).isAnswer(false).build(),
                ChoiceRequest.builder().id(5L).removedYn(true).content("보기4").number(4).isAnswer(false).build(),
                ChoiceRequest.builder().id(-1L).content("보기5").number(5).isAnswer(true).build());

        Problem problem = Problem.builder().id(1L).build();

        // when
        List<Choice> result = testContainer.choiceService.saveOrUpdate(choiceRequests, problem);

        // then
        assertThat(result.size()).isEqualTo(5);
        assertThat(result.getFirst().getIsAnswer()).isFalse();
        assertThat(result.getLast().getIsAnswer()).isTrue();

        Optional<Choice> choice5 = testContainer.choiceRepository.findById(5L);
        assertThat(choice5).isEmpty();
    }

}