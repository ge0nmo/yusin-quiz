package com.cpa.yusin.quiz.problem.infrastructure;

import com.cpa.yusin.quiz.problem.domain.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemJpaRepository extends JpaRepository<Problem, Long>
{
    @Query("SELECT p FROM Problem p WHERE p.exam.id = :examId ORDER BY p.number ASC")
    List<Problem> findAllByExamId(@Param("examId") long examId);

    boolean existsByExamIdAndNumber(@Param("examId") Long examId, @Param("number") int number);

    @Modifying
    @Query("DELETE FROM Problem p " +
            "WHERE p.exam.id IN (SELECT e.id FROM Exam e WHERE e.subjectId = :subjectId)")
    void deleteAllByExamSubjectId(@Param("subjectId") long subjectId);

    @Modifying
    @Query("DELETE FROM Problem p " +
            "WHERE p.exam.id = :examId ")
    void deleteAllByExamId(@Param("examId") long examId);
}
