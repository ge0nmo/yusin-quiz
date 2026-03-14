package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.global.exception.ChoiceException;
import com.cpa.yusin.quiz.global.exception.ProblemException;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.domain.Problem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProblemServiceTest extends MockSetup {

    @Test
    @DisplayName("문제 생성은 문제와 choice 를 함께 저장한다")
    void processSaveOrUpdate_whenCreate_thenSaveProblemAndChoices() {
        List<ChoiceRequest> choices = List.of(
                ChoiceRequest.builder().content("problem1-choice1").number(1).isAnswer(true).build(),
                ChoiceRequest.builder().content("problem1-choice2").number(2).isAnswer(false).build()
        );

        ProblemRequest request = ProblemRequest.builder()
                .content("problem1")
                .explanation("explanation1")
                .number(1)
                .choices(choices)
                .build();

        ProblemDTO result = testContainer.problemService.processSaveOrUpdate(request, biologyExam2.getId());

        assertThat(result.content()).isEqualTo("problem1");
        assertThat(result.explanation()).isEqualTo("explanation1");
        assertThat(testContainer.problemRepository.findAllByExamId(biologyExam2.getId())).hasSize(1);
        assertThat(testContainer.choiceRepository.findAllByExamId(biologyExam2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("문제 수정은 같은 시험 안에서 번호 충돌을 허용하지 않는다")
    void processSaveOrUpdate_whenProblemNumberCollides_thenThrow() {
        testContainer.problemRepository.save(Problem.builder()
                .id(10L)
                .number(10)
                .content("first")
                .exam(biologyExam1)
                .build());
        Problem secondProblem = testContainer.problemRepository.save(Problem.builder()
                .id(11L)
                .number(11)
                .content("second")
                .exam(biologyExam1)
                .build());

        ProblemRequest request = ProblemRequest.builder()
                .id(secondProblem.getId())
                .number(10)
                .content("updated")
                .explanation("updated explanation")
                .choices(List.of())
                .build();

        assertThatThrownBy(() -> testContainer.problemService.processSaveOrUpdate(request, biologyExam1.getId()))
                .isInstanceOf(ProblemException.class);
    }

    @Test
    @DisplayName("문제 수정은 다른 문제의 choice ID 를 재사용할 수 없다")
    void processSaveOrUpdate_whenChoiceBelongsToDifferentProblem_thenThrow() {
        Problem targetProblem = testContainer.problemRepository.save(Problem.builder()
                .id(10L)
                .number(10)
                .content("알맞은 것을 고르시오")
                .explanation("설명")
                .exam(biologyExam1)
                .build());
        Problem foreignProblem = testContainer.problemRepository.save(Problem.builder()
                .id(11L)
                .number(11)
                .content("다른 문제")
                .explanation("다른 설명")
                .exam(biologyExam1)
                .build());

        Choice foreignChoice = testContainer.choiceRepository.save(Choice.builder()
                .id(50L)
                .number(1)
                .content("다른 문제 보기")
                .isAnswer(true)
                .problem(foreignProblem)
                .build());

        ProblemRequest request = ProblemRequest.builder()
                .id(targetProblem.getId())
                .number(10)
                .content("수정된 문제")
                .explanation("수정된 설명")
                .choices(List.of(
                        ChoiceRequest.builder()
                                .id(foreignChoice.getId())
                                .number(1)
                                .content("잘못된 재사용")
                                .isAnswer(true)
                                .build()
                ))
                .build();

        assertThatThrownBy(() -> testContainer.problemService.processSaveOrUpdate(request, biologyExam1.getId()))
                .isInstanceOf(ChoiceException.class);
    }

    @Test
    @DisplayName("문제 생성은 정답 choice 가 둘 이상이면 거부한다")
    void processSaveOrUpdate_whenMultipleCorrectChoicesExist_thenThrow() {
        ProblemRequest request = ProblemRequest.builder()
                .content("problem1")
                .explanation("explanation1")
                .number(1)
                .choices(List.of(
                        ChoiceRequest.builder().content("problem1-choice1").number(1).isAnswer(true).build(),
                        ChoiceRequest.builder().content("problem1-choice2").number(2).isAnswer(true).build()
                ))
                .build();

        assertThatThrownBy(() -> testContainer.problemService.processSaveOrUpdate(request, biologyExam2.getId()))
                .isInstanceOf(ChoiceException.class);
    }

    @Test
    @DisplayName("문제 조회는 lecture playbackUrl 을 포함한다")
    void getById_whenProblemHasLecture_thenReturnsLectureResponse() {
        physicsProblem1.assignLecture("https://www.youtube.com/watch?v=abc123XYZ09", 430);

        ProblemDTO result = testContainer.problemService.getById(physicsProblem1.getId());

        assertThat(result.lecture()).isNotNull();
        assertThat(result.lecture().getYoutubeUrl()).isEqualTo("https://www.youtube.com/watch?v=abc123XYZ09");
        assertThat(result.lecture().getStartTimeSecond()).isEqualTo(430);
        assertThat(result.lecture().getPlaybackUrl())
                .isEqualTo("https://www.youtube.com/watch?v=abc123XYZ09&t=430s");
    }
}
