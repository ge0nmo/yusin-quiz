package com.cpa.yusin.quiz.wordpractice.service;

import com.cpa.yusin.quiz.problem.service.dto.WordProblemCandidateProjection;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WordPracticeOrderStrategyTest {

    private final WordPracticeOrderStrategy strategy = new WordPracticeOrderStrategy();

    @Test
    void createsDeterministicCompleteRoundRobinOrder() {
        List<WordProblemCandidateProjection> candidates = List.of(
                candidate(1L, 10L), candidate(2L, 10L), candidate(3L, 10L),
                candidate(4L, 20L), candidate(5L, 20L),
                candidate(6L, 30L)
        );

        List<Long> first = strategy.createOrder(candidates, "seed-a");
        List<Long> second = strategy.createOrder(candidates, "seed-a");

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSize(candidates.size()).containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 5L, 6L);
        assertThat(first.stream().collect(java.util.stream.Collectors.toSet())).hasSize(candidates.size());
        var examByProblemId = candidates.stream().collect(java.util.stream.Collectors.toMap(
                WordProblemCandidateProjection::problemId,
                WordProblemCandidateProjection::examId
        ));
        assertThat(first.subList(0, 3).stream().map(examByProblemId::get)).doesNotHaveDuplicates();
    }

    @Test
    void supportsSingleExamAndEmptyCandidates() {
        assertThat(strategy.createOrder(List.of(candidate(1L, 10L), candidate(2L, 10L)), "seed-a"))
                .containsExactlyInAnyOrder(1L, 2L);
        assertThat(strategy.createOrder(List.of(), "seed-a")).isEmpty();
    }

    /**
     * 회차 생성은 DB 정렬이나 ORDER BY RAND()가 아니라 메모리 순서 전략을 사용한다.
     * 운영 규모를 대표하는 1,000개 후보에서도 모든 ID가 한 번씩만 저장되는지 확인한다.
     */
    @Test
    void createsDistinctOrderForOneThousandCandidates() {
        List<WordProblemCandidateProjection> candidates = new ArrayList<>();
        for (long problemId = 1; problemId <= 1_000; problemId++) {
            long examId = ((problemId - 1) % 20) + 1;
            candidates.add(candidate(problemId, examId));
        }

        List<Long> order = strategy.createOrder(candidates, "one-thousand-candidate-seed");

        assertThat(order).hasSize(1_000).containsExactlyInAnyOrderElementsOf(
                candidates.stream().map(WordProblemCandidateProjection::problemId).toList());
        assertThat(new HashSet<>(order)).hasSize(1_000);
    }

    private WordProblemCandidateProjection candidate(Long problemId, Long examId) {
        return new WordProblemCandidateProjection(problemId, examId, 2024);
    }
}
