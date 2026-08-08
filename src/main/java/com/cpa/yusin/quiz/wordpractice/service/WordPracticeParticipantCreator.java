package com.cpa.yusin.quiz.wordpractice.service;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipant;
import com.cpa.yusin.quiz.wordpractice.service.port.WordPracticeParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * MySQL upsert로 참여자 최초 생성을 원자적으로 처리한다. 회차 생성과 같은 트랜잭션에
 * 참여하므로 이후 검증이 실패하면 새 participant도 함께 롤백된다.
 */
@Service
@RequiredArgsConstructor
public class WordPracticeParticipantCreator {

    private final WordPracticeParticipantRepository participantRepository;
    private final ClockHolder clockHolder;

    @Transactional
    public WordPracticeParticipant create(WordPracticeParticipant participant) {
        participantRepository.insertIfAbsent(
                participant.getType(), participant.getOwnerKey(), clockHolder.getCurrentDateTime());
        return participantRepository.findByTypeAndOwnerKey(participant.getType(), participant.getOwnerKey())
                .orElseThrow(() -> new IllegalStateException("Word practice participant upsert did not return a row"));
    }
}
