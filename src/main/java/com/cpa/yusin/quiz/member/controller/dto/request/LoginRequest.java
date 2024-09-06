package com.cpa.yusin.quiz.member.controller.dto.request;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoginRequest
{
    private String email;
    private String password;
}
