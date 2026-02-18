package com.cpa.yusin.quiz.study.service;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import com.cpa.yusin.quiz.study.service.port.DailyStudyLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class StudyLogService {

    private final DailyStudyLogRepository dailyStudyLogRepository;

    private final MemberRepository memberRepository; // Needed for getReferenceById

    @Autowired
    @Lazy
    private StudyLogService self;

    /**
     * Record activity for the given memberId on the current date (Increments by 1).
     */
    @Transactional
    public void recordActivity(Long memberId) {
        recordActivity(memberId, 1);
    }

    /**
     * Record activity with specific count.
     * Uses atomic increment if record exists, or creates new one with initial
     * count.
     */
    @Transactional
    public void recordActivity(Long memberId, int count) {
        if (count <= 0)
            return;

        LocalDate today = LocalDate.now();

        // 1. Try atomic update first (Optimistic assumption: Log exists)
        int updatedRows = dailyStudyLogRepository.increaseSolvedCount(memberId, today, count);

        if (updatedRows > 0) {
            return; // Successfully updated
        }

        // 2. If no rows updated, it means no log exists -> Create new one
        // We use a separate transaction (REQUIRES_NEW) for creation to avoid marking
        // the outer transaction
        // as rollback-only if a DataIntegrityViolationException occurs (race
        // condition).
        try {
            self.createLog(memberId, today, count);
        } catch (DataIntegrityViolationException e) {
            // 3. Race condition: Another thread created it just now.
            // Creation failed, but we know the row exists now. Retry update in the current
            // transaction.
            dailyStudyLogRepository.increaseSolvedCount(memberId, today, count);
        }
    }

    /**
     * Helper method to create log in a separate transaction.
     * This ensures that if it fails (due to constraint), the main transaction isn't
     * affected.
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void createLog(Long memberId, LocalDate date, int count) {
        // Use getReferenceById to avoid unnecessary SELECT
        Member memberRef = memberRepository.getReferenceById(memberId);
        DailyStudyLog newLog = DailyStudyLog.createWithCount(memberRef, date, count);
        dailyStudyLogRepository.save(newLog);
    }

    public List<DailyStudyLog> getMonthlyLog(Long memberId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return dailyStudyLogRepository.findByMemberIdAndDateBetween(memberId, startDate, endDate);
    }

    public List<DailyStudyLog> getYearlyLog(Long memberId, int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return dailyStudyLogRepository.findByMemberIdAndDateBetween(memberId, startDate, endDate);
    }
}
