package com.cpa.yusin.quiz.subject.controller.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SubjectCreateResponse
{
    private final long id;
    private final String name;
}
