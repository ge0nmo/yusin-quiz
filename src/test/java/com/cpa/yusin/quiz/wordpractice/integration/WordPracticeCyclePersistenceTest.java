package com.cpa.yusin.quiz.wordpractice.integration;

import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeAnswer;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeCycle;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipant;
import com.cpa.yusin.quiz.wordpractice.infrastructure.WordPracticeAnswerJpaRepository;
import com.cpa.yusin.quiz.wordpractice.infrastructure.WordPracticeCycleJpaRepository;
import com.cpa.yusin.quiz.wordpractice.infrastructure.WordPracticeParticipantJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(TeardownExtension.class)
@SpringBootTest
class WordPracticeCyclePersistenceTest {

    @Autowired private WordPracticeParticipantJpaRepository participantJpaRepository;
    @Autowired private WordPracticeCycleJpaRepository cycleJpaRepository;
    @Autowired private WordPracticeAnswerJpaRepository answerJpaRepository;
    @Autowired private EntityManager entityManager;

    @Test
    void persistsJsonProblemOrderAndFindsTheLatestCycle() {
        WordPracticeParticipant participant = participantJpaRepository.saveAndFlush(WordPracticeParticipant.member(42L));
        WordPracticeCycle first = cycleJpaRepository.saveAndFlush(cycle(participant, 1, List.of(11L, 12L)));
        WordPracticeCycle second = cycleJpaRepository.saveAndFlush(cycle(participant, 2, List.of(21L, 22L)));
        entityManager.clear();

        assertThat(cycleJpaRepository.findById(first.getId()).orElseThrow().getProblemOrder()).containsExactly(11L, 12L);
        assertThat(cycleJpaRepository.findFirstByParticipantIdAndSubjectIdOrderByRoundNumberDesc(participant.getId(), 7L))
                .map(WordPracticeCycle::getId)
                .contains(second.getId());
    }

    @Test
    void enforcesCycleUniqueness() {
        WordPracticeParticipant participant = participantJpaRepository.saveAndFlush(WordPracticeParticipant.member(42L));
        WordPracticeCycle cycle = cycleJpaRepository.saveAndFlush(cycle(participant, 1, List.of(11L, 12L)));

        assertThatThrownBy(() -> cycleJpaRepository.saveAndFlush(cycle(participant, 1, List.of(21L))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void enforcesAnswerProblemUniqueness() {
        WordPracticeCycle cycle = savedCycle();
        answerJpaRepository.saveAndFlush(WordPracticeAnswer.create(cycle, 11L, 101L, 1, true, LocalDateTime.now()));

        assertThatThrownBy(() -> answerJpaRepository.saveAndFlush(
                WordPracticeAnswer.create(cycle, 11L, 102L, 2, false, LocalDateTime.now())
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void enforcesAnswerSequenceUniqueness() {
        WordPracticeCycle cycle = savedCycle();
        answerJpaRepository.saveAndFlush(WordPracticeAnswer.create(cycle, 11L, 101L, 1, true, LocalDateTime.now()));

        assertThatThrownBy(() -> answerJpaRepository.saveAndFlush(
                WordPracticeAnswer.create(cycle, 12L, 102L, 1, false, LocalDateTime.now())
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void providesPessimisticLockLookup() {
        WordPracticeParticipant participant = participantJpaRepository.saveAndFlush(WordPracticeParticipant.member(42L));
        WordPracticeCycle cycle = cycleJpaRepository.saveAndFlush(cycle(participant, 1, List.of(11L)));

        assertThat(cycleJpaRepository.findByIdWithLock(cycle.getId())).isPresent();
    }

    private WordPracticeCycle cycle(WordPracticeParticipant participant, int roundNumber, List<Long> order) {
        return WordPracticeCycle.start(participant, 7L, roundNumber, "seed-" + roundNumber, order,
                LocalDateTime.of(2026, 8, 9, 10, 0));
    }

    private WordPracticeCycle savedCycle() {
        WordPracticeParticipant participant = participantJpaRepository.saveAndFlush(WordPracticeParticipant.member(42L));
        return cycleJpaRepository.saveAndFlush(cycle(participant, 1, List.of(11L, 12L)));
    }
}
