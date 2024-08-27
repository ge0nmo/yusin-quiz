package com.cpa.yusin.quiz.choice.service.port;

import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;

import java.util.List;
import java.util.Optional;

public interface ChoiceRepository
{
    ChoiceDomain save(ChoiceDomain domain);

    List<ChoiceDomain> saveAll(List<ChoiceDomain> domains);

    List<ChoiceDomain> findAllByProblemId(long problemId);

    List<ChoiceDomain> findAllByProblemIds(List<Long> problemIds);

    List<ChoiceDomain> findAllByExamId(long examId);

    Optional<ChoiceDomain> findById(long id);

    void deleteById(long id);

    void deleteAllByIdInBatch(List<Long> ids);

    void deleteAllBySubjectId(long subjectId);

    void deleteAllByExamId(long examId);
}
