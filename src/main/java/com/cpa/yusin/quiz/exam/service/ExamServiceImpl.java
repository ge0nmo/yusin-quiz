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
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@Service
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final ExamMapper examMapper;
    private final SubjectService subjectService;
    private final CascadeDeleteService cascadeDeleteService;
    private final ExamValidator examValidator;

    @Transactional
    @Override
    public ExamCreateResponse save(long subjectId, ExamCreateRequest request) {
        Subject subject = subjectService.findById(subjectId);
        examValidator.validate(subjectId, request.getName(), request.getYear());

        Exam exam = Exam.from(request, subject.getId());
        exam = examRepository.save(exam);

        return examMapper.toCreateResponse(exam);
    }

    @Transactional
    @Override
    public long saveAsAdmin(long subjectId, ExamCreateRequest request) {
        Subject subject = subjectService.findById(subjectId);
        examValidator.validate(subjectId, request.getName(), request.getYear());

        Exam exam = Exam.from(request, subject.getId());
        return examRepository.save(exam).getId();
    }

    @Transactional
    @Override
    public void update(long examId, ExamUpdateRequest request) {
        Exam domain = findById(examId);
        examValidator.validate(examId, domain.getId(), request.getName(), request.getYear());
        domain.update(request);
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

        List<Exam> exams;

        // year가 null이면 전체 조회, 있으면 필터링 조회
        if (year == null) {
            // Repository에 findAllBySubjectIdOrderByYearDesc 메서드가 있다고 가정 (또는 JPA naming convention)
            exams = examRepository.findAllBySubjectId(subjectId);
            // 만약 Repository가 정렬을 지원하지 않는다면 여기서 Java Sorting을 해도 되지만,
            // 가능하다면 Repository 쿼리 메서드를 `findAllBySubjectIdOrderByYearDesc(subjectId)`로 만드는 것을 추천함.
        } else {
            exams = examRepository.findAllBySubjectId(subjectId, year);
        }

        if (exams.isEmpty()) {
            return Collections.emptyList();
        }

        return exams.stream()
                .sorted((e1, e2) -> {
                    int yearCompare = Integer.compare(e2.getYear(), e1.getYear()); // 내림차순
                    if (yearCompare != 0) return yearCompare;
                    return e1.getName().compareTo(e2.getName()); // 이름은 오름차순
                })
                .map(examMapper::toExamDTO)
                .toList();
    }

    @Override
    public List<Integer> getAllYearsBySubjectId(long subjectId) {
        return examRepository.getYearsBySubjectId(subjectId);
    }

    @Override
    public List<Exam> getAllBySubjectId(long subjectId) {
        return examRepository.findAllBySubjectId(subjectId);
    }

    @Transactional
    @Override
    public void deleteById(long id) {
        cascadeDeleteService.deleteExamByExamId(id);
    }

    // deleteById(List<Long> ids)는 Interface에 정의되어 있다면 구현 유지
    @Transactional
    @Override
    public void deleteById(List<Long> ids) {
        ids.forEach(this::deleteById);
    }
}