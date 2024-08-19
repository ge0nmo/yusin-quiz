package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.member.controller.port.MemberService;
import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.infrastructure.Member;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class MemberServiceImpl implements MemberService
{
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

   /* @Override
    public MemberDomain register(String email, String password, String username, Platform platform)
    {
        MemberDomain memberDomain = MemberDomain.fromHome(email, password, username, platform);

        return memberRepository.save(memberDomain);
    }*/


}
