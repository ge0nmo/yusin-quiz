package com.cpa.yusin.quiz.member.service;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class WithdrawnAuthorProvider {
    private final MemberRepository memberRepository;
    private final WithdrawnAuthorCreator withdrawnAuthorCreator;

    public Member getOrCreate() {
        try {
            withdrawnAuthorCreator.createIfMissing();
        } catch (DataIntegrityViolationException ignored) {
            // Another withdrawal committed the unique shared author first.
        }

        return memberRepository.findWithdrawnAuthorWithLock()
                .orElseThrow(() -> new IllegalStateException("Withdrawn author was not persisted"));
    }
}
