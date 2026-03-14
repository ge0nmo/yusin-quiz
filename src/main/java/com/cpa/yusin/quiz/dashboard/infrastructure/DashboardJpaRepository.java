package com.cpa.yusin.quiz.dashboard.infrastructure;

import com.cpa.yusin.quiz.dashboard.service.port.DashboardExamContextProjection;
import com.cpa.yusin.quiz.dashboard.service.port.DashboardPendingQuestionProjection;
import com.cpa.yusin.quiz.dashboard.service.port.DashboardSubjectContextProjection;
import com.cpa.yusin.quiz.subject.domain.Subject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DashboardJpaRepository extends Repository<Subject, Long> {

    @Query("SELECT COUNT(s) FROM Subject s WHERE s.isRemoved = false")
    long countActiveSubjects();

    @Query("SELECT COUNT(e) FROM Exam e " +
            "WHERE e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ")")
    long countActiveExams();

    @Query("SELECT COUNT(p) FROM Problem p " +
            "JOIN p.exam e " +
            "WHERE p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ")")
    long countActiveProblems();

    @Query("SELECT COUNT(q) FROM Question q " +
            "JOIN q.problem p " +
            "JOIN p.exam e " +
            "WHERE q.isRemoved = false " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ")")
    long countActiveQuestions();

    @Query("SELECT COUNT(q) FROM Question q " +
            "JOIN q.problem p " +
            "JOIN p.exam e " +
            "WHERE q.isRemoved = false " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND q.createdAt >= :startOfDay " +
            "AND q.createdAt < :endOfDay " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ")")
    long countTodayQuestions(@Param("startOfDay") LocalDateTime startOfDay,
                             @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COUNT(q) FROM Question q " +
            "JOIN q.problem p " +
            "JOIN p.exam e " +
            "WHERE q.isRemoved = false " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND q.answeredByAdmin = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ")")
    long countUnansweredQuestions();

    @Query("SELECT COUNT(p) FROM Problem p " +
            "JOIN p.exam e " +
            "WHERE p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND TRIM(COALESCE(p.lectureYoutubeUrl, '')) = '' " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ")")
    long countProblemsWithoutLecture();

    @Query("SELECT new com.cpa.yusin.quiz.dashboard.service.port.DashboardPendingQuestionProjection(" +
            "q.id, q.title, m.username, q.createdAt, q.answerCount, p.id" +
            ") " +
            "FROM Question q " +
            "JOIN q.member m " +
            "JOIN q.problem p " +
            "JOIN p.exam e " +
            "WHERE q.isRemoved = false " +
            "AND q.answeredByAdmin = false " +
            "AND p.isRemoved = false " +
            "AND e.isRemoved = false " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ") " +
            "ORDER BY q.createdAt DESC, q.id DESC")
    List<DashboardPendingQuestionProjection> findPendingQuestions(Pageable pageable);

    @Query("SELECT new com.cpa.yusin.quiz.dashboard.service.port.DashboardSubjectContextProjection(" +
            "s.id, " +
            "s.name, " +
            "(SELECT COUNT(e) FROM Exam e " +
            " WHERE e.subjectId = s.id " +
            " AND e.isRemoved = false), " +
            "(SELECT COUNT(p) FROM Problem p " +
            " JOIN p.exam e " +
            " WHERE e.subjectId = s.id " +
            " AND e.isRemoved = false " +
            " AND p.isRemoved = false)" +
            ") " +
            "FROM Subject s " +
            "WHERE s.id = :subjectId " +
            "AND s.isRemoved = false")
    Optional<DashboardSubjectContextProjection> findSubjectContext(@Param("subjectId") long subjectId);

    @Query("SELECT new com.cpa.yusin.quiz.dashboard.service.port.DashboardExamContextProjection(" +
            "e.id, " +
            "e.name, " +
            "e.year, " +
            "(SELECT COUNT(p) FROM Problem p " +
            " WHERE p.exam.id = e.id " +
            " AND p.isRemoved = false), " +
            "(SELECT COUNT(q) FROM Question q " +
            " JOIN q.problem p " +
            " WHERE p.exam.id = e.id " +
            " AND p.isRemoved = false " +
            " AND q.isRemoved = false), " +
            "(SELECT COUNT(q) FROM Question q " +
            " JOIN q.problem p " +
            " WHERE p.exam.id = e.id " +
            " AND p.isRemoved = false " +
            " AND q.isRemoved = false " +
            " AND q.answeredByAdmin = false), " +
            "(SELECT COUNT(p) FROM Problem p " +
            " WHERE p.exam.id = e.id " +
            " AND p.isRemoved = false " +
            " AND TRIM(COALESCE(p.lectureYoutubeUrl, '')) <> '')" +
            ") " +
            "FROM Exam e " +
            "WHERE e.id = :examId " +
            "AND e.isRemoved = false " +
            "AND (:subjectId IS NULL OR e.subjectId = :subjectId) " +
            "AND EXISTS (" +
            "   SELECT s.id FROM Subject s " +
            "   WHERE s.id = e.subjectId " +
            "   AND s.isRemoved = false" +
            ")")
    Optional<DashboardExamContextProjection> findExamContext(@Param("examId") long examId,
                                                             @Param("subjectId") Long subjectId);
}
