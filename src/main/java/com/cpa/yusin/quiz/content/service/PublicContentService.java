package com.cpa.yusin.quiz.content.service;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.common.domain.ContentStatus;
import com.cpa.yusin.quiz.content.controller.dto.PublicContentDto.*;
import com.cpa.yusin.quiz.global.exception.ContentException;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.infrastructure.ProblemRepository;
import com.cpa.yusin.quiz.problem.service.JsonBlockContentProcessor;
import com.cpa.yusin.quiz.qualification.domain.QualificationExam;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamSubject;
import com.cpa.yusin.quiz.qualification.infrastructure.QualificationExamRepository;
import com.cpa.yusin.quiz.qualification.infrastructure.QualificationExamSubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicContentService {
    private final QualificationExamRepository qualificationExamRepository;
    private final QualificationExamSubjectRepository mappingRepository;
    private final ProblemRepository problemRepository;
    private final JsonBlockContentProcessor blockContentProcessor;

    public List<SubjectResponse> getSubjects(String code) {
        QualificationExam qualificationExam = findPublishedQualification(code);
        return mappingRepository.findAllByQualificationExamIdOrderByDisplayOrderAscSubjectNameAsc(qualificationExam.getId())
                .stream()
                .filter(mapping -> mapping.getStatus() == ContentStatus.PUBLISHED)
                .filter(mapping -> mapping.getSubject().getStatus() == ContentStatus.PUBLISHED)
                .map(mapping -> new SubjectResponse(mapping.getSubject().getId(), mapping.getSubject().getName(),
                        problemRepository.countPublished(qualificationExam.getCode(), mapping.getSubject().getId())))
                .toList();
    }

    public List<ProblemResponse> getProblems(String code, Long subjectId) {
        validatePublishedMapping(code, subjectId);
        return problemRepository.findPublished(normalizeCode(code), subjectId).stream().map(this::toProblemResponse).toList();
    }

    public CheckResponse check(String code, Long problemId, CheckRequest request) {
        Problem problem = findPublishedProblem(code, problemId);
        if (request == null || request.selectedChoiceId() == null) {
            throw new ContentException(ExceptionMessage.CHOICE_NOT_FOUND);
        }
        Choice choice = problem.getChoices().stream()
                .filter(candidate -> candidate.getId().equals(request.selectedChoiceId()))
                .findFirst()
                .orElseThrow(() -> new ContentException(ExceptionMessage.CHOICE_NOT_FOUND));
        return new CheckResponse(choice.isAnswer());
    }

    public List<SolutionResponse> getSolutions(String code, SolutionsRequest request) {
        if (request == null || request.problemIds() == null || request.problemIds().isEmpty()
                || request.problemIds().size() > 5
                || request.problemIds().stream().anyMatch(Objects::isNull)
                || request.problemIds().stream().distinct().count() != request.problemIds().size()) {
            throw new ContentException(ExceptionMessage.INVALID_DATA);
        }
        return request.problemIds().stream()
                .map(id -> toSolutionResponse(findPublishedProblem(code, id)))
                .toList();
    }

    private ProblemResponse toProblemResponse(Problem problem) {
        List<ChoiceResponse> choices = problem.getChoices().stream()
                .sorted(Comparator.comparingInt(Choice::getNumber))
                .map(choice -> new ChoiceResponse(choice.getId(), choice.getNumber(), choice.getContent()))
                .toList();
        ExamResponse exam = new ExamResponse(problem.getExam().getId(), problem.getExam().getName(),
                problem.getExam().getYear());
        return new ProblemResponse(problem.getId(), problem.getNumber(),
                blockContentProcessor.withFreshImageUrls(problem.getContent()), exam, choices);
    }

    private SolutionResponse toSolutionResponse(Problem problem) {
        Choice correct = problem.getChoices().stream().filter(Choice::isAnswer).findFirst()
                .orElseThrow(() -> new ContentException(ExceptionMessage.INVALID_PROBLEM_CHOICES));
        List<ChoiceSolutionResponse> choiceSolutions = problem.getChoices().stream()
                .sorted(Comparator.comparingInt(Choice::getNumber))
                .map(choice -> new ChoiceSolutionResponse(choice.getId(),
                        blockContentProcessor.withFreshImageUrls(choice.getExplanation())))
                .toList();
        return new SolutionResponse(problem.getId(), correct.getId(),
                blockContentProcessor.withFreshImageUrls(problem.getExplanation()), choiceSolutions);
    }

    private QualificationExam findPublishedQualification(String code) {
        QualificationExam qualificationExam = qualificationExamRepository.findByCode(normalizeCode(code))
                .orElseThrow(() -> new ContentException(ExceptionMessage.QUALIFICATION_EXAM_NOT_FOUND));
        if (qualificationExam.getStatus() != ContentStatus.PUBLISHED) {
            throw new ContentException(ExceptionMessage.QUALIFICATION_EXAM_NOT_FOUND);
        }
        return qualificationExam;
    }

    private void validatePublishedMapping(String code, Long subjectId) {
        QualificationExam qualificationExam = findPublishedQualification(code);
        QualificationExamSubject mapping = mappingRepository
                .findByQualificationExamIdAndSubjectId(qualificationExam.getId(), subjectId)
                .orElseThrow(() -> new ContentException(ExceptionMessage.SUBJECT_MAPPING_NOT_FOUND));
        if (mapping.getStatus() != ContentStatus.PUBLISHED
                || mapping.getSubject().getStatus() != ContentStatus.PUBLISHED) {
            throw new ContentException(ExceptionMessage.SUBJECT_MAPPING_NOT_FOUND);
        }
    }

    private Problem findPublishedProblem(String code, Long problemId) {
        Problem problem = problemRepository.findDetailById(problemId)
                .orElseThrow(() -> new ContentException(ExceptionMessage.PROBLEM_NOT_FOUND));
        String normalizedCode = normalizeCode(code);
        if (!problem.getExam().getQualificationExam().getCode().equals(normalizedCode)
                || problem.getStatus() != ContentStatus.PUBLISHED
                || problem.getExam().getStatus() != ContentStatus.PUBLISHED
                || problem.getExam().getQualificationExam().getStatus() != ContentStatus.PUBLISHED
                || problem.getSubjectMapping().getStatus() != ContentStatus.PUBLISHED
                || problem.getSubjectMapping().getSubject().getStatus() != ContentStatus.PUBLISHED) {
            throw new ContentException(ExceptionMessage.PROBLEM_NOT_FOUND);
        }
        return problem;
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ContentException(ExceptionMessage.QUALIFICATION_EXAM_NOT_FOUND);
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

}
