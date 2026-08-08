package com.cpa.yusin.quiz.problem.infrastructure;

import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.dto.AdminProblemSearchProjection;
import com.cpa.yusin.quiz.problem.service.dto.ProblemCountByExamProjection;
import com.cpa.yusin.quiz.problem.service.dto.WordProblemCandidateProjection;
import com.cpa.yusin.quiz.problem.service.dto.WordProblemCountProjection;
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

    @Query("SELECT new com.cpa.yusin.quiz.problem.service.dto.WordProblemCountProjection(s.id, COUNT(p)) " +
            "FROM Subject s " +
            "LEFT JOIN Exam e ON e.subjectId = s.id " +
            "AND e.isRemoved = false " +
            "AND e.status = com.cpa.yusin.quiz.exam.domain.ExamStatus.PUBLISHED " +
            "LEFT JOIN Problem p ON p.exam = e " +
            "AND p.isRemoved = false " +
            "AND p.requiresCalculation = false " +
            "WHERE s.isRemoved = false " +
            "AND (s.status = com.cpa.yusin.quiz.subject.domain.SubjectStatus.PUBLISHED OR s.status IS NULL) " +
            "GROUP BY s.id")
    List<WordProblemCountProjection> countPublishedWordProblemsBySubject();

    @Query("SELECT new com.cpa.yusin.quiz.problem.service.dto.WordProblemCandidateProjection(p.id, e.id, e.year) " +
            "FROM Problem p " +
            "JOIN p.exam e " +
            "JOIN Subject s ON s.id = e.subjectId " +
            "WHERE s.id = :subjectId " +
            "AND s.isRemoved = false " +
            "AND (s.status = com.cpa.yusin.quiz.subject.domain.SubjectStatus.PUBLISHED OR s.status IS NULL) " +
            "AND e.isRemoved = false " +
            "AND e.status = com.cpa.yusin.quiz.exam.domain.ExamStatus.PUBLISHED " +
            "AND p.isRemoved = false " +
            "AND p.requiresCalculation = false " +
            "ORDER BY e.year ASC, e.id ASC, p.id ASC")
    List<WordProblemCandidateProjection> findPublishedWordProblemCandidatesBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * 말문제 회차의 스냅샷 ID를 현재 공개 계층으로 재검증하는 배치 조회다.
     * IN 절의 DB 반환 순서는 호출자가 신뢰하지 않고 회차 order로 다시 정렬한다.
     */
    @Query("SELECT p FROM Problem p " +
            "JOIN p.exam e " +
            "JOIN Subject s ON s.id = e.subjectId " +
            "WHERE p.id IN :problemIds " +
            "AND p.isRemoved = false " +
            "AND p.requiresCalculation = false " +
            "AND e.isRemoved = false " +
            "AND e.status = com.cpa.yusin.quiz.exam.domain.ExamStatus.PUBLISHED " +
            "AND s.isRemoved = false " +
            "AND (s.status = com.cpa.yusin.quiz.subject.domain.SubjectStatus.PUBLISHED OR s.status IS NULL)")
    List<Problem> findPublishedWordProblemsByIds(@Param("problemIds") List<Long> problemIds);

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
