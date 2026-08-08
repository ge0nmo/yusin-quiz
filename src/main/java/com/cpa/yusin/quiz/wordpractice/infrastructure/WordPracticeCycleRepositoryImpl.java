package com.cpa.yusin.quiz.wordpractice.infrastructure;

import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeCycle;
import com.cpa.yusin.quiz.wordpractice.service.port.WordPracticeCycleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class WordPracticeCycleRepositoryImpl implements WordPracticeCycleRepository {

    private final WordPracticeCycleJpaRepository cycleJpaRepository;

    @Override
    public WordPracticeCycle save(WordPracticeCycle cycle) {
        return cycleJpaRepository.save(cycle);
    }

    @Override
    public Optional<WordPracticeCycle> findById(Long cycleId) {
        return cycleJpaRepository.findById(cycleId);
    }

    @Override
    public Optional<WordPracticeCycle> findByIdWithLock(Long cycleId) {
        return cycleJpaRepository.findByIdWithLock(cycleId);
    }

    @Override
    public Optional<WordPracticeCycle> findLatestByParticipantIdAndSubjectId(Long participantId, Long subjectId) {
        return cycleJpaRepository.findFirstByParticipantIdAndSubjectIdOrderByRoundNumberDesc(participantId, subjectId);
    }

    /** 최초 회차 경합 시 최신 회차를 잠금 읽기로 확인한다. */
    @Override
    public Optional<WordPracticeCycle> findLatestByParticipantIdAndSubjectIdWithLock(Long participantId, Long subjectId) {
        return cycleJpaRepository.findLatestByParticipantIdAndSubjectIdWithLock(participantId, subjectId);
    }

    /** subject 목록 화면이 참여자별 최신 회차를 한 번에 읽도록 JPA 쿼리를 위임한다. */
    @Override
    public List<WordPracticeCycle> findLatestByParticipantId(Long participantId) {
        return cycleJpaRepository.findLatestByParticipantId(participantId);
    }
}
