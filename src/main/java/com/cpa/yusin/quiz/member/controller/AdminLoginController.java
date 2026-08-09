package com.cpa.yusin.quiz.member.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.member.controller.dto.AdminAuthDto.LoginRequest;
import com.cpa.yusin.quiz.member.controller.dto.AdminAuthDto.LoginResponse;
import com.cpa.yusin.quiz.member.controller.dto.AdminAuthDto.RefreshRequest;
import com.cpa.yusin.quiz.member.controller.dto.AdminAuthDto.TokenResponse;
import com.cpa.yusin.quiz.member.service.AdminAuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminLoginController {
    private final AdminAuthenticationService authenticationService;

    @PostMapping("/login")
    public GlobalResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                                HttpServletResponse servletResponse) {
        LoginResponse response = authenticationService.login(request);
        servletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie("JWT_TOKEN", response.accessToken(), 3600).toString());
        servletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie("REFRESH_TOKEN", response.refreshToken(), 30 * 24 * 3600).toString());
        return GlobalResponse.success(response);
    }

    @PostMapping("/refresh")
    public GlobalResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request,
                                                  HttpServletResponse servletResponse) {
        TokenResponse response = authenticationService.refresh(request);
        servletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie("JWT_TOKEN", response.accessToken(), 3600).toString());
        servletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie("REFRESH_TOKEN", response.refreshToken(), 30 * 24 * 3600).toString());
        return GlobalResponse.success(response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie("JWT_TOKEN", "", 0).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie("REFRESH_TOKEN", "", 0).toString());
    }

    private ResponseCookie cookie(String name, String value, long maxAge) {
        return ResponseCookie.from(name, value).httpOnly(true).sameSite("Lax").path("/").maxAge(maxAge).build();
    }
}
