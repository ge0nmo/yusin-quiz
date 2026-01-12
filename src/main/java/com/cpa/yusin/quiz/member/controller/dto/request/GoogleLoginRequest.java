package com.cpa.yusin.quiz.member.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GoogleLoginRequest
{
    @NotBlank(message = "ID Token은 필수입니다.")
    private String idToken;
}