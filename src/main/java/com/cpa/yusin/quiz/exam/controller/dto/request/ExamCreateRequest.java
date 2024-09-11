package com.cpa.yusin.quiz.exam.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ExamCreateRequest
{
    @NotBlank
    private String name;

    @NotNull
    private int year;

    @NotNull
    private int maxProblemCount;
}
