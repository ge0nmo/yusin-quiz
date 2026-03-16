package com.cpa.yusin.quiz.exam.infrastructure;

import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.domain.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamJpaRepository extends JpaRepository<Exam, Long> {
        @Query("SELECT e " +
                        "FROM Exam e " +
                        "WHERE e.subjectId = :subjectId " +
                        "AND (:year IS NULL OR e.year = :year) " +
                        "AND e.isRemoved = false " +
                        "ORDER BY e.year DESC, e.name ASC ")
        List<Exam> findExamsBySubjectIdAndYear(@Param("subjectId") long subjectId, @Param("year") Integer year);

        @Query("SELECT e " +
                        "FROM Exam e " +
                        "WHERE e.subjectId = :subjectId " +
                        "AND (:year IS NULL OR e.year = :year) " +
                        "AND e.isRemoved = false " +
                        "AND e.status = :publishedStatus " +
                        "ORDER BY e.year DESC, e.name ASC ")
        List<Exam> findPublishedExamsBySubjectIdAndYear(@Param("subjectId") long subjectId,
                                                        @Param("year") Integer year,
                                                        @Param("publishedStatus") ExamStatus publishedStatus);

        @Query("SELECT e " +
                        "FROM Exam e " +
                        "WHERE e.subjectId = :subjectId " +
                        "AND e.isRemoved = false " +
                        "ORDER BY e.year DESC, e.name ASC ")
        List<Exam> findAllBySubjectId(@Param("subjectId") long subjectId);

        @Query("SELECT e " +
                        "FROM Exam e " +
                        "WHERE e.subjectId = :subjectId " +
                        "AND e.isRemoved = false " +
                        "AND e.status = :publishedStatus " +
                        "ORDER BY e.year DESC, e.name ASC ")
        List<Exam> findAllPublishedBySubjectId(@Param("subjectId") long subjectId,
                                               @Param("publishedStatus") ExamStatus publishedStatus);

        /**
         * An exam is visible only while both the exam itself and its owning subject are
         * active.
         * This keeps subject-level soft delete from leaking stale exam IDs back into
         * user/admin APIs.
         */
        @Query("SELECT e FROM Exam e " +
                        "WHERE e.id = :id " +
                        "AND e.isRemoved = false " +
                        "AND EXISTS (" +
                        "   SELECT s.id FROM Subject s " +
                        "   WHERE s.id = e.subjectId " +
                        "   AND s.isRemoved = false" +
                        ")")
        Optional<Exam> findByIdAndIsRemovedFalse(long id);

        @Query("SELECT e FROM Exam e " +
                        "WHERE e.id = :id " +
                        "AND e.isRemoved = false " +
                        "AND e.status = :publishedStatus " +
                        "AND EXISTS (" +
                        "   SELECT s.id FROM Subject s " +
                        "   WHERE s.id = e.subjectId " +
                        "   AND s.isRemoved = false" +
                        ")")
        Optional<Exam> findPublishedById(@Param("id") long id,
                                         @Param("publishedStatus") ExamStatus publishedStatus);

        @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END " +
                        "FROM Exam e " +
                        "WHERE e.subjectId = :subjectId " +
                        "AND e.isRemoved = false " +
                        "AND e.name = :name " +
                        "AND e.year = :year")
        boolean existsBySubjectIdAndNameAndYear(@Param("subjectId") long subjectId,
                        @Param("name") String name,
                        @Param("year") int year);

        @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END " +
                        "FROM Exam e " +
                        "WHERE e.id != :examId " +
                        "AND e.isRemoved = false " +
                        "AND e.subjectId = :subjectId " +
                        "AND e.name = :name " +
                        "AND e.year = :year")
        boolean existsByIdNotSubjectIdAndNameAndYear(@Param("examId") long examId,
                        @Param("subjectId") long subjectId,
                        @Param("name") String name,
                        @Param("year") int year);

        @Query("SELECT DISTINCT e.year FROM Exam e " +
                        "WHERE e.subjectId = :subjectId " +
                        "AND e.isRemoved = false " +
                        "ORDER BY e.year DESC ")
        List<Integer> getYearsBySubjectId(@Param("subjectId") long subjectId);

        @Query("SELECT DISTINCT e.year FROM Exam e " +
                        "WHERE e.subjectId = :subjectId " +
                        "AND e.isRemoved = false " +
                        "AND e.status = :publishedStatus " +
                        "ORDER BY e.year DESC ")
        List<Integer> getPublishedYearsBySubjectId(@Param("subjectId") long subjectId,
                                                   @Param("publishedStatus") ExamStatus publishedStatus);
}
