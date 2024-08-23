package com.cpa.yusin.quiz.subject.controller.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubjectDTO
{
    private final long id;
    private final String name;

}
