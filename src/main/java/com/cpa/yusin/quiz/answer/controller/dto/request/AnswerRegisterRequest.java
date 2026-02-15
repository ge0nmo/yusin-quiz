package com.cpa.yusin.quiz.answer.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AnswerRegisterRequest {
    @NotBlank(message = "내용을 입력해주세요")
    private String content;
}
