package com.cpa.yusin.quiz.choice.service.port;

import com.cpa.yusin.quiz.choice.domain.Choice;

import java.util.List;
import java.util.Optional;

public interface ChoiceRepository
{
    Choice save(Choice domain);

    List<Choice> saveAll(List<Choice> domains);

    List<Choice> findAllByProblemId(long problemId);

    List<Choice> findAllByProblemIds(List<Long> problemIds);

    List<Choice> findAllByExamId(long examId);

    Optional<Choice> findById(long id);

    void deleteById(long id);

    void deleteAllByIdInBatch(List<Long> ids);

    void deleteAllBySubjectId(long subjectId);

    void deleteAllByExamId(long examId);
}
