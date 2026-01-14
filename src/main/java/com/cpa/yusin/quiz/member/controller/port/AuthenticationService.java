package com.cpa.yusin.quiz.member.controller.port;

import com.cpa.yusin.quiz.member.controller.dto.request.LoginRequest;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberCreateRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.LoginResponse;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberCreateResponse;
import com.cpa.yusin.quiz.member.service.dto.SocialProfile;

public interface AuthenticationService
{
    MemberCreateResponse signUp(MemberCreateRequest request);

    LoginResponse login(LoginRequest loginRequest);

    LoginResponse loginAsAdmin(String email, String password);


    LoginResponse socialLogin(SocialProfile socialProfile);

    String refreshAccessToken(String refreshToken);
}
