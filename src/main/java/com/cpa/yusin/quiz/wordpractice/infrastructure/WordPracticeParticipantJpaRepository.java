package com.cpa.yusin.quiz.wordpractice.infrastructure;

import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipant;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipantType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface WordPracticeParticipantJpaRepository extends JpaRepository<WordPracticeParticipant, Long> {

    Optional<WordPracticeParticipant> findByTypeAndOwnerKey(WordPracticeParticipantType type, String ownerKey);

    /** unique 충돌을 예외로 만들지 않고 현재 트랜잭션 안에서 참여자 행을 원자적으로 확보한다. */
    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO word_practice_participant (type, owner_key, created_at, updated_at)
            VALUES (:type, :ownerKey, :now, :now)
            ON DUPLICATE KEY UPDATE owner_key = :ownerKey
            """, nativeQuery = true)
    int insertIfAbsent(@Param("type") String type, @Param("ownerKey") String ownerKey, @Param("now") LocalDateTime now);

    /** 말문제 회차 생성 시 참여자 단위 직렬화를 보장하는 잠금 조회다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from WordPracticeParticipant p where p.id = :participantId")
    Optional<WordPracticeParticipant> findByIdWithLock(@Param("participantId") Long participantId);
}
