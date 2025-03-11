package com.cpa.yusin.quiz.web.dto;

import jakarta.validation.constraints.NotBlank;


public record AdminAnswerUpdateRequest(@NotBlank String content)
{

}
