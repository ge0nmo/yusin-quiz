package com.cpa.yusin.quiz.study.service;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import com.cpa.yusin.quiz.study.service.port.DailyStudyLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class StudyLogService {

    private final DailyStudyLogRepository dailyStudyLogRepository;
    private final ClockHolder clockHolder;

    @Transactional
    public void recordActivity(Long memberId, int count) {
        if (count <= 0) {
            return;
        }

        LocalDateTime now = clockHolder.getCurrentDateTime();
        LocalDate today = now.toLocalDate();
        dailyStudyLogRepository.upsertSolvedCount(memberId, today, count, now);
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

    public int calculateCurrentStreak(Long memberId) {
        LocalDate today = clockHolder.getCurrentDateTime().toLocalDate();
        LocalDate oneYearAgo = today.minusDays(365);
        List<DailyStudyLog> logs = new ArrayList<>(
                dailyStudyLogRepository.findByMemberIdAndDateBetween(memberId, oneYearAgo, today)
        );

        logs.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        int streak = 0;
        LocalDate expectedDate = today;

        if (logs.isEmpty() || !logs.get(0).getDate().equals(today)) {
            expectedDate = today.minusDays(1);
        }

        for (DailyStudyLog log : logs) {
            if (log.getDate().equals(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else if (log.getDate().isBefore(expectedDate)) {
                break;
            }
        }

        return streak;
    }
}
