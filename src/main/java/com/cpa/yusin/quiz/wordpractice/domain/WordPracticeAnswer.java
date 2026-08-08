package com.cpa.yusin.quiz.wordpractice.domain;

import com.cpa.yusin.quiz.common.infrastructure.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 말문제 빠른 풀이에서 최초 제출된 선택을 보존하는 불변 이력이다.
 * 회차 진행률과 즉시 정오답 응답은 이 엔티티를 저장한 뒤 갱신하며, 기존 SubmittedAnswer에는 사용하지 않는다.
 */
@Entity
@Table(name = "word_practice_answer", uniqueConstraints = {
        @UniqueConstraint(name = "uk_word_practice_answer_cycle_problem", columnNames = {"cycle_id", "problem_id"}),
        @UniqueConstraint(name = "uk_word_practice_answer_cycle_sequence", columnNames = {"cycle_id", "sequence"})
}, indexes = @Index(name = "idx_word_practice_answer_cycle_correct", columnList = "cycle_id,correct"))
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordPracticeAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false, updatable = false)
    private WordPracticeCycle cycle;

    @Column(name = "problem_id", nullable = false, updatable = false)
    private Long problemId;

    @Column(name = "choice_id", nullable = false, updatable = false)
    private Long choiceId;

    /** 사람이 읽는 제출 순서이므로 첫 답안은 1부터 시작한다. */
    @Column(nullable = false, updatable = false)
    private int sequence;

    @Column(nullable = false, updatable = false)
    private boolean correct;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    public static WordPracticeAnswer create(
            WordPracticeCycle cycle,
            Long problemId,
            Long choiceId,
            int sequence,
            boolean correct,
            LocalDateTime submittedAt
    ) {
        // 수정 가능한 DTO가 아닌 제출 시점의 선택·순서·정오답·시간을 한 번만 스냅샷한다.
        if (cycle == null || problemId == null || choiceId == null || sequence < 1 || submittedAt == null) {
            throw new IllegalArgumentException("Word practice answer requires immutable submission data");
        }
        return WordPracticeAnswer.builder()
                .cycle(cycle)
                .problemId(problemId)
                .choiceId(choiceId)
                .sequence(sequence)
                .correct(correct)
                .submittedAt(submittedAt)
                .build();
    }
}
