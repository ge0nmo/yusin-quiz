package com.cpa.yusin.quiz.question.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Data
public class QuestionDTO
{
    private final long id;
    private final String title;
    private final String content;
    private final Boolean isAnswered;
    private final LocalDateTime createdAt;
}
