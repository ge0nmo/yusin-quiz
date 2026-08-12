package com.cpa.yusin.quiz.content.service;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.content.controller.dto.AdminContentDto.*;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.infrastructure.ExamRepository;
import com.cpa.yusin.quiz.global.exception.ContentException;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.infrastructure.ProblemRepository;
import com.cpa.yusin.quiz.problem.service.JsonBlockContentProcessor;
import com.cpa.yusin.quiz.problem.service.JsonBlockContentValidator;
import com.cpa.yusin.quiz.qualification.domain.QualificationExam;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamSubject;
import com.cpa.yusin.quiz.qualification.infrastructure.QualificationExamRepository;
import com.cpa.yusin.quiz.qualification.infrastructure.QualificationExamSubjectRepository;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.infrastructure.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminContentService {
    private final QualificationExamRepository qualificationExamRepository;
    private final QualificationExamSubjectRepository mappingRepository;
    private final SubjectRepository subjectRepository;
    private final ExamRepository examRepository;
    private final ProblemRepository problemRepository;
    private final JsonBlockContentProcessor blockContentProcessor;
    private final JsonBlockContentValidator blockContentValidator;

    public List<QualificationExamResponse> getQualificationExams() {
        return qualificationExamRepository.findAllByOrderByNameAsc().stream().map(this::toQualificationResponse).toList();
    }

    public QualificationExamResponse getQualificationExam(Long id) {
        return toQualificationResponse(findQualification(id));
    }

    @Transactional
    public QualificationExamResponse createQualificationExam(QualificationExamCreateRequest request) {
        if (qualificationExamRepository.existsByCode(request.code())) {
            throw new ContentException(ExceptionMessage.QUALIFICATION_EXAM_CODE_EXISTS);
        }
        validateDistinctMappings(request.subjects());
        QualificationExam qualificationExam = qualificationExamRepository.save(
                new QualificationExam(request.code(), request.status()));
        saveMappings(qualificationExam, request.subjects());
        return toQualificationResponse(qualificationExam);
    }

    @Transactional
    public QualificationExamResponse updateQualificationExam(Long id, QualificationExamUpdateRequest request) {
        QualificationExam qualificationExam = findQualification(id);
        validateDistinctMappings(request.subjects());
        qualificationExam.update(request.status());
        synchronizeMappings(qualificationExam, request.subjects());
        return toQualificationResponse(qualificationExam);
    }

    @Transactional
    public void deleteQualificationExam(Long id) {
        QualificationExam qualificationExam = findQualification(id);
        if (examRepository.existsByQualificationExamId(id)) {
            throw new ContentException(ExceptionMessage.CONTENT_IN_USE);
        }
        mappingRepository.deleteAllByQualificationExamId(id);
        qualificationExamRepository.delete(qualificationExam);
    }

    public List<SubjectResponse> getSubjects() {
        return subjectRepository.findAllByOrderByNameAsc().stream().map(this::toSubjectResponse).toList();
    }

    public SubjectResponse getSubject(Long id) {
        return toSubjectResponse(findSubject(id));
    }

    @Transactional
    public SubjectResponse createSubject(SubjectRequest request) {
        String name = request.name().trim();
        if (subjectRepository.existsByName(name)) {
            throw new ContentException(ExceptionMessage.SUBJECT_NAME_EXISTS);
        }
        return toSubjectResponse(subjectRepository.save(new Subject(name, request.status())));
    }

    @Transactional
    public SubjectResponse updateSubject(Long id, SubjectRequest request) {
        Subject subject = findSubject(id);
        String name = request.name().trim();
        if (subjectRepository.existsByNameAndIdNot(name, id)) {
            throw new ContentException(ExceptionMessage.SUBJECT_NAME_EXISTS);
        }
        subject.update(name, request.status());
        return toSubjectResponse(subject);
    }

    @Transactional
    public void deleteSubject(Long id) {
        Subject subject = findSubject(id);
        if (mappingRepository.existsBySubjectId(id)) {
            throw new ContentException(ExceptionMessage.CONTENT_IN_USE);
        }
        subjectRepository.delete(subject);
    }

    public List<ExamResponse> getExams(Long qualificationExamId) {
        List<Exam> exams = qualificationExamId == null
                ? examRepository.findAllByOrderByYearDescNameAsc()
                : examRepository.findAllByQualificationExamIdOrderByYearDescNameAsc(qualificationExamId);
        return exams.stream().map(this::toExamResponse).toList();
    }

    public ExamResponse getExam(Long id) {
        return toExamResponse(findExam(id));
    }

    @Transactional
    public ExamResponse createExam(ExamRequest request) {
        QualificationExam qualificationExam = findQualification(request.qualificationExamId());
        validateExamUniqueness(null, qualificationExam.getId(), request.year(), request.name().trim());
        Exam exam = examRepository.save(new Exam(qualificationExam, request.name().trim(), request.year(), request.status()));
        return toExamResponse(exam);
    }

    @Transactional
    public ExamResponse updateExam(Long id, ExamRequest request) {
        Exam exam = findExam(id);
        QualificationExam qualificationExam = findQualification(request.qualificationExamId());
        validateExamUniqueness(id, qualificationExam.getId(), request.year(), request.name().trim());
        if (!Objects.equals(exam.getQualificationExam().getId(), qualificationExam.getId())
                && problemRepository.existsByExamId(id)) {
            throw new ContentException(ExceptionMessage.CONTENT_IN_USE);
        }
        exam.update(qualificationExam, request.name().trim(), request.year(), request.status());
        return toExamResponse(exam);
    }

    @Transactional
    public void deleteExam(Long id) {
        Exam exam = findExam(id);
        if (problemRepository.existsByExamId(id)) {
            throw new ContentException(ExceptionMessage.CONTENT_IN_USE);
        }
        examRepository.delete(exam);
    }

    public List<ProblemSummaryResponse> getProblems(Long qualificationExamId, Long examId, Long subjectId) {
        return problemRepository.searchAdmin(qualificationExamId, examId, subjectId).stream()
                .map(this::toProblemSummary).toList();
    }

    public ProblemDetailResponse getProblem(Long id) {
        return toProblemDetail(findProblem(id));
    }

    public NextProblemNumberResponse getNextProblemNumber(Long examId, Long subjectId) {
        Exam exam = findExam(examId);
        QualificationExamSubject mapping = findMapping(exam, subjectId);
        return new NextProblemNumberResponse(problemRepository.findMaxNumber(examId, mapping.getId()) + 1);
    }

    @Transactional
    public ProblemDetailResponse createProblem(ProblemRequest request) {
        validateChoices(request.choices());
        validateRichContent(request);
        Exam exam = findExam(request.examId());
        QualificationExamSubject mapping = findMapping(exam, request.subjectId());
        validateProblemNumber(null, exam, mapping, request.number());
        Problem problem = new Problem(exam, mapping, request.number(), request.status(), request.content(),
                safeBlocks(request.explanation()));
        problem.replaceChoices(newChoices(request.choices()));
        return toProblemDetail(problemRepository.save(problem));
    }

    @Transactional
    public ProblemDetailResponse updateProblem(Long id, ProblemRequest request) {
        validateChoices(request.choices());
        validateRichContent(request);
        Problem problem = findProblem(id);
        Exam exam = findExam(request.examId());
        QualificationExamSubject mapping = findMapping(exam, request.subjectId());
        validateProblemNumber(id, exam, mapping, request.number());
        problem.update(exam, mapping, request.number(), request.status(), request.content(),
                safeBlocks(request.explanation()));
        Map<Integer, Choice> existing = new HashMap<>();
        problem.getChoices().forEach(choice -> existing.put(choice.getNumber(), choice));
        if (existing.size() != 5) {
            problem.replaceChoices(newChoices(request.choices()));
        } else {
            request.choices().forEach(choiceRequest -> existing.get(choiceRequest.number()).update(
                    choiceRequest.content().trim(), choiceRequest.isAnswer(), safeBlocks(choiceRequest.explanation())));
        }
        return toProblemDetail(problem);
    }

    @Transactional
    public void deleteProblem(Long id) {
        problemRepository.delete(findProblem(id));
    }

    public DashboardResponse getDashboard() {
        return new DashboardResponse(qualificationExamRepository.count(), subjectRepository.count(),
                examRepository.count(), problemRepository.count());
    }

    private void synchronizeMappings(QualificationExam qualificationExam, List<MappingRequest> requests) {
        Map<Long, QualificationExamSubject> existingBySubject = new HashMap<>();
        mappingRepository.findAllByQualificationExamIdOrderByDisplayOrderAscSubjectNameAsc(qualificationExam.getId())
                .forEach(mapping -> existingBySubject.put(mapping.getSubject().getId(), mapping));
        Set<Long> requestedSubjectIds = new HashSet<>();
        for (MappingRequest request : requests) {
            requestedSubjectIds.add(request.subjectId());
            QualificationExamSubject existing = existingBySubject.get(request.subjectId());
            if (existing == null) {
                Subject subject = findSubject(request.subjectId());
                mappingRepository.save(new QualificationExamSubject(qualificationExam, subject,
                        request.status(), request.displayOrder()));
            } else {
                existing.update(request.status(), request.displayOrder());
            }
        }
        for (QualificationExamSubject mapping : existingBySubject.values()) {
            if (!requestedSubjectIds.contains(mapping.getSubject().getId())) {
                if (problemRepository.existsBySubjectMappingId(mapping.getId())) {
                    throw new ContentException(ExceptionMessage.CONTENT_IN_USE);
                }
                mappingRepository.delete(mapping);
            }
        }
    }

    private void saveMappings(QualificationExam qualificationExam, List<MappingRequest> requests) {
        requests.forEach(request -> mappingRepository.save(new QualificationExamSubject(
                qualificationExam, findSubject(request.subjectId()), request.status(), request.displayOrder())));
    }

    private void validateDistinctMappings(List<MappingRequest> mappings) {
        if (mappings.stream().map(MappingRequest::subjectId).distinct().count() != mappings.size()) {
            throw new ContentException(ExceptionMessage.INVALID_DATA);
        }
    }

    private void validateChoices(List<ChoiceRequest> choices) {
        if (choices == null || choices.size() != 5
                || choices.stream().filter(ChoiceRequest::isAnswer).count() != 1
                || !choices.stream().map(ChoiceRequest::number).sorted().toList().equals(List.of(1, 2, 3, 4, 5))) {
            throw new ContentException(ExceptionMessage.INVALID_PROBLEM_CHOICES);
        }
    }

    private void validateRichContent(ProblemRequest request) {
        blockContentValidator.validate(request.content());
        blockContentValidator.validate(request.explanation());
        request.choices().forEach(choice -> blockContentValidator.validate(choice.explanation()));
    }

    private void validateProblemNumber(Long problemId, Exam exam, QualificationExamSubject mapping, int number) {
        boolean exists = problemId == null
                ? problemRepository.existsByExamIdAndSubjectMappingIdAndNumber(exam.getId(), mapping.getId(), number)
                : problemRepository.existsByExamIdAndSubjectMappingIdAndNumberAndIdNot(
                        exam.getId(), mapping.getId(), number, problemId);
        if (exists) {
            throw new ContentException(ExceptionMessage.PROBLEM_NUMBER_EXISTS);
        }
    }

    private void validateExamUniqueness(Long examId, Long qualificationExamId, int year, String name) {
        boolean exists = examId == null
                ? examRepository.existsByQualificationExamIdAndYearAndName(qualificationExamId, year, name)
                : examRepository.existsByQualificationExamIdAndYearAndNameAndIdNot(
                        qualificationExamId, year, name, examId);
        if (exists) {
            throw new ContentException(ExceptionMessage.EXAM_DUPLICATED);
        }
    }

    private List<Choice> newChoices(List<ChoiceRequest> requests) {
        return requests.stream()
                .sorted(Comparator.comparingInt(ChoiceRequest::number))
                .map(request -> new Choice(request.number(), request.content().trim(), request.isAnswer(),
                        safeBlocks(request.explanation())))
                .toList();
    }

    private QualificationExamSubject findMapping(Exam exam, Long subjectId) {
        return mappingRepository.findByQualificationExamIdAndSubjectId(exam.getQualificationExam().getId(), subjectId)
                .orElseThrow(() -> new ContentException(ExceptionMessage.INVALID_SUBJECT_MAPPING));
    }

    private QualificationExam findQualification(Long id) {
        return qualificationExamRepository.findById(id)
                .orElseThrow(() -> new ContentException(ExceptionMessage.QUALIFICATION_EXAM_NOT_FOUND));
    }

    private Subject findSubject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ContentException(ExceptionMessage.SUBJECT_NOT_FOUND));
    }

    private Exam findExam(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ContentException(ExceptionMessage.EXAM_NOT_FOUND));
    }

    private Problem findProblem(Long id) {
        return problemRepository.findDetailById(id)
                .orElseThrow(() -> new ContentException(ExceptionMessage.PROBLEM_NOT_FOUND));
    }

    private QualificationExamResponse toQualificationResponse(QualificationExam qualificationExam) {
        List<MappingResponse> mappings = mappingRepository
                .findAllByQualificationExamIdOrderByDisplayOrderAscSubjectNameAsc(qualificationExam.getId()).stream()
                .map(mapping -> new MappingResponse(mapping.getId(), mapping.getSubject().getId(),
                        mapping.getSubject().getName(), mapping.getStatus(), mapping.getDisplayOrder(),
                        problemRepository.countPublished(qualificationExam.getCode(), mapping.getSubject().getId())))
                .toList();
        return new QualificationExamResponse(qualificationExam.getId(), qualificationExam.getCode(),
                qualificationExam.getName(), qualificationExam.getStatus(), mappings);
    }

    private SubjectResponse toSubjectResponse(Subject subject) {
        return new SubjectResponse(subject.getId(), subject.getName(), subject.getStatus());
    }

    private ExamResponse toExamResponse(Exam exam) {
        QualificationExam qualification = exam.getQualificationExam();
        return new ExamResponse(exam.getId(), qualification.getId(), qualification.getCode(), qualification.getName(),
                exam.getName(), exam.getYear(), exam.getStatus(), problemRepository.countByExamId(exam.getId()));
    }

    private ProblemSummaryResponse toProblemSummary(Problem problem) {
        long answers = problem.getChoices().stream().filter(Choice::isAnswer).count();
        return new ProblemSummaryResponse(problem.getId(), problem.getExam().getId(), problem.getExam().getName(),
                problem.getExam().getYear(), problem.getExam().getQualificationExam().getId(),
                problem.getExam().getQualificationExam().getName(), problem.getSubjectMapping().getSubject().getId(),
                problem.getSubjectMapping().getSubject().getName(), problem.getNumber(), problem.getStatus(),
                preview(problem.getContent()), problem.getChoices().size(), answers);
    }

    private ProblemDetailResponse toProblemDetail(Problem problem) {
        List<ChoiceDetailResponse> choices = problem.getChoices().stream()
                .sorted(Comparator.comparingInt(Choice::getNumber))
                .map(choice -> new ChoiceDetailResponse(choice.getId(), choice.getNumber(), choice.getContent(),
                        choice.isAnswer(), blockContentProcessor.withFreshImageUrls(choice.getExplanation())))
                .toList();
        return new ProblemDetailResponse(problem.getId(), problem.getExam().getId(),
                problem.getSubjectMapping().getSubject().getId(), problem.getNumber(), problem.getStatus(),
                blockContentProcessor.withFreshImageUrls(problem.getContent()),
                blockContentProcessor.withFreshImageUrls(problem.getExplanation()), choices);
    }

    private String preview(List<Map<String, Object>> blocks) {
        String value = blocks == null ? "" : blocks.toString();
        return value.length() <= 160 ? value : value.substring(0, 160);
    }

    private List<Map<String, Object>> safeBlocks(List<Map<String, Object>> blocks) {
        return blocks == null ? List.of() : blocks;
    }
}
