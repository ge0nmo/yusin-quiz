package com.cpa.yusin.quiz.exam.controller.port;

import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamDTO;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;

import java.util.List;
import java.util.Optional;

public interface ExamService
{
    ExamCreateResponse save(long subjectId, ExamCreateRequest request);

    void update(long examId, ExamUpdateRequest request);

    ExamDomain findById(long id);

    ExamDTO getById(long id);

    List<ExamDTO> getAllBySubjectId(long subjectId);
}
