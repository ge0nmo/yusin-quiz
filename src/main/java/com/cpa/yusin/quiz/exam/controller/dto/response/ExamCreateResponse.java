package com.cpa.yusin.quiz.exam.controller.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExamCreateResponse
{
    private final long id;
    private final String name;
    private final int year;
    private final int maxProblemCount;
}
