package com.cpa.yusin.quiz.member.controller.dto.response;

import com.cpa.yusin.quiz.member.domain.type.Role;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoginResponse
{
    private long id;
    private String email;
    private Role role;
    private String accessToken;

    public static LoginResponse from(long id, String email, Role role, String accessToken)
    {
        return LoginResponse.builder()
                .id(id)
                .email(email)
                .role(role)
                .accessToken(accessToken)
                .build();
    }
}
