package com.cpa.yusin.quiz.subject.controller.mapper;

import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectCreateResponse;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;

import java.util.List;

public interface SubjectMapper
{
    SubjectCreateResponse toSubjectCreateResponse(SubjectDomain domain);

    SubjectDTO toSubjectDTO(SubjectDomain domain);

    List<SubjectDTO> toSubjectDTOs(List<SubjectDomain> domains);
}
