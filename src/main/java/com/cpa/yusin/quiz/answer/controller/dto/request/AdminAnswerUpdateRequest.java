package com.cpa.yusin.quiz.answer.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminAnswerUpdateRequest(@NotBlank String content) {
}
