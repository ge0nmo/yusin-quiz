package com.cpa.yusin.quiz.choice.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @NotNull
    private Boolean isDeleted;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public boolean isNew()
    {
        return id == null;
    }
}
