package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.global.jwt.JwtService;
import com.cpa.yusin.quiz.global.security.CustomAuthenticationProvider;
import com.cpa.yusin.quiz.member.controller.dto.request.LoginRequest;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberCreateRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.LoginResponse;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberCreateResponse;
import com.cpa.yusin.quiz.member.controller.dto.response.TokenResponse;
import com.cpa.yusin.quiz.member.controller.mapper.MemberMapper;
import com.cpa.yusin.quiz.member.controller.port.AuthenticationService;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.dto.SocialProfile;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.member.service.port.MemberValidator;
import com.cpa.yusin.quiz.member.service.port.SocialTokenVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthenticationServiceImpl implements AuthenticationService
{
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final CustomAuthenticationProvider authenticationProvider;
    private final MemberMapper memberMapper;
    private final MemberValidator memberValidator;

    @Override
    public LoginResponse login(LoginRequest request)
    {
        return processLogin(request.getEmail(), request.getPassword());
    }

    public LoginResponse loginAsAdmin(String email, String password)
    {
        LoginResponse response = processLogin(email, password);

        if (!"ADMIN".equals(response.getRole().toString())) {
            log.warn("일반 유저가 관리자 페이지 접속 시도: {}", email);
            throw new RuntimeException("관리자 권한이 없습니다.");
        }

        return response;
    }

    private LoginResponse processLogin(String email, String password)
    {
        Authentication auth = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        MemberDetails memberDetails = (MemberDetails) auth.getPrincipal();

        Member member = memberDetails.getMember();

        String accessToken = jwtService.createAccessToken(member.getEmail());
        String refreshToken = jwtService.createRefreshToken(member.getEmail());

        return LoginResponse.from(member.getId(), member.getEmail(), member.getRole(), accessToken, refreshToken);
    }


    @Override
    public MemberCreateResponse signUp(MemberCreateRequest request)
    {
        memberValidator.validateEmail(request.getEmail());

        Member member = Member.fromHome(request, passwordEncoder);
        member = memberRepository.save(member);

        return memberMapper.toMemberCreateResponse(member);
    }

    @Override
    @Transactional
    public LoginResponse socialLogin(SocialProfile socialProfile)
    {
        Member member = memberRepository.findByEmail(socialProfile.getEmail())
                .orElseGet(() -> registerSocialMember(socialProfile));

        String accessToken = jwtService.createAccessToken(member.getEmail());
        String refreshToken = jwtService.createRefreshToken(member.getEmail());

        return LoginResponse.from(member.getId(), member.getEmail(), member.getRole(), accessToken, refreshToken);
    }

    private Member registerSocialMember(SocialProfile profile) {
        Member newMember = Member.builder()
                .email(profile.getEmail())
                .username(profile.getName()) // 이름 저장
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // 랜덤 비번
                .role(Role.USER)
                .platform(profile.getPlatform())
                .build();
        return memberRepository.save(newMember);
    }

    public TokenResponse refreshAccessToken(String refreshToken)
    {
        // 1. Refresh Token 만료 여부 확인
        if (jwtService.isTokenExpired(refreshToken)) {
            throw new RuntimeException("Refresh Token이 만료되었습니다. 다시 로그인해주세요.");
        }

        // 2. 이메일 추출 후 새로운 Access Token 생성
        String email = jwtService.extractSubject(refreshToken);
        String accessToken = jwtService.createAccessToken(email);
        String newRefreshToken = jwtService.createRefreshToken(email);

        return new TokenResponse(accessToken, newRefreshToken);
    }
}

