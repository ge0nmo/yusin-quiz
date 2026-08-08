package com.cpa.yusin.quiz.wordpractice.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.WordPracticeException;
import com.cpa.yusin.quiz.wordpractice.infrastructure.LongListJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "word_practice_cycle", uniqueConstraints = @UniqueConstraint(
        name = "uk_word_practice_cycle_participant_subject_round",
        columnNames = {"participant_id", "subject_id", "round_number"}
), indexes = {
        @Index(name = "idx_word_practice_cycle_participant_status", columnList = "participant_id,status")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordPracticeCycle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false, updatable = false)
    private WordPracticeParticipant participant;

    @Column(name = "subject_id", nullable = false, updatable = false)
    private Long subjectId;

    @Column(name = "round_number", nullable = false, updatable = false)
    private int roundNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WordPracticeCycleStatus status;

    @Column(name = "shuffle_seed", nullable = false, updatable = false, length = 64)
    private String shuffleSeed;

    @Convert(converter = LongListJsonConverter.class)
    @Column(name = "problem_order", nullable = false, columnDefinition = "json")
    private List<Long> problemOrder;

    @Column(name = "planned_problem_count", nullable = false, updatable = false)
    private int plannedProblemCount;

    @Column(name = "next_index", nullable = false)
    private int nextIndex;

    @Column(name = "solved_count", nullable = false)
    private int solvedCount;

    @Column(name = "correct_count", nullable = false)
    private int correctCount;

    @Column(name = "skipped_count", nullable = false)
    private int skippedCount;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Version
    private Long version;

    public static WordPracticeCycle start(
            WordPracticeParticipant participant,
            Long subjectId,
            int roundNumber,
            String shuffleSeed,
            List<Long> problemOrder,
            LocalDateTime startedAt
    ) {
        if (participant == null || subjectId == null || roundNumber < 1 || shuffleSeed == null || shuffleSeed.isBlank()
                || startedAt == null || problemOrder == null || problemOrder.isEmpty()) {
            throw new WordPracticeException(ExceptionMessage.WORD_PRACTICE_NO_PROBLEMS);
        }
        if (problemOrder.stream().anyMatch(problemId -> problemId == null) || new HashSet<>(problemOrder).size() != problemOrder.size()) {
            throw new IllegalArgumentException("Problem order must contain distinct non-null IDs");
        }
        return WordPracticeCycle.builder()
                .participant(participant)
                .subjectId(subjectId)
                .roundNumber(roundNumber)
                .status(WordPracticeCycleStatus.IN_PROGRESS)
                .shuffleSeed(shuffleSeed)
                .problemOrder(new ArrayList<>(problemOrder))
                .plannedProblemCount(problemOrder.size())
                .nextIndex(0)
                .solvedCount(0)
                .correctCount(0)
                .skippedCount(0)
                .startedAt(startedAt)
                .build();
    }

    public List<Long> getProblemOrder() {
        return problemOrder == null ? null : List.copyOf(problemOrder);
    }

    public Optional<Long> currentProblemId() {
        if (nextIndex >= problemOrder.size()) {
            return Optional.empty();
        }
        return Optional.of(problemOrder.get(nextIndex));
    }

    public void markAnswered(boolean correct) {
        if (isCompleted() || nextIndex >= problemOrder.size()) {
            throw new IllegalStateException("Completed word practice cycle cannot accept answers");
        }
        solvedCount++;
        nextIndex++;
        if (correct) {
            correctCount++;
        }
        assertCounts();
    }

    public void removeUnavailableProblemIds(Collection<Long> unavailableProblemIds) {
        if (unavailableProblemIds == null || unavailableProblemIds.isEmpty()) {
            return;
        }
        Set<Long> unavailableIds = new HashSet<>(unavailableProblemIds);
        for (int index = 0; index < nextIndex; index++) {
            if (unavailableIds.contains(problemOrder.get(index))) {
                throw new IllegalArgumentException("Already answered problem cannot be removed");
            }
        }
        int originalSize = problemOrder.size();
        problemOrder.removeIf(problemId -> unavailableIds.contains(problemId));
        skippedCount += originalSize - problemOrder.size();
        assertCounts();
    }

    public void completeIfFinished(LocalDateTime now) {
        if (nextIndex != problemOrder.size() || isCompleted()) {
            return;
        }
        if (now == null) {
            throw new IllegalArgumentException("Completion time is required");
        }
        status = WordPracticeCycleStatus.COMPLETED;
        finishedAt = now;
    }

    public boolean isCompleted() {
        return status == WordPracticeCycleStatus.COMPLETED;
    }

    public int getTotalCount() {
        return problemOrder.size();
    }

    public int getIncorrectCount() {
        return solvedCount - correctCount;
    }

    public int getRemainingCount() {
        return getTotalCount() - solvedCount;
    }

    private void assertCounts() {
        if (correctCount < 0 || correctCount > solvedCount || solvedCount > problemOrder.size()
                || nextIndex != solvedCount || skippedCount < 0) {
            throw new IllegalStateException("Invalid word practice cycle counts");
        }
    }
}
