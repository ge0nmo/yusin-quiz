package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.ProblemException;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemUpdateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.mapper.ProblemMapper;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
        choiceService.save(problem, request.getChoices(), examId);
    }

    @Transactional
    @Override
    public void update(long problemId, ProblemUpdateRequest request, long examId)
    {
        Problem problem = findById(problemId);

        problem.update(request.getContent(), request.getNumber(), request.getExplanation());
        problemRepository.save(problem);
    }

    @Transactional
    @Override
    public ProblemDTO processSaveOrUpdate(ProblemRequest request, long examId)
    {
        Exam exam = examService.findById(examId);

        return request.isNew() ? save(request, exam) : update(request);
    }

    private ProblemDTO save(ProblemRequest request, Exam exam){
        Problem problem = Problem.fromSaveOrUpdate(request, exam);
        problem = problemRepository.save(problem);

        List<Choice> choices = choiceService.saveOrUpdate(request.getChoices(), problem);

        return problemMapper.mapToProblemDTO(problem, choices);
    }

    private ProblemDTO update(ProblemRequest request)
    {
        Problem problem = findById(request.getId());
        problem.update(request.getContent(), request.getNumber(), request.getExplanation());
        problem = problemRepository.save(problem);

        List<Choice> choices = choiceService.saveOrUpdate(request.getChoices(), problem);

        return problemMapper.mapToProblemDTO(problem, choices);
    }


    @Transactional
    @Override
    public void deleteProblem(long problemId, long examId)
    {
        choiceService.deleteAllByProblemId(problemId);
        problemRepository.deleteById(problemId);
    }

    @Override
    public GlobalResponse<List<ProblemDTO>> getAllByExamId(long examId)
    {
        List<Problem> problems = problemRepository.findAllByExamId(examId);
        Map<Long, List<ChoiceResponse>> choiceMap = choiceService.findAllByExamId(examId);

        List<ProblemDTO> response = problems.stream()
                .map(problem -> problemMapper.toProblemDTO(problem, choiceMap.get(problem.getId())))
                .toList();

        return new GlobalResponse<>(response);
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
