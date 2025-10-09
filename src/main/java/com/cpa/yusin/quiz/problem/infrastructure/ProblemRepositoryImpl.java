package com.cpa.yusin.quiz.problem.infrastructure;

import com.cpa.yusin.quiz.problem.domain.Problem;
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
    public Problem save(Problem problem)
    {
        return problemJpaRepository.save(problem);
    }

    @Override
    public List<Problem> saveAll(List<Problem> problems)
    {
        return problemJpaRepository.saveAll(problems);
    }

    @Override
    public List<Problem> findAllByExamId(long examId)
    {
        return problemJpaRepository.findAllByExamId(examId);
    }

    @Override
    public Optional<Problem> findById(long id)
    {
        return problemJpaRepository.findById(id);
    }

    @Override
    public void deleteById(long id)
    {
        problemJpaRepository.deleteById(id);
    }

    @Override
    public void deleteAllByIdInBatch(List<Long> ids)
    {
        problemJpaRepository.deleteAllByIdInBatch(ids);
    }

    @Override
    public boolean existsById(long id)
    {
        return problemJpaRepository.existsById(id);
    }

    @Override
    public void deleteAllByExamId(long examId)
    {
        problemJpaRepository.deleteAllByExamId(examId);
    }

    @Override
    public void deleteAllBySubjectId(long subjectId)
    {
        problemJpaRepository.deleteAllByExamSubjectId(subjectId);
    }

    @Override
    public boolean existsByExamIdAndNumber(Long examId, int number)
    {
        return problemJpaRepository.existsByExamIdAndNumber(examId, number);
    }
}
