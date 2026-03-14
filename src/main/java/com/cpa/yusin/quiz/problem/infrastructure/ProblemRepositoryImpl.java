package com.cpa.yusin.quiz.problem.infrastructure;

import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.dto.AdminProblemSearchCondition;
import com.cpa.yusin.quiz.problem.service.dto.AdminProblemSearchProjection;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public List<Problem> findAll()
    {
        return problemJpaRepository.findAll();
    }


    @Override
    public List<Problem> findAllByExamId(long examId)
    {
        return problemJpaRepository.findAllByExamIdWithActiveHierarchy(examId);
    }

    @Override
    public Optional<Problem> findById(long id)
    {
        return problemJpaRepository.findByIdWithActiveHierarchy(id);
    }


    @Override
    public boolean existsByExamIdAndNumber(Long examId, int number)
    {
        return problemJpaRepository.existsByExamIdAndNumberAndIsRemovedFalse(examId, number);
    }

    @Override
    public Page<AdminProblemSearchProjection> searchAdminProblems(Pageable pageable,
                                                                  AdminProblemSearchCondition searchCondition) {
        return problemJpaRepository.searchAdminProblems(
                searchCondition.lectureStatus().name(),
                searchCondition.subjectId(),
                searchCondition.year(),
                searchCondition.examId(),
                pageable
        );
    }
}
