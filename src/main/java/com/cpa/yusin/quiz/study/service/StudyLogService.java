package com.cpa.yusin.quiz.study.service;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import com.cpa.yusin.quiz.study.service.port.DailyStudyLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final MemberRepository memberRepository;

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

        dailyStudyLogRepository.findByMemberIdAndDate(memberId, today)
                .ifPresentOrElse(
                        log -> log.increaseSolvedCount(count),
                        () -> {
                            Member member = memberRepository.getReferenceById(memberId);
                            DailyStudyLog newLog = DailyStudyLog.createWithCount(member, today, count);
                            dailyStudyLogRepository.save(newLog);
                        });
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
