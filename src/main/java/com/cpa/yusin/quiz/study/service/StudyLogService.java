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
    public void recordActivity(Member member) {
        LocalDate today = LocalDate.now();

        // 1. Check if record already exists for today
        // Optimization: If exists, we don't need to do anything (User already studied
        // today)
        if (dailyStudyLogRepository.findByMemberIdAndDate(member.getId(), today).isPresent()) {
            return;
        }

        // 2. If not exists, try to create new one
        try {
            DailyStudyLog newLog = DailyStudyLog.createFirst(member, today);
            dailyStudyLogRepository.save(newLog);
        } catch (DataIntegrityViolationException e) {
            // 3. Race condition: Another thread created it just now.
            // Since we only care about "Studied or Not" (count 1 is enough), we can ignore
            // this.
            log.debug("Activity already recorded for member {} on {}", member.getId(), today);
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
