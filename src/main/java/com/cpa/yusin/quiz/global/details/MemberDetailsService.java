package com.cpa.yusin.quiz.global.details;

import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.infrastructure.Member;
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
        MemberDomain memberDomain = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        return new MemberDetails(Member.fromDomain(memberDomain), new HashMap<>());
    }
}
