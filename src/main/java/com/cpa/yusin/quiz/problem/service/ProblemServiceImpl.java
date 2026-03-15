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
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemLectureResponse;
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
public class ProblemServiceImpl implements ProblemService {
    private final ProblemRepository problemRepository;
    private final ProblemMapper problemMapper;
    private final ExamService examService;
    private final ChoiceService choiceService;
    private final ProblemValidator problemValidator;
    private final ProblemNumberSlotManager problemNumberSlotManager;
    private final ProblemContentProcessor problemContentProcessor;
    private final ProblemHtmlImageStorageService problemHtmlImageStorageService;

    @Transactional
    @Override
    public void save(long examId, ProblemCreateRequest request) {
        Exam exam = examService.findById(examId);
        problemValidator.validateCreateNumber(examId, request.getNumber());
        problemNumberSlotManager.releaseRemovedNumberSlot(examId, request.getNumber());

        String cleanContent = problemHtmlImageStorageService.replaceEmbeddedImages(request.getContent());
        String cleanExplanation = problemHtmlImageStorageService.replaceEmbeddedImages(request.getExplanation());

        request.setContent(cleanContent);
        request.setExplanation(cleanExplanation);

        Problem problem = problemMapper.toProblemEntity(request, exam);
        problem = problemRepository.save(problem);

        // Choice는 이미지 처리 없이 그대로 저장
        choiceService.save(problem, request.getChoices());
    }

    @Transactional
    @Override
    public void update(long problemId, ProblemUpdateRequest request, long examId) {
        examService.findById(examId);
        Problem problem = findById(problemId);
        problemValidator.validateBelongsToExam(problem, examId);
        problemValidator.validateUpdateNumber(problem, request.getNumber());
        problemNumberSlotManager.releaseRemovedNumberSlot(examId, request.getNumber());

        String cleanContent = problemHtmlImageStorageService.replaceEmbeddedImages(request.getContent());
        String cleanExplanation = problemHtmlImageStorageService.replaceEmbeddedImages(request.getExplanation());

        problem.update(cleanContent, request.getNumber(), cleanExplanation);
        problemRepository.save(problem);
    }

    @Transactional
    @Override
    public ProblemDTO processSaveOrUpdate(ProblemRequest request, long examId) {
        request.setContent(problemHtmlImageStorageService.replaceEmbeddedImages(request.getContent()));
        request.setExplanation(problemHtmlImageStorageService.replaceEmbeddedImages(request.getExplanation()));

        Exam exam = examService.findById(examId);
        return request.isNew() ? save(request, exam) : update(request);
    }

    private ProblemDTO save(ProblemRequest request, Exam exam) {
        problemValidator.validateCreateNumber(exam.getId(), request.getNumber());
        problemNumberSlotManager.releaseRemovedNumberSlot(exam.getId(), request.getNumber());

        Problem problem = Problem.fromSaveOrUpdate(request.getContent(), request.getExplanation(), request.getNumber(),
                exam);
        problem = problemRepository.save(problem);
        List<Choice> choices = choiceService.saveOrUpdate(request.getChoices(), problem);
        return problemMapper.mapToProblemDTO(problem, choices);
    }

    private ProblemDTO update(ProblemRequest request) {
        Problem problem = findById(request.getId());
        problemValidator.validateUpdateNumber(problem, request.getNumber());
        problemNumberSlotManager.releaseRemovedNumberSlot(problem.getExam().getId(), request.getNumber());
        problem.update(request.getContent(), request.getNumber(), request.getExplanation());
        problem = problemRepository.save(problem);
        List<Choice> choices = choiceService.saveOrUpdate(request.getChoices(), problem);
        return problemMapper.mapToProblemDTO(problem, choices);
    }

    @Override
    public GlobalResponse<List<ProblemDTO>> getAllByExamId(long examId) {
        examService.findPublishedById(examId);

        List<Problem> problems = problemRepository.findAllByExamId(examId);
        Map<Long, List<ChoiceResponse>> choiceMap = choiceService.findAllByExamId(examId);

        List<ProblemDTO> response = problems.stream()
                .map(problem -> {
                    String signedContent = problemContentProcessor.processHtmlWithPresignedUrl(problem.getContent());
                    String signedExplanation = problemContentProcessor
                            .processHtmlWithPresignedUrl(problem.getExplanation());

                    return ProblemDTO.builder()
                            .id(problem.getId())
                            .number(problem.getNumber())
                            .content(signedContent)
                            .explanation(signedExplanation)
                            .lecture(ProblemLectureResponse.from(problem))
                            .choices(choiceMap.get(problem.getId()))
                            .build();
                })
                .toList();

        return new GlobalResponse<>(response);
    }

    @Override
    public ProblemDTO getById(long id) {
        Problem problem = findById(id);
        List<ChoiceResponse> choices = choiceService.getAllByProblemId(problem.getId());

        String signedContent = problemContentProcessor.processHtmlWithPresignedUrl(problem.getContent());
        String signedExplanation = problemContentProcessor.processHtmlWithPresignedUrl(problem.getExplanation());

        return ProblemDTO.builder()
                .id(problem.getId())
                .number(problem.getNumber())
                .content(signedContent)
                .explanation(signedExplanation)
                .lecture(ProblemLectureResponse.from(problem))
                .choices(choices)
                .build();
    }

    @Override
    public Problem findById(long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ProblemException(ExceptionMessage.PROBLEM_NOT_FOUND));
        examService.findPublishedById(problem.getExam().getId());
        return problem;
    }
}
