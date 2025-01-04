package com.cpa.yusin.quiz.exam.service;

import com.cpa.yusin.quiz.common.service.CascadeDeleteService;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamDTO;
import com.cpa.yusin.quiz.exam.controller.mapper.ExamMapper;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.exam.service.port.ExamValidator;
import com.cpa.yusin.quiz.global.exception.ExamException;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import com.cpa.yusin.quiz.subject.domain.Subject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@Service
public class ExamServiceImpl implements ExamService
{
    private final ExamRepository examRepository;
    private final ExamMapper examMapper;
    private final SubjectService subjectService;
    private final CascadeDeleteService cascadeDeleteService;
    private final ExamValidator examValidator;

    @Transactional
    @Override
    public ExamCreateResponse save(long subjectId, ExamCreateRequest request)
    {
        Subject subject = subjectService.findById(subjectId);
        examValidator.validate(subjectId, request.getName(), request.getYear());

        Exam exam = Exam.from(request, subject.getId());
        exam = examRepository.save(exam);

        return examMapper.toCreateResponse(exam);
    }

    @Transactional
    @Override
    public void update(long examId, ExamUpdateRequest request)
    {
        Exam domain = findById(examId);

        examValidator.validate(examId, domain.getId(), request.getName(), request.getYear());
        domain.update(request);

        examRepository.save(domain);
    }

    @Override
    public Exam findById(long id)
    {
        return examRepository.findById(id)
                .orElseThrow(() -> new ExamException(ExceptionMessage.EXAM_NOT_FOUND));
    }

    @Override
    public ExamDTO getById(long id)
    {
        Exam domain = findById(id);

        return examMapper.toExamDTO(domain);
    }

    @Override
    public List<ExamDTO> getAllBySubjectId(long subjectId, int year)
    {
        subjectService.findById(subjectId);

        List<Exam> exams = examRepository.findAllBySubjectId(subjectId, year);

        if(exams.isEmpty())
            return Collections.emptyList();

        return exams.stream()
                .sorted(Comparator.comparing(Exam::getYear).reversed()
                        .thenComparing(Exam::getName))
                .map(this.examMapper::toExamDTO)
                .toList();
    }

    @Override
    public List<Integer> getAllYearsBySubjectId(long subjectId)
    {
        return examRepository.getYearsBySubjectId(subjectId);
    }

    @Override
    public List<Exam> getAllBySubjectId(long subjectId)
    {
        return examRepository.findAllBySubjectId(subjectId);
    }

    @Transactional
    @Override
    public void deleteById(List<Long> ids)
    {
        ids.forEach(id -> {
            findById(id);
            cascadeDeleteService.deleteExamByExamId(id);
        });
    }

    @Transactional
    @Override
    public void deleteById(long id)
    {
        cascadeDeleteService.deleteExamByExamId(id);
    }

}
