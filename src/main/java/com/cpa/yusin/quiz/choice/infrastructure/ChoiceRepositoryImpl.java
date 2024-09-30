package com.cpa.yusin.quiz.choice.infrastructure;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ChoiceRepositoryImpl implements ChoiceRepository
{
    private final ChoiceJpaRepository choiceJpaRepository;

    @Override
    public Choice save(Choice choice)
    {
        return choiceJpaRepository.save(choice);
    }

    @Override
    public List<Choice> saveAll(List<Choice> choices)
    {
        return choiceJpaRepository.saveAll(choices);
    }

    @Override
    public List<Choice> findAllByProblemId(long problemId)
    {
        return choiceJpaRepository.findAllByProblemId(problemId);
    }

    @Override
    public List<Choice> findAllByProblemIds(List<Long> problemIds)
    {
        return choiceJpaRepository.findAllByProblemIds(problemIds);
    }

    @Override
    public List<Choice> findAllByExamId(long examId)
    {
        return choiceJpaRepository.findAllByExamId(examId);
    }

    @Override
    public Optional<Choice> findById(long id)
    {
        return choiceJpaRepository.findById(id);
    }

    @Override
    public void deleteById(long id)
    {
        choiceJpaRepository.deleteById(id);
    }

    @Override
    public void deleteAllByIdInBatch(List<Long> ids)
    {
        choiceJpaRepository.deleteAllByIdInBatch(ids);
    }

    @Override
    public void deleteAllBySubjectId(long subjectId)
    {
        choiceJpaRepository.deleteAllByProblemExamSubjectId(subjectId);
    }

    @Override
    public void deleteAllByExamId(long examId)
    {
        choiceJpaRepository.deleteAllByProblemExamId(examId);
    }

}
