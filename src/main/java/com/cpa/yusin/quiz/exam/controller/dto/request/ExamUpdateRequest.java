package com.cpa.yusin.quiz.exam.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamUpdateRequest
{
    private String name;
    private int year;
    private int maxProblemCount;
}
