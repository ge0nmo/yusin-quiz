package com.cpa.yusin.quiz.problem.infrastructure;

import com.cpa.yusin.quiz.problem.domain.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemJpaRepository extends JpaRepository<Problem, Long>
{
    @Query("SELECT p FROM Problem p WHERE p.exam.id = :examId AND p.isRemoved = false ORDER BY p.number ASC")
    List<Problem> findAllByExamId(@Param("examId") long examId);

    boolean existsByExamIdAndNumberAndIsRemovedFalse(@Param("examId") Long examId, @Param("number") int number);

}
