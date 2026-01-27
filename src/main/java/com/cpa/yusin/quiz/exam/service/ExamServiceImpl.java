package com.cpa.yusin.quiz.exam.service;

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
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@Service
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final ExamMapper examMapper;
    private final SubjectService subjectService;
    private final ExamValidator examValidator;

    @Transactional
    @Override
    public ExamCreateResponse save(long subjectId, ExamCreateRequest request) {
        Subject subject = subjectService.findById(subjectId);
        examValidator.validate(subjectId, request.getName(), request.getYear());

        Exam exam = Exam.from(request.getName(), request.getYear(), subject.getId());
        exam = examRepository.save(exam);

        return examMapper.toCreateResponse(exam);
    }

    @Transactional
    @Override
    public long saveAsAdmin(long subjectId, ExamCreateRequest request) {
        Subject subject = subjectService.findById(subjectId);
        examValidator.validate(subjectId, request.getName(), request.getYear());

        Exam exam = Exam.from(request.getName(), request.getYear(), subject.getId());
        return examRepository.save(exam).getId();
    }

    @Transactional
    @Override
    public void update(long examId, ExamUpdateRequest request) {
        Exam domain = findById(examId);
        examValidator.validate(examId, domain.getId(), request.getName(), request.getYear());
        domain.update(request.getName(), request.getYear());
        examRepository.save(domain);
    }

    @Override
    public Exam findById(long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ExamException(ExceptionMessage.EXAM_NOT_FOUND));
    }

    @Override
    public ExamDTO getById(long id) {
        Exam domain = findById(id);
        return examMapper.toExamDTO(domain);
    }

    @Override
    public List<ExamDTO> getAllBySubjectId(long subjectId, Integer year) {
        // 과목 존재 확인
        subjectService.findById(subjectId);

        List<Exam> exams = year == null ? examRepository.findAllBySubjectId(subjectId)
                : examRepository.findAllBySubjectId(subjectId, year);

        if (exams.isEmpty()) {
            return Collections.emptyList();
        }

        return exams.stream()
                .map(examMapper::toExamDTO)
                .toList();
    }

    @Override
    public List<Integer> getAllYearsBySubjectId(long subjectId) {
        return examRepository.getYearsBySubjectId(subjectId);
    }

}