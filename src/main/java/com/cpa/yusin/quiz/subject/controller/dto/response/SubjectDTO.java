package com.cpa.yusin.quiz.subject.controller.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
public class SubjectDTO
{
    private final long id;
    private final String name;

}
