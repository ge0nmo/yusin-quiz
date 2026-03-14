package com.cpa.yusin.quiz.question.infrastructure;

import com.cpa.yusin.quiz.question.domain.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface QuestionJpaRepository extends JpaRepository<Question, Long>
{
    /**
     * Questions are visible only when the full chain
     * question -> problem -> exam -> subject remains active.
     */
    @Query("SELECT q FROM Question q " +
            "JOIN q.problem p " +
            "JOIN p.exam e " +
            "WHERE p.id = :problemId " +
            "AND q.isRemoved = false " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ") " +
            "ORDER BY q.createdAt DESC ")
    Page<Question> findAllByProblemId(@Param("problemId") long problemId,
                                      Pageable pageable);

    @Query(
            value = """
                    SELECT q FROM Question q
                    JOIN FETCH q.problem p
                    JOIN p.exam e
                    JOIN FETCH q.member m
                    WHERE q.isRemoved = false
                    AND p.isRemoved = false
                    AND e.isRemoved = false
                    AND EXISTS (
                        SELECT s.id FROM Subject s
                        WHERE s.id = e.subjectId
                        AND s.isRemoved = false
                    )
                    AND (:answeredByAdmin IS NULL OR q.answeredByAdmin = :answeredByAdmin)
                    AND (
                        :keyword IS NULL OR
                        LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                        LOWER(COALESCE(q.content, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                        LOWER(m.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                        LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    )
                    AND (:createdAtFrom IS NULL OR q.createdAt >= :createdAtFrom)
                    AND (:createdAtTo IS NULL OR q.createdAt < :createdAtTo)
                    """,
            countQuery = """
                    SELECT COUNT(q) FROM Question q
                    JOIN q.problem p
                    JOIN p.exam e
                    JOIN q.member m
                    WHERE q.isRemoved = false
                    AND p.isRemoved = false
                    AND e.isRemoved = false
                    AND EXISTS (
                        SELECT s.id FROM Subject s
                        WHERE s.id = e.subjectId
                        AND s.isRemoved = false
                    )
                    AND (:answeredByAdmin IS NULL OR q.answeredByAdmin = :answeredByAdmin)
                    AND (
                        :keyword IS NULL OR
                        LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                        LOWER(COALESCE(q.content, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                        LOWER(m.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                        LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    )
                    AND (:createdAtFrom IS NULL OR q.createdAt >= :createdAtFrom)
                    AND (:createdAtTo IS NULL OR q.createdAt < :createdAtTo)
                    """
    )
    Page<Question> findAllQuestions(@Param("answeredByAdmin") Boolean answeredByAdmin,
                                    @Param("keyword") String keyword,
                                    @Param("createdAtFrom") LocalDateTime createdAtFrom,
                                    @Param("createdAtTo") LocalDateTime createdAtTo,
                                    Pageable pageable);


    /**
     * Admin and user detail lookups share the same rule: a question under a deleted
     * parent should look exactly like a missing question.
     */
    @Query("SELECT q FROM Question q " +
            "JOIN q.problem p " +
            "JOIN p.exam e " +
            "WHERE q.id = :id " +
            "AND q.isRemoved = false " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ")")
    Optional<Question> findByIdAndIsRemovedFalse(@Param("id") long id);
}
