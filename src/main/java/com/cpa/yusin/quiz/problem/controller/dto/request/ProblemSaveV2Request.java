package com.cpa.yusin.quiz.problem.controller.dto.request;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.problem.domain.block.Block;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemSaveV2Request
{
    private Long id; // null이면 생성, 값이 있으면 수정
    
    private int number;
    

    private List<Block> content;
    private List<Block> explanation;
    
    private List<ChoiceRequest> choices;

    // 신규 생성인지 확인하는 헬퍼 메서드
    public boolean isNew() {
        return this.id == null || this.id <= 0;
    }
}