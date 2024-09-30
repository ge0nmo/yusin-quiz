package com.cpa.yusin.quiz.choice.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.problem.domain.Problem;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ChoiceServiceTest extends MockSetup
{

    @Test
    void saveOrUpdate()
    {
        // given
        Problem problem = physicsProblem2;
        Map<Problem, List<ChoiceRequest>> choiceMap = new HashMap<>();

        List<ChoiceRequest> choices = List.of(
                ChoiceRequest.builder().content("problem1 - choice1").number(1).isAnswer(true).build(),
                ChoiceRequest.builder().content("problem1 - choice2").number(2).isAnswer(false).build(),
                ChoiceRequest.builder().content("problem1 - choice3").number(3).isAnswer(false).build(),
                ChoiceRequest.builder().content("problem1 - choice4").number(4).isAnswer(false).build(),
                ChoiceRequest.builder().content("problem1 - choice5").number(5).isAnswer(false).build()

        );

        choiceMap.put(problem, choices);

        // when
        testContainer.choiceService.saveOrUpdate(choiceMap);

        // then
        List<Choice> choiceDomains = testContainer.choiceRepository.findAllByProblemId(problem.getId());
        assertThat(choiceDomains).hasSize(5);

        assertThat(choiceDomains.getFirst().getContent()).isEqualTo("problem1 - choice1");
        assertThat(choiceDomains.getFirst().getNumber()).isEqualTo(1);
        assertThat(choiceDomains.getFirst().getIsAnswer()).isTrue();

        assertThat(choiceDomains.get(1).getContent()).isEqualTo("problem1 - choice2");
        assertThat(choiceDomains.get(1).getNumber()).isEqualTo(2);
        assertThat(choiceDomains.get(1).getIsAnswer()).isFalse();

        assertThat(choiceDomains.get(2).getContent()).isEqualTo("problem1 - choice3");
        assertThat(choiceDomains.get(2).getNumber()).isEqualTo(3);
        assertThat(choiceDomains.get(2).getIsAnswer()).isFalse();

        assertThat(choiceDomains.get(3).getContent()).isEqualTo("problem1 - choice4");
        assertThat(choiceDomains.get(3).getNumber()).isEqualTo(4);
        assertThat(choiceDomains.get(3).getIsAnswer()).isFalse();

        assertThat(choiceDomains.get(4).getContent()).isEqualTo("problem1 - choice5");
        assertThat(choiceDomains.get(4).getNumber()).isEqualTo(5);
        assertThat(choiceDomains.get(4).getIsAnswer()).isFalse();
    }

}