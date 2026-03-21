package com.cpa.yusin.quiz.problem.infrastructure;

import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.dto.AdminProblemSearchProjection;
import com.cpa.yusin.quiz.problem.service.dto.ProblemCountByExamProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProblemJpaRepository extends JpaRepository<Problem, Long>
{
    /**
     * Problems follow a query-gated soft-delete model.
     * Even if the problem row still exists, it becomes inaccessible when the parent
     * exam or subject is removed.
     */
    @Query("SELECT p FROM Problem p " +
            "JOIN p.exam e " +
            "WHERE e.id = :examId " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ") " +
            "ORDER BY p.number ASC")
    List<Problem> findAllByExamIdWithActiveHierarchy(@Param("examId") long examId);

    /**
     * Direct ID lookup must honor the full active hierarchy so callers cannot bypass
     * a deleted parent by guessing a child ID.
     */
    @Query("SELECT p FROM Problem p " +
            "JOIN p.exam e " +
            "WHERE p.id = :id " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ")")
    Optional<Problem> findByIdWithActiveHierarchy(@Param("id") long id);

    /**
     * Duplicate-number validation should ignore removed parents for the same reason
     * reads do: deleted subject/exam trees are treated as inaccessible, not active
     * content.
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Problem p " +
            "JOIN p.exam e " +
            "WHERE e.id = :examId " +
            "AND p.number = :number " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ")")
    boolean existsByExamIdAndNumberAndIsRemovedFalse(@Param("examId") Long examId, @Param("number") int number);

    @Query("SELECT p FROM Problem p " +
            "WHERE p.exam.id = :examId " +
            "AND p.number = :number " +
            "AND p.isRemoved = true")
    Optional<Problem> findRemovedByExamIdAndNumber(@Param("examId") long examId, @Param("number") int number);

    @Query("SELECT MIN(p.number) FROM Problem p WHERE p.exam.id = :examId")
    Integer findMinimumNumberByExamId(@Param("examId") long examId);

    @Query("SELECT COUNT(p) FROM Problem p " +
            "JOIN p.exam e " +
            "WHERE e.id = :examId " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ")")
    long countActiveByExamId(@Param("examId") long examId);

    @Query("SELECT new com.cpa.yusin.quiz.problem.service.dto.ProblemCountByExamProjection(e.id, COUNT(p)) " +
            "FROM Problem p " +
            "JOIN p.exam e " +
            "WHERE e.id IN :examIds " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ") " +
            "GROUP BY e.id")
    List<ProblemCountByExamProjection> countActiveByExamIds(@Param("examIds") List<Long> examIds);

    @Query(
            value = """
                    SELECT new com.cpa.yusin.quiz.problem.service.dto.AdminProblemSearchProjection(
                        p,
                        s.id,
                        s.name,
                        e.id,
                        e.name,
                        e.year,
                        (SELECT COUNT(c) FROM Choice c WHERE c.problem.id = p.id),
                        (SELECT COUNT(c) FROM Choice c WHERE c.problem.id = p.id AND c.isAnswer = true)
                    )
                    FROM Problem p
                    JOIN p.exam e
                    JOIN Subject s ON s.id = e.subjectId
                    WHERE p.isRemoved = false
                    AND e.isRemoved = false
                    AND s.isRemoved = false
                    AND (:subjectId IS NULL OR s.id = :subjectId)
                    AND (:year IS NULL OR e.year = :year)
                    AND (:examId IS NULL OR e.id = :examId)
                    AND (
                        :lectureStatus = 'ALL' OR
                        (:lectureStatus = 'WITH_LECTURE'
                            AND TRIM(COALESCE(p.lectureYoutubeUrl, '')) <> '') OR
                        (:lectureStatus = 'WITHOUT_LECTURE'
                            AND TRIM(COALESCE(p.lectureYoutubeUrl, '')) = '')
                    )
                    ORDER BY e.year DESC, e.id DESC, p.number ASC, p.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM Problem p
                    JOIN p.exam e
                    JOIN Subject s ON s.id = e.subjectId
                    WHERE p.isRemoved = false
                    AND e.isRemoved = false
                    AND s.isRemoved = false
                    AND (:subjectId IS NULL OR s.id = :subjectId)
                    AND (:year IS NULL OR e.year = :year)
                    AND (:examId IS NULL OR e.id = :examId)
                    AND (
                        :lectureStatus = 'ALL' OR
                        (:lectureStatus = 'WITH_LECTURE'
                            AND TRIM(COALESCE(p.lectureYoutubeUrl, '')) <> '') OR
                        (:lectureStatus = 'WITHOUT_LECTURE'
                            AND TRIM(COALESCE(p.lectureYoutubeUrl, '')) = '')
                    )
                    """
    )
    Page<AdminProblemSearchProjection> searchAdminProblems(@Param("lectureStatus") String lectureStatus,
                                                           @Param("subjectId") Long subjectId,
                                                           @Param("year") Integer year,
                                                           @Param("examId") Long examId,
                                                           Pageable pageable);

}
