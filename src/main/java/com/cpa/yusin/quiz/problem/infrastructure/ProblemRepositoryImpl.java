package com.cpa.yusin.quiz.problem.infrastructure;

import com.cpa.yusin.quiz.problem.domain.ProblemDomain;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ProblemRepositoryImpl implements ProblemRepository
{
    private final ProblemJpaRepository problemJpaRepository;


    @Override
    public ProblemDomain save(ProblemDomain domain)
    {
        return problemJpaRepository.save(Problem.from(domain))
                .toModel();
    }

    @Override
    public List<ProblemDomain> saveAll(List<ProblemDomain> domains)
    {
        List<Problem> problems = domains.stream()
                .map(Problem::from)
                .toList();

        return problemJpaRepository.saveAll(problems).stream()
                .map(Problem::toModel)
                .toList();
    }

    @Override
    public List<ProblemDomain> findAllByExamId(long examId)
    {
        return problemJpaRepository.findAllByExamId(examId).stream()
                .map(Problem::toModel)
                .toList();
    }

    @Override
    public Optional<ProblemDomain> findById(long id)
    {
        return problemJpaRepository.findById(id)
                .map(Problem::toModel);
    }

    @Override
    public void deleteById(long id)
    {
        problemJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(long id)
    {
        return problemJpaRepository.existsById(id);
    }
}
