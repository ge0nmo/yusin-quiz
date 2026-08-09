package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.MemberException;
import com.cpa.yusin.quiz.member.controller.port.MemberWithdrawalService;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.member.service.port.MemberWithdrawalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberWithdrawalServiceImpl implements MemberWithdrawalService {
    private final MemberRepository memberRepository;
    private final MemberWithdrawalRepository memberWithdrawalRepository;
    private final WithdrawnAuthorProvider withdrawnAuthorProvider;

    @Transactional
    @Override
    public void withdraw(long memberId) {
        Member member = memberRepository.findByIdWithLock(memberId)
                .orElseThrow(() -> new MemberException(ExceptionMessage.USER_NOT_FOUND));

        if (member.isWithdrawnAuthor()) {
            throw new MemberException(ExceptionMessage.NO_AUTHORIZATION);
        }

        Member withdrawnAuthor = withdrawnAuthorProvider.getOrCreate();
        memberWithdrawalRepository.withdraw(memberId, withdrawnAuthor);
    }
}
