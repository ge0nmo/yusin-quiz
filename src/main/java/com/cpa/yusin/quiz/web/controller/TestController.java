package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/test")
@RestController
public class TestController
{
    private final ProblemService problemService;

    @PostMapping
    public void test(@RequestParam("examId") long examId) {
        for (int i = 1; i <= 40; i++) {
            problemService.save(examId, ProblemCreateRequest.builder()
                    .number(i)
                    .content("문제 " + i + ": 다음 중 올바른 선택을 고르시오")
                    .choices(List.of(
                            ChoiceCreateRequest.builder().isAnswer(i % 5 == 0).number(1).content("선택지 1: 정답이 아닌 내용").build(),
                            ChoiceCreateRequest.builder().isAnswer(i % 5 == 1).number(2).content("선택지 2: 정답이 아닌 내용").build(),
                            ChoiceCreateRequest.builder().isAnswer(i % 5 == 2).number(3).content("선택지 3: 정답인 내용").build(),
                            ChoiceCreateRequest.builder().isAnswer(i % 5 == 3).number(4).content("선택지 4: 정답이 아닌 내용").build(),
                            ChoiceCreateRequest.builder().isAnswer(i % 5 == 4).number(5).content("선택지 5: 정답이 아닌 내용").build()
                    ))
                    .build());
        }
    }
}
