package com.cpa.yusin.quiz.member.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.member.controller.dto.request.GoogleLoginRequest;
import com.cpa.yusin.quiz.member.controller.dto.request.LoginRequest;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberCreateRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.LoginResponse;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberCreateResponse;
import com.cpa.yusin.quiz.member.controller.dto.request.RefreshRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.TokenResponse;
import com.cpa.yusin.quiz.member.controller.port.AuthenticationService;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.service.SocialLoadService;
import com.cpa.yusin.quiz.member.service.dto.SocialProfile;
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
    private final SocialLoadService socialLoadService;

    @PostMapping("/login")
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

    @PostMapping("/auth/login/google")
    public ResponseEntity<GlobalResponse<LoginResponse>> googleLogin(@Validated @RequestBody GoogleLoginRequest request)
    {
        SocialProfile socialProfile = socialLoadService.getSocialProfile(Platform.GOOGLE, request.getIdToken());

        // 2. 받아온 정보로 로그인/가입 처리 (AuthenticationService 동작)
        LoginResponse response = authenticationService.socialLogin(socialProfile);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }


    @PostMapping("/auth/refresh")
    public ResponseEntity<GlobalResponse<TokenResponse>> refresh(@RequestBody RefreshRequest request)
    {
        TokenResponse response = authenticationService.refreshAccessToken(request.refreshToken());

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }
}
