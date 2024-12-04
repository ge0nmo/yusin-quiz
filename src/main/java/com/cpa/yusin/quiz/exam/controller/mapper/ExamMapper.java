package com.cpa.yusin.quiz.exam.controller.mapper;

import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamDTO;
import com.cpa.yusin.quiz.exam.domain.Exam;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ExamMapper
{
    public ExamCreateResponse toCreateResponse(Exam domain)
    {
        if(domain == null)
            return null;

        return ExamCreateResponse.builder()
                .id(domain.getId())
                .name(domain.getName())
                .year(domain.getYear())
                .build();
    }

    public ExamDTO toExamDTO(Exam domain)
    {
        if(domain == null)
            return null;

        return ExamDTO.builder()
                .id(domain.getId())
                .name(domain.getName())
                .year(domain.getYear())
                .build();
    }

    public List<ExamDTO> toExamDTOs(List<Exam> domains)
    {
        if(domains == null || domains.isEmpty())
            return Collections.emptyList();

        return domains.stream()
                .map(this::toExamDTO)
                .toList();
    }
}
