package com.cpa.yusin.quiz.choice.infrastructure;

import com.cpa.yusin.quiz.choice.domain.Choice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChoiceJpaRepository extends JpaRepository<Choice, Long> {
    /**
     * Choices are treated as active only while their parent problem/exam/subject
     * chain is active.
     * This keeps deleted hierarchies from resurfacing through problem detail or
     * study APIs.
     */
    @Query("SELECT c FROM Choice c " +
            "JOIN c.problem p " +
            "JOIN p.exam e " +
            "WHERE p.id = :problemId " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ")")
    List<Choice> findAllByProblemIdWithActiveHierarchy(@Param("problemId") long problemId);

    @Query("SELECT c FROM Choice c " +
            "JOIN c.problem p " +
            "JOIN p.exam e " +
            "WHERE c.id = :id " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ")")
    Optional<Choice> findByIdWithActiveHierarchy(@Param("id") long id);

    /**
     * Exam-level choice loading powers bulk problem rendering, so it must apply the
     * same ancestor checks as single-problem reads.
     */
    @Query("SELECT c FROM Choice c " +
            "JOIN Problem p ON p.id = c.problem.id " +
            "JOIN Exam e ON e.id = p.exam.id " +
            "WHERE e.id = :examId " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ") ")
    List<Choice> findAllByExamId(@Param("examId") long examId);

    @Query("SELECT c FROM Choice c " +
            "JOIN c.problem p " +
            "JOIN p.exam e " +
            "WHERE p.id IN :problemIds " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ")")
    List<Choice> findAllByProblemIdsWithActiveHierarchy(@Param("problemIds") List<Long> problemIds);
}
