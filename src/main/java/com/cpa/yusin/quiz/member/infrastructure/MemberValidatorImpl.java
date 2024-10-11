package com.cpa.yusin.quiz.member.infrastructure;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.MemberException;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.member.service.port.MemberValidator;
import org.springframework.stereotype.Component;

@Component
public class MemberValidatorImpl implements MemberValidator
{
    private final MemberRepository memberRepository;

    public MemberValidatorImpl(MemberRepository memberRepository)
    {
        this.memberRepository = memberRepository;
    }


    @Override
    public void validateEmail(String email)
    {
        if(memberRepository.existsByEmail(email)){
            throw new MemberException(ExceptionMessage.EMAIL_EXISTS);
        }
    }
}
