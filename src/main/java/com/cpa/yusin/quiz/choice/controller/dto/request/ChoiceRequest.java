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
    private Integer number;
    @NotNull
    private String content;
    @NotNull
    private Boolean isAnswer;

    private boolean removedYn;

    public boolean isNew()
    {
        return !removedYn;
    }

}
