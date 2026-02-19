package com.cpa.yusin.quiz.study.service;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import com.cpa.yusin.quiz.study.service.port.DailyStudyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DailyStudyLogManager {

    private final DailyStudyLogRepository dailyStudyLogRepository;
    private final MemberRepository memberRepository;

    /**
     * Helper method to create log in a separate transaction.
     * This ensures that if it fails (due to constraint), the main transaction isn't
     * affected.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createLog(Long memberId, LocalDate date, int count) {
        // Use getReferenceById to avoid unnecessary SELECT
        Member memberRef = memberRepository.getReferenceById(memberId);
        DailyStudyLog newLog = DailyStudyLog.createWithCount(memberRef, date, count);
        dailyStudyLogRepository.save(newLog);
    }
}
