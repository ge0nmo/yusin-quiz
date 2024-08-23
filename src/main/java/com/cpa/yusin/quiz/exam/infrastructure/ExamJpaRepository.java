package com.cpa.yusin.quiz.exam.infrastructure;

import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamJpaRepository extends JpaRepository<Exam, Long>
{
    @Query("SELECT e " +
            "FROM Exam e " +
            "WHERE e.subject.id = :subjectId")
    List<Exam> findAllBySubjectId(@Param("subjectId") long subjectId);
}
