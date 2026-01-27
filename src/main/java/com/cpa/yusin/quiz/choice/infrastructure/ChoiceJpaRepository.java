package com.cpa.yusin.quiz.choice.infrastructure;

import com.cpa.yusin.quiz.choice.domain.Choice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChoiceJpaRepository extends JpaRepository<Choice, Long>
{
    @Query("SELECT c FROM Choice c WHERE c.problem.id = :problemId ")
    List<Choice> findAllByProblemId(@Param("problemId") long problemId);

    @Query("SELECT c FROM Choice c " +
            "JOIN Problem p ON p.id = c.problem.id " +
            "JOIN Exam e ON e.id = p.exam.id " +
            "WHERE e.id = :examId ")
    List<Choice> findAllByExamId(@Param("examId") long examId);
}
