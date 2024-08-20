package com.cpa.yusin.quiz.subject.controller.dto.response;

import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import lombok.Getter;

@Getter
public class SubjectCreateResponse
{
    private long id;
    private String name;

    public static SubjectCreateResponse from(SubjectDomain domain)
    {
        SubjectCreateResponse response = new SubjectCreateResponse();
        response.id = domain.getId();
        response.name = domain.getName();

        return response;
    }
}
