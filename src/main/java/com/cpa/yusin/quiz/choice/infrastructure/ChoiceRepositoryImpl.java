package com.cpa.yusin.quiz.choice.infrastructure;

import com.cpa.yusin.quiz.choice.domain.ChoiceDomain;
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
    public ChoiceDomain save(ChoiceDomain domain)
    {
        return choiceJpaRepository.save(Choice.from(domain))
                .toModel();
    }

    @Override
    public List<ChoiceDomain> findAllByProblemId(long problemId)
    {
        return choiceJpaRepository.findAllByProblemId(problemId).stream()
                .map(Choice::toModel)
                .toList();
    }

    @Override
    public List<ChoiceDomain> findAllByExamId(long examId)
    {
        return choiceJpaRepository.findAllByExamId(examId).stream()
                .map(Choice::toModel)
                .toList();
    }

    @Override
    public Optional<ChoiceDomain> findById(long id)
    {
        return choiceJpaRepository.findById(id)
                .map(Choice::toModel);
    }

    @Override
    public void deleteById(long id)
    {
        choiceJpaRepository.deleteById(id);
    }
}
