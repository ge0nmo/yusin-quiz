package com.cpa.yusin.quiz.mock;

import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipant;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipantType;
import com.cpa.yusin.quiz.wordpractice.service.port.WordPracticeParticipantRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.time.LocalDateTime;

public class FakeWordPracticeParticipantRepository implements WordPracticeParticipantRepository {

    private final AtomicLong nextId = new AtomicLong(1);
    private final List<WordPracticeParticipant> participants = new ArrayList<>();

    @Override
    public synchronized Optional<WordPracticeParticipant> findByTypeAndOwnerKey(
            WordPracticeParticipantType type,
            String ownerKey
    ) {
        return participants.stream()
                .filter(participant -> participant.getType() == type)
                .filter(participant -> participant.getOwnerKey().equals(ownerKey))
                .findFirst();
    }

    @Override
    public synchronized void insertIfAbsent(
            WordPracticeParticipantType type,
            String ownerKey,
            LocalDateTime now
    ) {
        if (findByTypeAndOwnerKey(type, ownerKey).isEmpty()) {
            saveAndFlush(WordPracticeParticipant.builder().type(type).ownerKey(ownerKey).build());
        }
    }

    /** 단위 테스트 fake에서는 synchronized 접근으로 실제 DB 잠금의 직렬화 의미를 흉내 낸다. */
    @Override
    public synchronized Optional<WordPracticeParticipant> findByIdWithLock(Long participantId) {
        return participants.stream()
                .filter(participant -> participant.getId().equals(participantId))
                .findFirst();
    }

    @Override
    public synchronized WordPracticeParticipant saveAndFlush(WordPracticeParticipant participant) {
        WordPracticeParticipant saved = WordPracticeParticipant.builder()
                .id(nextId.getAndIncrement())
                .type(participant.getType())
                .ownerKey(participant.getOwnerKey())
                .build();
        participants.add(saved);
        return saved;
    }

    public synchronized int size() {
        return participants.size();
    }
}
