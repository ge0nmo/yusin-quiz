package com.cpa.yusin.quiz.problem.service.port;

import com.cpa.yusin.quiz.problem.domain.ProblemDomain;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository
{
    ProblemDomain save(ProblemDomain problem);

    List<ProblemDomain> saveAll(List<ProblemDomain> problems);

    List<ProblemDomain> findAllByExamId(long examId);

    Optional<ProblemDomain> findById(long id);

    void deleteById(long id);

    void deleteAllByIdInBatch(List<Long> ids);

    boolean existsById(long id);

    void deleteAllByExamId(long examId);

    void deleteAllBySubjectId(long subjectId);
}
