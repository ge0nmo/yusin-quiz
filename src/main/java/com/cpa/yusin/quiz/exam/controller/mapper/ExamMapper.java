package com.cpa.yusin.quiz.exam.controller.mapper;

import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamDTO;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;

import java.util.List;

public interface ExamMapper
{
    ExamCreateResponse toCreateResponse(ExamDomain domain, SubjectDTO subjectDTO);

    ExamDTO toExamDTO(ExamDomain domain);

    List<ExamDTO> toExamDTOs(List<ExamDomain> domains);
}
