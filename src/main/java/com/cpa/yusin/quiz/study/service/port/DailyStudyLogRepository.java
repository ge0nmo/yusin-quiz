package com.cpa.yusin.quiz.study.service.port;

import com.cpa.yusin.quiz.study.domain.DailyStudyLog;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyStudyLogRepository {
    DailyStudyLog save(DailyStudyLog dailyStudyLog);

    Optional<DailyStudyLog> findByMemberIdAndDate(Long memberId, LocalDate date);

    List<DailyStudyLog> findByMemberIdAndDateBetween(Long memberId, LocalDate startDate, LocalDate endDate);

    int increaseSolvedCount(Long memberId, LocalDate date);

    int increaseSolvedCount(Long memberId, LocalDate date, int amount);

}
