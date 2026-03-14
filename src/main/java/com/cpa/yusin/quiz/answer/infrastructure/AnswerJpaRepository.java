package com.cpa.yusin.quiz.answer.infrastructure;

import com.cpa.yusin.quiz.answer.domain.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnswerJpaRepository extends JpaRepository<Answer, Long>
{
    /**
     * Answers do not own a soft-delete flag, so accessibility is inherited from the
     * question/problem/exam/subject chain.
     */
    @Query("SELECT a FROM Answer a " +
            "JOIN a.question q " +
            "JOIN q.problem p " +
            "JOIN p.exam e " +
            "WHERE a.id = :id " +
            "AND q.isRemoved = false " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ")")
    Optional<Answer> findByIdWithActiveHierarchy(@Param("id") Long id);

    /**
     * Listing answers must reuse the same active-hierarchy rule as detail lookup.
     * Otherwise deleted questions would still expose historical answer threads.
     */
    @Query("SELECT a FROM Answer a " +
            "JOIN a.question q " +
            "JOIN q.problem p " +
            "JOIN p.exam e " +
            "WHERE q.id = :questionId " +
            "AND q.isRemoved = false " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ") " +
            "ORDER BY a.createdAt ASC ")
    Page<Answer> findByQuestionId(@Param("questionId") long questionId, Pageable pageable);

    @Query("SELECT a FROM Answer a " +
            "JOIN a.question q " +
            "JOIN q.problem p " +
            "JOIN p.exam e " +
            "WHERE q.id = :questionId " +
            "AND q.isRemoved = false " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ") " +
            "ORDER BY a.createdAt ASC ")
    List<Answer> findByQuestionId(long questionId);

    /**
     * Aggregate checks such as answer-count logic should also ignore deleted parent
     * trees to keep question status consistent with visible data.
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Answer a " +
            "JOIN a.question q " +
            "JOIN q.problem p " +
            "JOIN p.exam e " +
            "WHERE q.id = :questionId " +
            "AND q.isRemoved = false " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ")")
    boolean existsByQuestionId(@Param("questionId") long questionId);
}
