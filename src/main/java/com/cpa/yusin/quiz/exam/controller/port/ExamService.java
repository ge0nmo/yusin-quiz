package com.cpa.yusin.quiz.exam.controller.port;

import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamDTO;
import com.cpa.yusin.quiz.exam.domain.Exam;

import java.util.List;

public interface ExamService
{
    ExamCreateResponse save(long subjectId, ExamCreateRequest request);
    long saveAsAdmin(long subjectId, ExamCreateRequest request);

    void update(long examId, ExamUpdateRequest request);

    Exam findById(long id);

    ExamDTO getById(long id);

    List<ExamDTO> getAllBySubjectId(long subjectId, Integer year);

    List<Exam> getAllBySubjectId(long subjectId);

    void deleteById(List<Long> ids);

    void deleteById(long id);

    List<Integer> getAllYearsBySubjectId(long subjectId);
}
