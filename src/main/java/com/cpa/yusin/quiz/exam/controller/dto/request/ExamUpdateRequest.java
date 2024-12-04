package com.cpa.yusin.quiz.exam.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank
    private String name;

    @NotNull
    private Integer year;
}
