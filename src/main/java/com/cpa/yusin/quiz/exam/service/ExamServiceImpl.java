package com.cpa.yusin.quiz.exam.service;

import com.cpa.yusin.quiz.common.service.CascadeDeleteService;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamDTO;
import com.cpa.yusin.quiz.exam.controller.mapper.ExamMapper;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.exam.service.port.ExamValidator;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.subject.controller.mapper.SubjectMapper;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
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
        SubjectDomain subject = subjectService.findById(subjectId);
        examValidator.validate(subjectId, request.getName(), request.getYear());

        ExamDomain exam = ExamDomain.from(request, subject.getId());
        exam = examRepository.save(exam);

        return examMapper.toCreateResponse(exam);
    }

    @Transactional
    @Override
    public void update(long examId, ExamUpdateRequest request)
    {
        ExamDomain domain = findById(examId);

        examValidator.validate(examId, domain.getId(), request.getName(), request.getYear());
        domain.update(request);

        examRepository.save(domain);
    }

    @Override
    public ExamDomain findById(long id)
    {
        return examRepository.findById(id)
                .orElseThrow(() -> new GlobalException(ExceptionMessage.EXAM_NOT_FOUND));
    }

    @Override
    public ExamDTO getById(long id)
    {
        ExamDomain domain = findById(id);

        return examMapper.toExamDTO(domain);
    }

    @Override
    public List<ExamDTO> getAllBySubjectId(long subjectId, int year)
    {
        subjectService.findById(subjectId);

        List<ExamDomain> examDomains = examRepository.findAllBySubjectId(subjectId, year);

        if(examDomains.isEmpty())
            return Collections.emptyList();

        return examDomains.stream()
                .sorted(Comparator.comparing(ExamDomain::getYear).reversed()
                        .thenComparing(ExamDomain::getName))
                .map(this.examMapper::toExamDTO)
                .toList();
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


}
