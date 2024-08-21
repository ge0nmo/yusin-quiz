package com.cpa.yusin.quiz.mock;

import com.cpa.yusin.quiz.global.details.MemberDetailsService;
import com.cpa.yusin.quiz.global.jwt.JwtService;
import com.cpa.yusin.quiz.global.jwt.JwtServiceImpl;
import com.cpa.yusin.quiz.global.security.CustomAuthenticationProvider;
import com.cpa.yusin.quiz.member.controller.port.AuthenticationService;
import com.cpa.yusin.quiz.member.controller.port.MemberService;
import com.cpa.yusin.quiz.member.service.AuthenticationServiceImpl;
import com.cpa.yusin.quiz.member.service.MemberServiceImpl;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TestContainer
{
    private static final String FAKE_SECRET_KEY = "thisIsATestSecretKeyUsedOnlyForTesasdfeefsdfewdfesredfesfwqewdasdqrewtingPurposes";

    public final MemberRepository memberRepository;
    public final MemberService memberService;
    public final MemberDetailsService memberDetailsService;
    public final CustomAuthenticationProvider authenticationProvider;
    public final PasswordEncoder passwordEncoder;
    public final JwtService jwtService;
    public final AuthenticationService authenticationService;


    public TestContainer()
    {
        this.memberRepository = new FakeMemberRepository();
        this.memberService = new MemberServiceImpl(this.memberRepository);
        this.memberDetailsService = new MemberDetailsService(this.memberRepository);
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.authenticationProvider = new CustomAuthenticationProvider(this.memberDetailsService, this.passwordEncoder);
        this.jwtService = new JwtServiceImpl(FAKE_SECRET_KEY);
        this.authenticationService = new AuthenticationServiceImpl(this.passwordEncoder, this.jwtService,
                this.memberRepository, this.authenticationProvider, this.memberDetailsService);


    }

}
