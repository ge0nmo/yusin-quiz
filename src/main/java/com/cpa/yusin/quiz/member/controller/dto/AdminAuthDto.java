package com.cpa.yusin.quiz.member.controller.dto;

import com.cpa.yusin.quiz.member.domain.type.Role;
import jakarta.validation.constraints.NotBlank;

public final class AdminAuthDto {
    private AdminAuthDto() {}
    public record LoginRequest(@NotBlank String loginId, @NotBlank String password) {}
    public record LoginResponse(Long id, String loginId, Role role, String accessToken, String refreshToken) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record TokenResponse(String accessToken, String refreshToken) {}
}
