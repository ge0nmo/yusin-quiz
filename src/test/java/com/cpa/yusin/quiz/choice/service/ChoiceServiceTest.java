package com.cpa.yusin.quiz.choice.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.global.exception.ChoiceException;
import com.cpa.yusin.quiz.problem.domain.Problem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChoiceServiceTest extends MockSetup {

    @Test
    @DisplayName("새 choice 요청은 모두 현재 문제에 연결되어 저장된다")
    void saveOrUpdate_whenChoicesAreNew_thenCreateAll() {
        List<ChoiceRequest> requests = List.of(
                ChoiceRequest.builder().content("보기1").number(1).isAnswer(true).build(),
                ChoiceRequest.builder().content("보기2").number(2).isAnswer(false).build()
        );

        Problem problem = physicsProblem2;

        List<Choice> result = testContainer.choiceService.saveOrUpdate(requests, problem);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(choice -> assertThat(choice.getProblem().getId()).isEqualTo(problem.getId()));
    }

    @Test
    @DisplayName("기존 choice 는 업데이트하고 removedYn 은 삭제하며 신규 choice 는 추가한다")
    void saveOrUpdate_whenChoicesContainCreateUpdateDelete_thenApplyAllOperations() {
        Problem problem = physicsProblem1;
        Choice removableChoice = testContainer.choiceRepository.save(Choice.builder()
                .id(99L)
                .content("삭제 대상")
                .number(99)
                .isAnswer(false)
                .problem(problem)
                .build());

        List<ChoiceRequest> requests = List.of(
                ChoiceRequest.builder().id(choice1.getId()).content("수정된 보기1").number(1).isAnswer(false).build(),
                ChoiceRequest.builder().id(choice2.getId()).content("수정된 보기2").number(2).isAnswer(true).build(),
                ChoiceRequest.builder().id(removableChoice.getId()).removedYn(true).content("삭제 대상").number(99).isAnswer(false).build(),
                ChoiceRequest.builder().id(-1L).content("신규 보기").number(4).isAnswer(false).build()
        );

        List<Choice> result = testContainer.choiceService.saveOrUpdate(requests, problem);

        assertThat(result).hasSize(3);
        assertThat(testContainer.choiceRepository.findById(choice1.getId())).hasValueSatisfying(choice -> {
            assertThat(choice.getContent()).isEqualTo("수정된 보기1");
            assertThat(choice.getIsAnswer()).isFalse();
        });
        assertThat(testContainer.choiceRepository.findById(choice2.getId())).hasValueSatisfying(choice -> {
            assertThat(choice.getContent()).isEqualTo("수정된 보기2");
            assertThat(choice.getIsAnswer()).isTrue();
        });
        assertThat(testContainer.choiceRepository.findById(removableChoice.getId())).isEmpty();
    }

    @Test
    @DisplayName("동일 요청 안에 중복 choice 번호가 있으면 저장을 거부한다")
    void saveOrUpdate_whenChoiceNumbersDuplicate_thenThrow() {
        List<ChoiceRequest> requests = List.of(
                ChoiceRequest.builder().content("보기1").number(1).isAnswer(true).build(),
                ChoiceRequest.builder().content("보기1-중복").number(1).isAnswer(false).build()
        );

        assertThatThrownBy(() -> testContainer.choiceService.saveOrUpdate(requests, physicsProblem1))
                .isInstanceOf(ChoiceException.class);
    }

    @Test
    @DisplayName("활성 choice 안에 정답이 둘 이상이면 저장을 거부한다")
    void saveOrUpdate_whenMultipleCorrectAnswersExist_thenThrow() {
        List<ChoiceRequest> requests = List.of(
                ChoiceRequest.builder().content("보기1").number(1).isAnswer(true).build(),
                ChoiceRequest.builder().content("보기2").number(2).isAnswer(true).build()
        );

        assertThatThrownBy(() -> testContainer.choiceService.saveOrUpdate(requests, physicsProblem1))
                .isInstanceOf(ChoiceException.class);
    }

    @Test
    @DisplayName("다른 문제에 속한 choice 를 현재 문제 수정 요청으로 보내면 차단한다")
    void saveOrUpdate_whenChoiceBelongsToDifferentProblem_thenThrow() {
        Choice foreignChoice = testContainer.choiceRepository.save(Choice.builder()
                .id(101L)
                .content("다른 문제 보기")
                .number(1)
                .isAnswer(false)
                .problem(physicsProblem2)
                .build());

        List<ChoiceRequest> requests = List.of(
                ChoiceRequest.builder()
                        .id(foreignChoice.getId())
                        .content("잘못된 수정")
                        .number(1)
                        .isAnswer(false)
                        .build()
        );

        assertThatThrownBy(() -> testContainer.choiceService.saveOrUpdate(requests, physicsProblem1))
                .isInstanceOf(ChoiceException.class);
    }
}
