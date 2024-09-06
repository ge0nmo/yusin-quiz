package com.cpa.yusin.quiz.member.controller.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoginResponse
{
    private long id;
    private String email;
    private String accessToken;

    public static LoginResponse from(long id, String email, String accessToken)
    {
        return LoginResponse.builder()
                .id(id)
                .email(email)
                .accessToken(accessToken)
                .build();
    }
}
