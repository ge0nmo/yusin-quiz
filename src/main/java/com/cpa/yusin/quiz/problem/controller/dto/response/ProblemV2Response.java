package com.cpa.yusin.quiz.problem.controller.dto.response;

import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.problem.domain.block.Block;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProblemV2Response
{
    private Long id;
    private int number;

    // [핵심] HTML String이 아닌 Block List 반환
    private List<Block> content;
    private List<Block> explanation;

    private List<ChoiceResponse> choices;
}
