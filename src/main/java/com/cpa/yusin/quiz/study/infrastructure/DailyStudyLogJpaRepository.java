package com.cpa.yusin.quiz.study.infrastructure;

import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyStudyLogJpaRepository extends JpaRepository<DailyStudyLog, Long> {

        Optional<DailyStudyLog> findByMemberIdAndDate(Long memberId, LocalDate date);

        List<DailyStudyLog> findByMemberIdAndDateBetween(Long memberId, LocalDate startDate, LocalDate endDate);

        @Modifying(clearAutomatically = true)
        @Query("UPDATE DailyStudyLog d SET d.solvedCount = d.solvedCount + 1 WHERE d.member.id = :memberId AND d.date = :date")
        int increaseSolvedCount(@Param("memberId") Long memberId, @Param("date") LocalDate date);

        @Modifying(clearAutomatically = true)
        @Query("UPDATE DailyStudyLog d SET d.solvedCount = d.solvedCount + :amount WHERE d.member.id = :memberId AND d.date = :date")
        int increaseSolvedCount(@Param("memberId") Long memberId, @Param("date") LocalDate date,
                        @Param("amount") int amount);

}
