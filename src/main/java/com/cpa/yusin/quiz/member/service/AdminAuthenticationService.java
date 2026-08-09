package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.global.exception.ContentException;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.jwt.JwtService;
import com.cpa.yusin.quiz.member.controller.dto.AdminAuthDto.LoginRequest;
import com.cpa.yusin.quiz.member.controller.dto.AdminAuthDto.LoginResponse;
import com.cpa.yusin.quiz.member.controller.dto.AdminAuthDto.RefreshRequest;
import com.cpa.yusin.quiz.member.controller.dto.AdminAuthDto.TokenResponse;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuthenticationService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new ContentException(ExceptionMessage.INVALID_LOGIN_INFORMATION));
        if (member.getRole() != Role.ADMIN || !passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new ContentException(ExceptionMessage.INVALID_LOGIN_INFORMATION);
        }
        return new LoginResponse(member.getId(), member.getLoginId(), member.getRole(),
                jwtService.createAccessToken(member), jwtService.createRefreshToken(member));
    }

    public TokenResponse refresh(RefreshRequest request) {
        Member member = memberRepository.findByLoginId(jwtService.extractSubject(request.refreshToken()))
                .orElseThrow(() -> new ContentException(ExceptionMessage.INVALID_REFRESH_TOKEN));
        if (member.getRole() != Role.ADMIN || !jwtService.isValidRefreshToken(request.refreshToken(), member)) {
            throw new ContentException(ExceptionMessage.INVALID_REFRESH_TOKEN);
        }
        return new TokenResponse(jwtService.createAccessToken(member), jwtService.createRefreshToken(member));
    }
}
