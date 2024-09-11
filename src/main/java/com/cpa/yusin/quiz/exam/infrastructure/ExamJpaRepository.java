package com.cpa.yusin.quiz.exam.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamJpaRepository extends JpaRepository<Exam, Long>
{
    @Query("SELECT e " +
            "FROM Exam e " +
            "WHERE e.subject.id = :subjectId " +
            "AND e.year = :year")
    List<Exam> findAllBySubjectId(@Param("subjectId") long subjectId, @Param("year") int year);

    @Modifying
    @Query("DELETE FROM Exam e WHERE e.subject.id = :subjectId")
    void deleteAllBySubjectId(@Param("subjectId") long subjectId);
}
