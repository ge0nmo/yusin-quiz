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

    @Query("SELECT c FROM Choice c WHERE c.problem.id in :problemIds")
    List<Choice> findAllByProblemIds(@Param("problemIds") List<Long> problemIds);

    @Query("SELECT c FROM Choice c " +
            "JOIN Problem p ON p.id = c.problem.id " +
            "JOIN Exam e ON e.id = p.exam.id " +
            "WHERE e.id = :examId ")
    List<Choice> findAllByExamId(@Param("examId") long examId);

    @Modifying
    @Query("DELETE FROM Choice c " +
            "WHERE c.problem.id IN " +
            "(SELECT p.id FROM Problem p WHERE p.exam.id IN " +
            "(SELECT e.id FROM Exam e WHERE e.subjectId = :subjectId))")
    void deleteAllByProblemExamSubjectId(@Param("subjectId") long subjectId);

    @Modifying
    @Query("DELETE FROM Choice c " +
            "WHERE c.problem.id IN " +
            "(SELECT p.id FROM Problem p WHERE p.exam.id = :examId)")
    void deleteAllByProblemExamId(@Param("examId") long examId);
}
