package com.cpa.yusin.quiz.global.details;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.MemberException;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class MemberDetailsService implements UserDetailsService
{
    private final MemberRepository memberRepository;

    public MemberDetailsService(MemberRepository memberRepository)
    {
        this.memberRepository = memberRepository;
    }


    @Override
    public MemberDetails loadUserByUsername(String email) throws UsernameNotFoundException
    {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(ExceptionMessage.USER_NOT_FOUND));

        return new MemberDetails(member, new HashMap<>());
    }
}
