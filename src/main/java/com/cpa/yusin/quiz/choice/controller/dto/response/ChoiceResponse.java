package com.cpa.yusin.quiz.choice.controller.dto.response;

import com.cpa.yusin.quiz.problem.domain.block.Block;
import lombok.Builder;

import java.util.List;

@Builder
public record ChoiceResponse(long id, int number, String content, Boolean isAnswer, List<Block> explanation) {
}
