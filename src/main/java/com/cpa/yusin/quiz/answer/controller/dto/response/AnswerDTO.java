package com.cpa.yusin.quiz.answer.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@Builder
public class AnswerDTO
{
    private final long id;
    private final String username;
    private final String content;
    private final LocalDateTime createdAt;
}
