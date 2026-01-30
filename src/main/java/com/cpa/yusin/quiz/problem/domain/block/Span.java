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
    private Boolean italic;        // 기울임
    private Boolean underline;     // 밑줄
    private Boolean strikethrough; // 취소선
    private String backgroundColor;
}