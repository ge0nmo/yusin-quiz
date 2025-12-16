package com.cpa.yusin.quiz.member.controller;

import com.cpa.yusin.quiz.member.controller.dto.request.AdminLoginRequest;
import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.member.controller.dto.response.LoginResponse;
import com.cpa.yusin.quiz.member.controller.port.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminLoginController
{
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<GlobalResponse<LoginResponse>> login(@Validated @RequestBody AdminLoginRequest request)
    {
        LoginResponse response = authenticationService.loginAsAdmin(request.getEmail(), request.getPassword());

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

}
