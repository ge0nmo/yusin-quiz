package com.cpa.yusin.quiz.wordpractice.infrastructure;

import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipant;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipantType;
import com.cpa.yusin.quiz.wordpractice.service.port.WordPracticeParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class WordPracticeParticipantRepositoryImpl implements WordPracticeParticipantRepository {

    private final WordPracticeParticipantJpaRepository wordPracticeParticipantJpaRepository;

    @Override
    public Optional<WordPracticeParticipant> findByTypeAndOwnerKey(WordPracticeParticipantType type, String ownerKey) {
        return wordPracticeParticipantJpaRepository.findByTypeAndOwnerKey(type, ownerKey);
    }

    @Override
    public void insertIfAbsent(WordPracticeParticipantType type, String ownerKey, LocalDateTime now) {
        wordPracticeParticipantJpaRepository.insertIfAbsent(type.name(), ownerKey, now);
    }

    /** 회차 생성 경합을 막기 위해 참여자 행 잠금 조회를 JPA에 위임한다. */
    @Override
    public Optional<WordPracticeParticipant> findByIdWithLock(Long participantId) {
        return wordPracticeParticipantJpaRepository.findByIdWithLock(participantId);
    }

    @Override
    public WordPracticeParticipant saveAndFlush(WordPracticeParticipant participant) {
        return wordPracticeParticipantJpaRepository.saveAndFlush(participant);
    }
}
