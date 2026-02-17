package com.cpa.yusin.quiz.study.service;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import com.cpa.yusin.quiz.study.service.port.DailyStudyLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * Record activity for the given member on the current date.
     * Uses atomic increment if record exists, or creates new one.
     * Handles race conditions where multiple requests try to create the record
     * simultaneously.
     */
    /**
     * Record activity for the given member on the current date (Increments by 1).
     */
    public void recordActivity(Member member) {
        recordActivity(member, 1);
    }

    /**
     * Record activity with specific count.
     * Uses atomic increment if record exists, or creates new one with initial
     * count.
     */
    public void recordActivity(Member member, int count) {
        if (count <= 0)
            return;

        LocalDate today = LocalDate.now();

        // 1. Try atomic update first (Optimistic assumption: Log exists)
        int updatedRows = dailyStudyLogRepository.increaseSolvedCount(member.getId(), today, count);

        if (updatedRows > 0) {
            return; // Successfully updated
        }

        // 2. If no rows updated, it means no log exists -> Create new one
        try {
            DailyStudyLog newLog = DailyStudyLog.createWithCount(member, today, count);
            dailyStudyLogRepository.save(newLog);
        } catch (DataIntegrityViolationException e) {
            // 3. Race condition: Another thread created it just now.
            // Retry update explicitly
            dailyStudyLogRepository.increaseSolvedCount(member.getId(), today, count);
        }
    }

    public List<DailyStudyLog> getMonthlyLog(Member member, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return dailyStudyLogRepository.findByMemberIdAndDateBetween(member.getId(), startDate, endDate);
    }

    public List<DailyStudyLog> getYearlyLog(Member member, int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return dailyStudyLogRepository.findByMemberIdAndDateBetween(member.getId(), startDate, endDate);
    }
}
