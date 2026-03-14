package com.cpa.yusin.quiz.problem.service;

import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The DB keeps a physical unique key on (exam_id, number), while the domain
 * treats deleted problems as inactive content. This component bridges that gap
 * by moving removed rows into a negative tombstone range before a new active
 * problem claims the business number again.
 */
@Component
@RequiredArgsConstructor
public class ProblemNumberSlotManager {
    private static final int FIRST_DELETED_NUMBER = -1;

    private final ProblemRepository problemRepository;

    public void releaseRemovedNumberSlot(long examId, int requestedNumber) {
        problemRepository.findRemovedByExamIdAndNumber(examId, requestedNumber)
                .ifPresent(removedProblem -> {
                    removedProblem.assignDeletedNumber(nextDeletedNumber(examId));
                    problemRepository.save(removedProblem);
                    problemRepository.flush();
                });
    }

    public void deleteAndReleaseNumber(Problem problem) {
        problem.delete();
        problem.assignDeletedNumber(nextDeletedNumber(problem.getExam().getId()));
    }

    private int nextDeletedNumber(long examId) {
        Integer minimumNumber = problemRepository.findMinimumNumberByExamId(examId);

        if (minimumNumber == null || minimumNumber > FIRST_DELETED_NUMBER) {
            return FIRST_DELETED_NUMBER;
        }

        if (minimumNumber == Integer.MIN_VALUE) {
            throw new IllegalStateException("No available deleted problem number remains.");
        }

        return minimumNumber - 1;
    }
}
