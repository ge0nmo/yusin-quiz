package com.cpa.yusin.quiz.answer.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminAnswerRegisterRequest {

    @NotBlank
    private String content;
}
