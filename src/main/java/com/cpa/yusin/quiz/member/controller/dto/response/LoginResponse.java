package com.cpa.yusin.quiz.member.controller.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoginResponse
{
    private String accessToken;

    public static LoginResponse from(String accessToken)
    {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
