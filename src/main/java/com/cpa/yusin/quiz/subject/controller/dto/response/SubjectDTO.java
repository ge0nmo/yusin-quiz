package com.cpa.yusin.quiz.subject.controller.dto.response;

import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import lombok.Builder;

@Builder
public class SubjectDTO
{
    private long id;
    private String name;

    public static SubjectDTO from(SubjectDomain domain)
    {
        return SubjectDTO.builder()
                .id(domain.getId())
                .name(domain.getName())
                .build();
    }
}
