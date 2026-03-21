package com.cpa.yusin.quiz.exam.controller.port;

import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamDTO;
import com.cpa.yusin.quiz.exam.controller.dto.response.UserExamDTO;
import com.cpa.yusin.quiz.exam.domain.Exam;

import java.util.List;

public interface ExamService
{
    ExamCreateResponse save(long subjectId, ExamCreateRequest request);
    long saveAsAdmin(long subjectId, ExamCreateRequest request);

    void update(long examId, ExamUpdateRequest request);

    Exam findById(long id);

    Exam findPublishedById(long id);

    ExamDTO getById(long id);

    List<UserExamDTO> getAllBySubjectId(long subjectId, Integer year);

    List<ExamDTO> getAllBySubjectIdForAdmin(long subjectId, Integer year);

    List<Integer> getAllYearsBySubjectId(long subjectId);

    List<Integer> getAllYearsBySubjectIdForAdmin(long subjectId);
}
