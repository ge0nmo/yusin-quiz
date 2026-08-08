package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemLectureResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemV2Response;
import com.cpa.yusin.quiz.problem.domain.Problem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 일반 V2 문제 조회와 말문제 회차 조회가 같은 외부 payload를 만들 때 사용하는 조립기다.
 * 콘텐츠 이미지 URL 서명과 lecture/choice 필드의 기존 응답 형태를 한 곳에서 보존한다.
 */
@Component
@RequiredArgsConstructor
public class ProblemV2ResponseAssembler {

    private final ProblemContentProcessor problemContentProcessor;

    /** Problem 엔티티와 이미 배치 조회된 choice를 기존 Problem V2 응답 형태로 변환한다. */
    public ProblemV2Response assemble(Problem problem, List<ChoiceResponse> choices) {
        return ProblemV2Response.builder()
                .id(problem.getId())
                .number(problem.getNumber())
                .requiresCalculation(problem.isRequiresCalculation())
                .content(problemContentProcessor.processBlocksWithPresignedUrl(problem.getContentJson()))
                .explanation(problemContentProcessor.processBlocksWithPresignedUrl(problem.getExplanationJson()))
                .lecture(ProblemLectureResponse.from(problem))
                .choices(choices)
                .build();
    }
}
