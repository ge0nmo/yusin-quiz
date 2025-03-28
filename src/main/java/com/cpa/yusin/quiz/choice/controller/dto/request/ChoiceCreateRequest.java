package com.cpa.yusin.quiz.choice.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChoiceCreateRequest
{
    @NotNull
    private Integer number;
    @NotNull
    private String content;
    @NotNull
    private Boolean isAnswer;
}
