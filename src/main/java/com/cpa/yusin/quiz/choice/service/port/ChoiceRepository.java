package com.cpa.yusin.quiz.choice.service.port;

import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
import com.cpa.yusin.quiz.choice.infrastructure.Choice;

import java.util.List;
import java.util.Optional;

public interface ChoiceRepository
{
    ChoiceDomain save(ChoiceDomain domain);

    List<ChoiceDomain> saveAll(List<ChoiceDomain> domains);

    List<ChoiceDomain> findAllByProblemId(long problemId);

    List<ChoiceDomain> findAllByExamId(long examId);

    Optional<ChoiceDomain> findById(long id);

    void deleteById(long id);
}
