package com.cpa.yusin.quiz.wordpractice.domain;

import com.cpa.yusin.quiz.global.exception.WordPracticeException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordPracticeCycleTest {

    private static final LocalDateTime STARTED_AT = LocalDateTime.of(2026, 8, 9, 10, 0);
    private static final LocalDateTime FINISHED_AT = LocalDateTime.of(2026, 8, 9, 10, 3);

    @Test
    void tracksProgressAndCompletesUsingTheProvidedTime() {
        WordPracticeCycle cycle = cycle(List.of(11L, 12L));

        assertThat(cycle.getTotalCount()).isEqualTo(2);
        assertThat(cycle.getRemainingCount()).isEqualTo(2);
        assertThat(cycle.currentProblemId()).contains(11L);

        cycle.markAnswered(true);
        cycle.completeIfFinished(FINISHED_AT);
        cycle.markAnswered(false);
        cycle.completeIfFinished(FINISHED_AT);

        assertThat(cycle.getSolvedCount()).isEqualTo(2);
        assertThat(cycle.getCorrectCount()).isEqualTo(1);
        assertThat(cycle.getIncorrectCount()).isEqualTo(1);
        assertThat(cycle.getRemainingCount()).isZero();
        assertThat(cycle.isCompleted()).isTrue();
        assertThat(cycle.getFinishedAt()).isEqualTo(FINISHED_AT);
    }

    @Test
    void removesOnlyUnansweredUnavailableProblems() {
        WordPracticeCycle cycle = cycle(List.of(11L, 12L, 13L));
        cycle.markAnswered(true);

        cycle.removeUnavailableProblemIds(List.of(12L, 999L));

        assertThat(cycle.getProblemOrder()).containsExactly(11L, 13L);
        assertThat(cycle.getSkippedCount()).isEqualTo(1);
        assertThat(cycle.getTotalCount()).isEqualTo(2);
        assertThat(cycle.currentProblemId()).contains(13L);
        assertThatThrownBy(() -> cycle.removeUnavailableProblemIds(List.of(11L)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsEmptyCycleSnapshot() {
        assertThatThrownBy(() -> cycle(List.of()))
                .isInstanceOf(WordPracticeException.class);
    }

    private WordPracticeCycle cycle(List<Long> problemOrder) {
        return WordPracticeCycle.start(
                WordPracticeParticipant.member(42L),
                7L,
                1,
                "seed-1",
                problemOrder,
                STARTED_AT
        );
    }
}
