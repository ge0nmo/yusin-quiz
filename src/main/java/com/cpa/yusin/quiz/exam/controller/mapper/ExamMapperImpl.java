package com.cpa.yusin.quiz.exam.controller.mapper;

import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamDTO;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ExamMapperImpl implements ExamMapper
{
    @Override
    public ExamCreateResponse toCreateResponse(ExamDomain domain, SubjectDTO subjectDTO)
    {
        if(domain == null)
            return null;

        return ExamCreateResponse.builder()
                .id(domain.getId())
                .name(domain.getName())
                .year(domain.getYear())
                .maxProblemCount(domain.getMaxProblemCount())
                .subject(subjectDTO)
                .build();
    }

    @Override
    public ExamDTO toExamDTO(ExamDomain domain)
    {
        if(domain == null)
            return null;

        return ExamDTO.builder()
                .id(domain.getId())
                .name(domain.getName())
                .year(domain.getYear())
                .maxProblemCount(domain.getMaxProblemCount())
                .build();
    }

    @Override
    public List<ExamDTO> toExamDTOs(List<ExamDomain> domains)
    {
        if(domains == null || domains.isEmpty())
            return Collections.emptyList();

        return domains.stream()
                .map(this::toExamDTO)
                .toList();
    }
}
