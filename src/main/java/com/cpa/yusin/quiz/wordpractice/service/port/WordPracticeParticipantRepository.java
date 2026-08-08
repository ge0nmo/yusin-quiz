package com.cpa.yusin.quiz.wordpractice.service.port;

import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipant;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipantType;

import java.util.Optional;
import java.time.LocalDateTime;

public interface WordPracticeParticipantRepository {

    Optional<WordPracticeParticipant> findByTypeAndOwnerKey(WordPracticeParticipantType type, String ownerKey);

    void insertIfAbsent(WordPracticeParticipantType type, String ownerKey, LocalDateTime now);

    /**
     * 동일 참여자의 동시 회차 시작을 직렬화하기 위해 참여자 행을 비관적으로 잠근다.
     * Task 05의 최초 회차 생성 서비스에서만 쓰는 쓰기 잠금 조회다.
     */
    Optional<WordPracticeParticipant> findByIdWithLock(Long participantId);

    WordPracticeParticipant saveAndFlush(WordPracticeParticipant participant);
}
