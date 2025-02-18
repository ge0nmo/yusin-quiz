package com.cpa.yusin.quiz.web.dto;

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
