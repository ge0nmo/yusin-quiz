package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.mock.FakeMemberRepository;
import org.junit.jupiter.api.BeforeEach;

class MemberServiceTest
{
    private MemberServiceImpl memberService;
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp()
    {
        FakeMemberRepository fakeMemberRepository = new FakeMemberRepository();
        this.memberService = new MemberServiceImpl(fakeMemberRepository);

        fakeMemberRepository.save(MemberDomain.builder()
                        .id(1L)
                        .email("test@gmail.com")
                        .password("aaaa")
                        .role(Role.USER)
                        .platform(Platform.HOME)
                        .build());

    }
}