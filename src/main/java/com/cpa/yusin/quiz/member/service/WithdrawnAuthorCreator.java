package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class WithdrawnAuthorCreator {
    private final MemberRepository memberRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createIfMissing() {
        if (memberRepository.findWithdrawnAuthor().isPresent()) {
            return;
        }
        memberRepository.saveAndFlush(Member.withdrawnAuthor());
    }
}
