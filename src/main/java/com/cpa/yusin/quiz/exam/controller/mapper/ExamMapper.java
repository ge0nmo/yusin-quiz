package com.cpa.yusin.quiz.exam.controller.mapper;

import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;

public interface ExamMapper
{
    ExamCreateResponse toCreateResponse(ExamDomain domain, SubjectDTO subjectDTO);


}
