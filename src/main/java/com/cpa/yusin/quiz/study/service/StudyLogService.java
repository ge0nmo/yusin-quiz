package com.cpa.yusin.quiz.study.service;

import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import com.cpa.yusin.quiz.study.service.port.DailyStudyLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

    private final DailyStudyLogManager dailyStudyLogManager;

    /**
     * Record activity with specific count.
     * Uses atomic increment if record exists, or creates new one with initial
     * count.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
            dailyStudyLogManager.createLog(memberId, today, count);
        } catch (DataIntegrityViolationException e) {
            // 3. Race condition: Another thread created it just now.
            // Creation failed, but we know the row exists now. Retry update in the current
            // transaction.
            dailyStudyLogRepository.increaseSolvedCount(memberId, today, count);
        }
    }

    // createLog moved to DailyStudyLogManager

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

    public int calculateCurrentStreak(Long memberId) {
        // Fetch logs for the last 365 days to cover enough ground for a streak
        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = today.minusDays(365);
        List<DailyStudyLog> logs = dailyStudyLogRepository.findByMemberIdAndDateBetween(memberId, oneYearAgo, today);

        // Sort by date descending (Newest first)
        logs.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        int streak = 0;
        LocalDate expectedDate = today;

        // If user hasn't studied today yet, check if they studied yesterday to keep the
        // streak alive
        if (logs.isEmpty() || !logs.get(0).getDate().equals(today)) {
            expectedDate = today.minusDays(1);
        }

        for (DailyStudyLog log : logs) {
            if (log.getDate().equals(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else if (log.getDate().isBefore(expectedDate)) {
                // Streak broken
                break;
            }
            // If log.getDate() is after expectedDate (which shouldn't happen with sorted
            // list and logic above), ignore
        }

        return streak;
    }
}
