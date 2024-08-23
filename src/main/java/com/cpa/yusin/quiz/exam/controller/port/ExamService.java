package com.cpa.yusin.quiz.exam.controller.port;

import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;

public interface ExamService
{
    ExamCreateResponse save(long subjectId, ExamCreateRequest request);
}
