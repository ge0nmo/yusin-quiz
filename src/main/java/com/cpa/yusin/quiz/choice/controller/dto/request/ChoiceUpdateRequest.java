package com.cpa.yusin.quiz.choice.controller.dto.request;

import com.cpa.yusin.quiz.problem.domain.block.Block;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChoiceUpdateRequest
{
    private Long id;

    @NotNull
    private Integer number;

    @NotNull
    private String content;

    @NotNull
    private Boolean isAnswer;

    private List<Block> explanation;

    private Boolean isDeleted;
}
