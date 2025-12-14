package com.cpa.yusin.quiz.admin.presentation.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminLoginRequest
{
    private String email;
    private String password;
}
