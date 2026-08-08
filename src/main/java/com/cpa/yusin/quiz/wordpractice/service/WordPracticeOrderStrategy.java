package com.cpa.yusin.quiz.wordpractice.service;

import com.cpa.yusin.quiz.problem.service.dto.WordProblemCandidateProjection;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Component
public class WordPracticeOrderStrategy {

    public List<Long> createOrder(List<WordProblemCandidateProjection> candidates, String shuffleSeed) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        if (shuffleSeed == null || shuffleSeed.isBlank()) {
            throw new IllegalArgumentException("Shuffle seed is required");
        }

        List<WordProblemCandidateProjection> sortedCandidates = candidates.stream()
                .sorted(Comparator.comparingInt(WordProblemCandidateProjection::examYear)
                        .thenComparing(WordProblemCandidateProjection::examId)
                        .thenComparing(WordProblemCandidateProjection::problemId))
                .toList();
        validateCandidateIds(sortedCandidates);

        Map<Long, List<Long>> problemIdsByExam = new HashMap<>();
        for (WordProblemCandidateProjection candidate : sortedCandidates) {
            problemIdsByExam.computeIfAbsent(candidate.examId(), ignored -> new ArrayList<>()).add(candidate.problemId());
        }

        Random random = new Random(seedAsLong(shuffleSeed));
        List<Long> examIds = new ArrayList<>(problemIdsByExam.keySet());
        Collections.shuffle(examIds, random);

        List<Deque<Long>> groups = new ArrayList<>();
        for (Long examId : examIds) {
            List<Long> problemIds = problemIdsByExam.get(examId);
            Collections.shuffle(problemIds, random);
            groups.add(new ArrayDeque<>(problemIds));
        }

        List<Long> order = new ArrayList<>(sortedCandidates.size());
        while (!groups.isEmpty()) {
            for (int index = 0; index < groups.size();) {
                Deque<Long> group = groups.get(index);
                order.add(group.removeFirst());
                if (group.isEmpty()) {
                    groups.remove(index);
                } else {
                    index++;
                }
            }
        }
        return List.copyOf(order);
    }

    private void validateCandidateIds(List<WordProblemCandidateProjection> candidates) {
        Set<Long> problemIds = new HashSet<>();
        for (WordProblemCandidateProjection candidate : candidates) {
            if (candidate == null || candidate.problemId() == null || candidate.examId() == null
                    || !problemIds.add(candidate.problemId())) {
                throw new IllegalArgumentException("Candidates require distinct problem and exam IDs");
            }
        }
    }

    private long seedAsLong(String shuffleSeed) {
        byte[] bytes = shuffleSeed.getBytes(StandardCharsets.UTF_8);
        long seed = 1125899906842597L;
        for (byte value : bytes) {
            seed = 31 * seed + value;
        }
        return seed;
    }
}
