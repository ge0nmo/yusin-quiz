package com.cpa.yusin.quiz.problem.service.port;

import com.cpa.yusin.quiz.problem.domain.Problem;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository
{
    Problem save(Problem problem);

    List<Problem> saveAll(List<Problem> problems);

    List<Problem> findAllByExamId(long examId);

    Optional<Problem> findById(long id);

    void deleteById(long id);

    void deleteAllByIdInBatch(List<Long> ids);

    boolean existsById(long id);

    void deleteAllByExamId(long examId);

    void deleteAllBySubjectId(long subjectId);
}
