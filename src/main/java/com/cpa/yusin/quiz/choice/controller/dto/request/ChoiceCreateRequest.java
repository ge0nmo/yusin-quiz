package com.cpa.yusin.quiz.choice.controller.dto.request;

import com.cpa.yusin.quiz.problem.domain.block.Block;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    private List<Block> explanation;
}
