package com.cpa.yusin.quiz.exam.controller.mapper;

import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import org.springframework.stereotype.Component;

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
                .subject(subjectDTO)
                .build();
    }
}
