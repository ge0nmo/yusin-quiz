package com.cpa.yusin.quiz.choice.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
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
        ProblemDomain problemDomain = physicsProblem2;
        Map<ProblemDomain, List<ChoiceRequest>> choiceMap = new HashMap<>();

        List<ChoiceRequest> choices = List.of(
                ChoiceRequest.builder().content("problem1 - choice1").number(1).answer(true).build(),
                ChoiceRequest.builder().content("problem1 - choice2").number(2).answer(false).build(),
                ChoiceRequest.builder().content("problem1 - choice3").number(3).answer(false).build(),
                ChoiceRequest.builder().content("problem1 - choice4").number(4).answer(false).build(),
                ChoiceRequest.builder().content("problem1 - choice5").number(5).answer(false).build()

        );

        choiceMap.put(problemDomain, choices);

        // when
        testContainer.choiceService.saveOrUpdate(choiceMap);

        // then
        List<ChoiceDomain> choiceDomains = testContainer.choiceRepository.findAllByProblemId(problemDomain.getId());
        assertThat(choiceDomains).hasSize(5);

        assertThat(choiceDomains.getFirst().getContent()).isEqualTo("problem1 - choice1");
        assertThat(choiceDomains.getFirst().getNumber()).isEqualTo(1);
        assertThat(choiceDomains.getFirst().isAnswer()).isTrue();

        assertThat(choiceDomains.get(1).getContent()).isEqualTo("problem1 - choice2");
        assertThat(choiceDomains.get(1).getNumber()).isEqualTo(2);
        assertThat(choiceDomains.get(1).isAnswer()).isFalse();

        assertThat(choiceDomains.get(2).getContent()).isEqualTo("problem1 - choice3");
        assertThat(choiceDomains.get(2).getNumber()).isEqualTo(3);
        assertThat(choiceDomains.get(2).isAnswer()).isFalse();

        assertThat(choiceDomains.get(3).getContent()).isEqualTo("problem1 - choice4");
        assertThat(choiceDomains.get(3).getNumber()).isEqualTo(4);
        assertThat(choiceDomains.get(3).isAnswer()).isFalse();

        assertThat(choiceDomains.get(4).getContent()).isEqualTo("problem1 - choice5");
        assertThat(choiceDomains.get(4).getNumber()).isEqualTo(5);
        assertThat(choiceDomains.get(4).isAnswer()).isFalse();
    }

}