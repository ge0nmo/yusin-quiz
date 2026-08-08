package com.cpa.yusin.quiz.wordpractice.infrastructure;

import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeCycle;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface WordPracticeCycleJpaRepository extends JpaRepository<WordPracticeCycle, Long> {

    Optional<WordPracticeCycle> findFirstByParticipantIdAndSubjectIdOrderByRoundNumberDesc(Long participantId, Long subjectId);

    /** participant 잠금 직후 최신 회차를 current read로 다시 확인하기 위한 잠금 조회다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from WordPracticeCycle c where c.participant.id = :participantId and c.subjectId = :subjectId " +
            "and c.roundNumber = (select max(latest.roundNumber) from WordPracticeCycle latest " +
            "where latest.participant.id = :participantId and latest.subjectId = :subjectId)")
    Optional<WordPracticeCycle> findLatestByParticipantIdAndSubjectIdWithLock(
            @Param("participantId") Long participantId,
            @Param("subjectId") Long subjectId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from WordPracticeCycle c where c.id = :cycleId")
    Optional<WordPracticeCycle> findByIdWithLock(@Param("cycleId") Long cycleId);

    /** 말문제 subject 목록의 진행률 조회를 위한 subject별 최신 회차 배치 쿼리다. */
    @Query("select c from WordPracticeCycle c where c.participant.id = :participantId " +
            "and c.roundNumber = (select max(latest.roundNumber) from WordPracticeCycle latest " +
            "where latest.participant.id = :participantId and latest.subjectId = c.subjectId)")
    List<WordPracticeCycle> findLatestByParticipantId(@Param("participantId") Long participantId);
}
