package com.cpa.yusin.quiz.member.controller.port;

import com.cpa.yusin.quiz.member.controller.dto.request.LoginRequest;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberCreateRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.LoginResponse;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberCreateResponse;

public interface AuthenticationService
{
    MemberCreateResponse signUp(MemberCreateRequest request);

    LoginResponse login(LoginRequest loginRequest);

    LoginResponse login(String email, String password);
}
