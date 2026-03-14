package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.ProblemException;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemLectureResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemV2Response;
import com.cpa.yusin.quiz.problem.controller.port.GetProblemV2Service;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetProblemV2ServiceImpl implements GetProblemV2Service {
    private final ProblemRepository problemRepository;
    private final ChoiceService choiceService;
    private final ExamService examService;
    private final ProblemContentProcessor problemContentProcessor; // [Refactor] Add processor

    @Override
    public ProblemV2Response getById(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ProblemException(ExceptionMessage.PROBLEM_NOT_FOUND));

        List<ChoiceResponse> choices = choiceService.getAllByProblemId(problemId);

        return mapToResponse(problem, choices);
    }

    @Override
    public List<ProblemV2Response> getAllByExamId(Long examId) {
        examService.findById(examId);

        // 1. 문제 전체 조회 (Query #1)
        List<Problem> problems = problemRepository.findAllByExamId(examId);

        if (problems.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 조회된 문제들의 ID 리스트 추출
        // 3. 문제들에 해당하는 모든 보기를 한 번에 조회하여 Map으로 변환 (Query #2)
        Map<Long, List<ChoiceResponse>> choicesMap = choiceService.findAllByExamId(examId);

        // 4. 메모리 매핑 (DB 접근 없음)
        return problems.stream()
                .map(problem -> {
                    // Map에서 문제 ID에 맞는 보기를 O(1)로 조회. 없으면 빈 리스트 반환.
                    List<ChoiceResponse> choices = choicesMap.getOrDefault(problem.getId(), Collections.emptyList());

                    return mapToResponse(problem, choices);
                })
                .collect(Collectors.toList());
    }

    // =================================================================
    // Helper Methods
    // =================================================================

    private ProblemV2Response mapToResponse(Problem problem, List<ChoiceResponse> choices) {
        return ProblemV2Response.builder()
                .id(problem.getId())
                .number(problem.getNumber())
                .content(problemContentProcessor.processBlocksWithPresignedUrl(problem.getContentJson())) // [Refactor]
                                                                                                          // Use
                                                                                                          // processor
                .explanation(problemContentProcessor.processBlocksWithPresignedUrl(problem.getExplanationJson())) // [Refactor]
                                                                                                                  // Use
                                                                                                                  // processor
                .lecture(ProblemLectureResponse.from(problem))
                .choices(choices)
                .build();
    }
}
