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
    private final ProblemContentProcessor problemContentProcessor;

    @Override
    public ProblemV2Response getById(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ProblemException(ExceptionMessage.PROBLEM_NOT_FOUND));
        examService.findPublishedById(problem.getExam().getId());

        List<ChoiceResponse> choices = choiceService.getAllByProblemId(problemId);

        return mapToResponse(problem, choices);
    }

    @Override
    public List<ProblemV2Response> getAllByExamId(Long examId) {
        examService.findPublishedById(examId);
        List<Problem> problems = problemRepository.findAllByExamId(examId);

        if (problems.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<ChoiceResponse>> choicesMap = choiceService.findAllByExamId(examId);

        return problems.stream()
                .map(problem -> {
                    List<ChoiceResponse> choices = choicesMap.getOrDefault(problem.getId(), Collections.emptyList());

                    return mapToResponse(problem, choices);
                })
                .toList();
    }

    @Override
    public ProblemV2Response getByIdForAdmin(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ProblemException(ExceptionMessage.PROBLEM_NOT_FOUND));

        List<ChoiceResponse> choices = choiceService.getAllByProblemId(problemId);

        return mapToResponse(problem, choices);
    }

    @Override
    public List<ProblemV2Response> getAllByExamIdForAdmin(Long examId) {
        examService.findById(examId);
        List<Problem> problems = problemRepository.findAllByExamId(examId);

        if (problems.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<ChoiceResponse>> choicesMap = choiceService.findAllByExamId(examId);

        return problems.stream()
                .map(problem -> {
                    List<ChoiceResponse> choices = choicesMap.getOrDefault(problem.getId(), Collections.emptyList());

                    return mapToResponse(problem, choices);
                })
                .toList();
    }

    private ProblemV2Response mapToResponse(Problem problem, List<ChoiceResponse> choices) {
        return ProblemV2Response.builder()
                .id(problem.getId())
                .number(problem.getNumber())
                .content(problemContentProcessor.processBlocksWithPresignedUrl(problem.getContentJson()))
                .explanation(problemContentProcessor.processBlocksWithPresignedUrl(problem.getExplanationJson()))
                .lecture(ProblemLectureResponse.from(problem))
                .choices(choices)
                .build();
    }
}
