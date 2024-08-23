package com.cpa.yusin.quiz.exam.controller.dto.request;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ExamCreateRequest
{
    private String name;
    private int year;
}
