package com.cpa.yusin.quiz.study.infrastructure;

import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DailyStudyLogJpaRepository extends JpaRepository<DailyStudyLog, Long> {

        Optional<DailyStudyLog> findByMemberIdAndDate(Long memberId, LocalDate date);

        List<DailyStudyLog> findByMemberIdAndDateBetween(Long memberId, LocalDate startDate, LocalDate endDate);

        @Modifying(clearAutomatically = true)
        @Query(value = """
                        INSERT INTO daily_study_log (member_id, date, solved_count, created_at, updated_at)
                        VALUES (:memberId, :date, :amount, :timestamp, :timestamp)
                        ON DUPLICATE KEY UPDATE
                            solved_count = solved_count + VALUES(solved_count),
                            updated_at = VALUES(updated_at)
                        """, nativeQuery = true)
        int upsertSolvedCount(@Param("memberId") Long memberId,
                        @Param("date") LocalDate date,
                        @Param("amount") int amount,
                        @Param("timestamp") LocalDateTime timestamp);

        @Modifying(clearAutomatically = true)
        @Query("UPDATE DailyStudyLog d SET d.solvedCount = d.solvedCount + 1 WHERE d.member.id = :memberId AND d.date = :date")
        int increaseSolvedCount(@Param("memberId") Long memberId, @Param("date") LocalDate date);

        @Modifying(clearAutomatically = true)
        @Query("UPDATE DailyStudyLog d SET d.solvedCount = d.solvedCount + :amount WHERE d.member.id = :memberId AND d.date = :date")
        int increaseSolvedCount(@Param("memberId") Long memberId, @Param("date") LocalDate date,
                        @Param("amount") int amount);

}
