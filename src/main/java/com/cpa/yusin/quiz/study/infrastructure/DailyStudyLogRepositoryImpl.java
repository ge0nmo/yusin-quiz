package com.cpa.yusin.quiz.study.infrastructure;

import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import com.cpa.yusin.quiz.study.service.port.DailyStudyLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    // Atomic increment logic can be exposed here if needed by the interface,
    // but for now strict adherence to the interface is sufficient.
    // The service might use 'save' with optimistic locking or this custom method if
    // high concurrency is expected.
    // Given the requirements, I will add a method to the interface if I need to use
    // the @Modifying query.
    // However, the interface currently defined doesn't have it. I should probably
    // add it to the interface
    // or use a refined approach in the service.
    // For "Contribution Graph", strict concurrency (losing 1 count) isn't critical,
    // but "Production Level"
    // implies handling it. I'll stick to 'save' with unique constraint handling in
    // Service for creation,
    // and potentially dirty checking or atomic update for increment.

    // Let's rely on JPA's dirty checking or explicit save for now.
    // For high concurrency increment, I'll add the method to the interface.

    @Override
    public int increaseSolvedCount(Long memberId, LocalDate date) {
        return dailyStudyLogJpaRepository.increaseSolvedCount(memberId, date);
    }

    @Override
    public int increaseSolvedCount(Long memberId, LocalDate date, int amount) {
        return dailyStudyLogJpaRepository.increaseSolvedCount(memberId, date, amount);
    }

}
