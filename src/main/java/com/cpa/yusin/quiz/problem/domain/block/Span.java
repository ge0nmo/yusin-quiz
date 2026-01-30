package com.cpa.yusin.quiz.problem.domain.block;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Span {
    private String text;       // 텍스트 내용
    private Boolean bold;      // 굵게
    private String color;      // 색상 (#FF0000)
    // 필요한 스타일 추가 (italic, underline 등)
}