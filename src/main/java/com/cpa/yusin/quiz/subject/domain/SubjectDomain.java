package com.cpa.yusin.quiz.subject.domain;

import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectUpdateRequest;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SubjectDomain
{
    private Long id;
    private String name;

    public static SubjectDomain from(SubjectCreateRequest request)
    {
        return SubjectDomain.builder()
                .name(request.getName())
                .build();
    }

    public SubjectDomain update(SubjectUpdateRequest request)
    {
        return SubjectDomain.builder()
                .id(id)
                .name(request.getName())
                .build();
    }
}
