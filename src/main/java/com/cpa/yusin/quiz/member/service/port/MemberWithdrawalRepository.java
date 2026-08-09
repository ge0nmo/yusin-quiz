package com.cpa.yusin.quiz.member.service.port;

import com.cpa.yusin.quiz.member.domain.Member;

public interface MemberWithdrawalRepository {
    void withdraw(long memberId, Member withdrawnAuthor);
}
