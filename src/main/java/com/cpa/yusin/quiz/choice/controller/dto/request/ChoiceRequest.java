package com.cpa.yusin.quiz.choice.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ChoiceRequest
{
    private Long id;
    @NotNull
    private int number;
    @NotNull
    private String content;
    @NotNull
    private boolean answer;
    @NotNull
    private boolean deleted;

    public boolean isNew()
    {
        return id == null;
    }
}
