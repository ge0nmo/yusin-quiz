package com.cpa.yusin.quiz.exam.infrastructure;

import com.cpa.yusin.quiz.exam.domain.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
                        "AND e.isRemoved = false " +
                        "ORDER BY e.year DESC, e.name ASC ")
        List<Exam> findAllBySubjectId(@Param("subjectId") long subjectId);

        @Query("SELECT e FROM Exam e WHERE e.id = :id AND e.isRemoved = false ")
        Optional<Exam> findByIdAndIsRemovedFalse(long id);

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
}