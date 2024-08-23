package com.cpa.yusin.quiz.problem.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemJpaRepository extends JpaRepository<Problem, Long>
{
    @Query("SELECT p FROM Problem p WHERE p.exam.id = :examId")
    List<Problem> findAllByExamId(@Param("examId") long examId);
}
