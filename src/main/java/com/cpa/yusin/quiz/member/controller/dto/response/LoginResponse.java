package com.cpa.yusin.quiz.member.controller.dto.response;

import com.cpa.yusin.quiz.member.domain.type.Role;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoginResponse {
    private long id;
    private String email;
    private Role role;
    private String username;
    private String accessToken;
    private String refreshToken;

    public static LoginResponse from(long id, String email, String username, Role role, String accessToken,
            String refreshToken) {
        return LoginResponse.builder()
                .id(id)
                .email(email)
                .username(username)
                .role(role)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
