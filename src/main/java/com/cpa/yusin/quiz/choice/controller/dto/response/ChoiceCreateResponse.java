package com.cpa.yusin.quiz.choice.controller.dto.response;

import com.cpa.yusin.quiz.problem.domain.block.Block;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Builder
@Getter
public class ChoiceCreateResponse
{
    private final long id;
    private final String content;
    private final int number;
    private boolean isAnswer;
    private final List<Block> explanation;
}
