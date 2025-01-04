package com.cpa.yusin.quiz.exam.infrastructure;

import com.cpa.yusin.quiz.exam.domain.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamJpaRepository extends JpaRepository<Exam, Long>
{
    @Query("SELECT e " +
            "FROM Exam e " +
            "WHERE e.subjectId = :subjectId " +
            "AND e.year = :year")
    List<Exam> findAllBySubjectId(@Param("subjectId") long subjectId, @Param("year") int year);

    @Query("SELECT e " +
            "FROM Exam e " +
            "WHERE e.subjectId = :subjectId")
    List<Exam> findAllBySubjectId(@Param("subjectId") long subjectId);

    @Modifying
    @Query("DELETE FROM Exam e WHERE e.subjectId = :subjectId")
    void deleteAllBySubjectId(@Param("subjectId") long subjectId);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Exam e " +
            "WHERE e.subjectId = :subjectId " +
            "AND e.name = :name " +
            "AND e.year = :year")
    boolean existsBySubjectIdAndNameAndYear(@Param("subjectId") long subjectId,
                                            @Param("name") String name,
                                            @Param("year") int year);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Exam e " +
            "WHERE e.id != :examId " +
            "AND e.subjectId = :subjectId " +
            "AND e.name = :name " +
            "AND e.year = :year")
    boolean existsByIdNotSubjectIdAndNameAndYear(@Param("examId") long examId,
                                                 @Param("subjectId") long subjectId,
                                                 @Param("name") String name,
                                                 @Param("year") int year);

    @Query("SELECT DISTINCT e.year FROM Exam e " +
            "WHERE e.subjectId = :subjectId " +
            "ORDER BY e.year DESC ")
    List<Integer> getYearsBySubjectId(long subjectId);
}
