package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.global.details.MemberDetailsService;
import com.cpa.yusin.quiz.global.jwt.JwtService;
import com.cpa.yusin.quiz.global.security.CustomAuthenticationProvider;
import com.cpa.yusin.quiz.member.controller.dto.request.LoginRequest;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberCreateRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.LoginResponse;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberCreateResponse;
import com.cpa.yusin.quiz.member.controller.mapper.MemberMapper;
import com.cpa.yusin.quiz.member.controller.port.AuthenticationService;
import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.member.service.port.MemberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthenticationServiceImpl implements AuthenticationService
{
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final CustomAuthenticationProvider authenticationProvider;
    private final MemberDetailsService memberDetailsService;
    private final MemberMapper memberMapper;
    private final MemberValidator memberValidator;


    @Override
    public LoginResponse login(LoginRequest request)
    {
        authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        MemberDetails memberDetails = memberDetailsService.loadUserByUsername(request.getEmail());

        String accessToken = jwtService.createAccessToken(memberDetails.getUsername());

        return LoginResponse.from(accessToken);
    }

    @Override
    public MemberCreateResponse signUp(MemberCreateRequest request)
    {
        memberValidator.validateEmail(request.getEmail());

        MemberDomain memberDomain = MemberDomain.fromHome(request, passwordEncoder);
        memberDomain = memberRepository.save(memberDomain);

        return memberMapper.toMemberCreateResponse(memberDomain);
    }


}

