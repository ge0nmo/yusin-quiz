package com.cpa.yusin.quiz.exam.controller.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExamUpdateRequest
{
    private String name;
    private int year;
}
