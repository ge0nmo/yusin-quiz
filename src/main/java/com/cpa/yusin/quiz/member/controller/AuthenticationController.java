package com.cpa.yusin.quiz.member.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.member.controller.dto.request.LoginRequest;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberCreateRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.LoginResponse;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberCreateResponse;
import com.cpa.yusin.quiz.member.controller.port.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@RestController
public class AuthenticationController
{
    private final AuthenticationService authenticationService;

    @PostMapping("/home/login")
    public ResponseEntity<GlobalResponse<LoginResponse>> login(@Validated @RequestBody LoginRequest loginRequest)
    {
        LoginResponse response = authenticationService.login(loginRequest);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @PostMapping("/sign-up")
    public ResponseEntity<GlobalResponse<MemberCreateResponse>> signUp(@Validated @RequestBody MemberCreateRequest request)
    {
        MemberCreateResponse response = authenticationService.signUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GlobalResponse<>(response));
    }
}
