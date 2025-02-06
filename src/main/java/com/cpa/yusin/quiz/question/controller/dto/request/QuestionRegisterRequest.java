package com.cpa.yusin.quiz.question.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class QuestionRegisterRequest
{
    private String username;

    private String password;

    private String title;

    private String content;
}
