package com.cpa.yusin.quiz.answer.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AnswerRegisterRequest
{
    private String username;
    private String password;
    private String content;
}
