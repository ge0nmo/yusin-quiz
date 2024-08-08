package com.cpa.yusin.quiz.global.details;

import com.cpa.yusin.quiz.domain.entity.Member;
import com.cpa.yusin.quiz.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new UsernameNotFoundException(email));

        return new MemberDetails(member);
    }
}
