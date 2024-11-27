package com.cpa.yusin.quiz.choice.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChoiceUpdateRequest
{
    private long id;

    @NotNull
    private Integer number;

    @NotNull
    private String content;

    @NotNull
    private Boolean isAnswer;

    private Boolean isDeleted;
}
