package com.cpa.yusin.quiz.study.infrastructure;

import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import com.cpa.yusin.quiz.study.service.port.DailyStudyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class DailyStudyLogRepositoryImpl implements DailyStudyLogRepository {

    private final DailyStudyLogJpaRepository dailyStudyLogJpaRepository;

    @Override
    public DailyStudyLog save(DailyStudyLog dailyStudyLog) {
        return dailyStudyLogJpaRepository.save(dailyStudyLog);
    }

    @Override
    public Optional<DailyStudyLog> findByMemberIdAndDate(Long memberId, LocalDate date) {
        return dailyStudyLogJpaRepository.findByMemberIdAndDate(memberId, date);
    }

    @Override
    public List<DailyStudyLog> findByMemberIdAndDateBetween(Long memberId, LocalDate startDate, LocalDate endDate) {
        return dailyStudyLogJpaRepository.findByMemberIdAndDateBetween(memberId, startDate, endDate);
    }

    @Override
    public int upsertSolvedCount(Long memberId, LocalDate date, int amount, LocalDateTime timestamp) {
        return dailyStudyLogJpaRepository.upsertSolvedCount(memberId, date, amount, timestamp);
    }

    @Override
    public int increaseSolvedCount(Long memberId, LocalDate date) {
        return dailyStudyLogJpaRepository.increaseSolvedCount(memberId, date);
    }

    @Override
    public int increaseSolvedCount(Long memberId, LocalDate date, int amount) {
        return dailyStudyLogJpaRepository.increaseSolvedCount(memberId, date, amount);
    }

}
