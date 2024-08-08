package com.cpa.yusin.quiz.service.impl;

import com.cpa.yusin.quiz.domain.entity.Member;
import com.cpa.yusin.quiz.domain.entity.type.Platform;
import com.cpa.yusin.quiz.repository.MemberRepository;
import com.cpa.yusin.quiz.service.MemberWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class MemberWriteServiceImpl implements MemberWriteService
{
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Member register(String email, String password, String username, Platform platform)
    {
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .username(username)
                .platform(platform)
                .build();

        return memberRepository.save(member);
    }
}
