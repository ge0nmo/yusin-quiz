package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.ProblemException;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemUpdateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.controller.mapper.ProblemMapper;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProblemServiceImpl implements ProblemService
{
    private final ProblemRepository problemRepository;
    private final ProblemMapper problemMapper;
    private final ExamService examService;
    private final ChoiceService choiceService;

    @Transactional
    @Override
    public void save(long examId, ProblemCreateRequest request)
    {
        Exam exam = examService.findById(examId);

        Problem problem = problemMapper.toProblemEntity(request, exam);

        problem = problemRepository.save(problem);
        choiceService.save(problem, request.getChoices());
    }

    @Transactional
    @Override
    public void update(long problemId, ProblemUpdateRequest request)
    {
        Problem problem = findById(problemId);

        problem.update(request.getContent(), request.getNumber(), request.getExplanation());
        problemRepository.save(problem);
    }

    @Transactional
    @Override
    public ProblemDTO saveOrUpdate(ProblemRequest request, long examId)
    {
        Exam exam = examService.findById(examId);

        Problem problem =  request.isNew() ? save(request, examId) : update(request);

        List<Choice> choices = choiceService.saveOrUpdate(request.getChoices(), problem);
        return problemMapper.toProblemDTO2(problem, choices);
    }

    private Problem save(ProblemRequest request, long examId){
        Exam exam = examService.findById(examId);

        Problem problem = Problem.fromSaveOrUpdate(request, exam);

        return problemRepository.save(problem);
    }

    private Problem update(ProblemRequest request)
    {
        Problem problem = findById(request.getId());
        problem.update(request.getContent(), request.getNumber(), request.getExplanation());
        return problemRepository.save(problem);
    }


    @Transactional
    @Override
    public void deleteProblem(long problemId)
    {
        choiceService.deleteAllByProblemId(problemId);
        problemRepository.deleteById(problemId);
    }

    @Override
    public List<ProblemResponse> getAllByExamId(long examId)
    {
        List<Problem> problems = problemRepository.findAllByExamId(examId);
        Map<Long, List<ChoiceResponse>> choiceMap = choiceService.findAllByExamId(examId);

        return problems.stream()
                .map(problem -> problemMapper.toResponse(problem, choiceMap.get(problem.getId())))
                .toList();
    }


    @Override
    public ProblemDTO getById(long id)
    {
        Problem problem = findById(id);

        List<ChoiceResponse> choices = choiceService.getAllByProblemId(problem.getId());

        return problemMapper.toProblemDTO(problem, choices);
    }

    @Override
    public Problem findById(long id)
    {
        return problemRepository.findById(id)
                .orElseThrow(() -> new ProblemException(ExceptionMessage.PROBLEM_NOT_FOUND));
    }
}
