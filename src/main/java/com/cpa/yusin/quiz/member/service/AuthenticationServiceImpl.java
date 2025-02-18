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
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.member.service.port.MemberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    private final MemberMapper memberMapper;
    private final MemberValidator memberValidator;


    @Override
    public LoginResponse login(LoginRequest request)
    {
        return processLogin(request.getEmail(), request.getPassword());
    }

    @Override
    public LoginResponse login(String email, String password)
    {
        return processLogin(email, password);
    }

    private LoginResponse processLogin(String email, String password)
    {
        Authentication auth = authenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        MemberDetails memberDetails = (MemberDetails) auth.getPrincipal();

        Member member = memberDetails.getMember();

        String accessToken = jwtService.createAccessToken(member.getEmail());

        return LoginResponse.from(member.getId(), member.getEmail(), member.getRole(), accessToken);
    }


    @Override
    public MemberCreateResponse signUp(MemberCreateRequest request)
    {
        memberValidator.validateEmail(request.getEmail());

        Member member = Member.fromHome(request, passwordEncoder);
        member = memberRepository.save(member);

        return memberMapper.toMemberCreateResponse(member);
    }


}

