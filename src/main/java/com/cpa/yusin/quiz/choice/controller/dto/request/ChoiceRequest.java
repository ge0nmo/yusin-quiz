package com.cpa.yusin.quiz.choice.controller.dto.request;

import com.cpa.yusin.quiz.problem.domain.block.Block;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

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

    private List<Block> explanation;

    private boolean removedYn;

    @JsonIgnore
    public boolean isNew()
    {
        return this.id == null || this.id.equals(-1L);
    }

}
