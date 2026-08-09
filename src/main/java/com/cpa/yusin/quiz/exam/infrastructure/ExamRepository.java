package com.cpa.yusin.quiz.exam.infrastructure;

import com.cpa.yusin.quiz.exam.domain.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findAllByQualificationExamIdOrderByYearDescNameAsc(Long qualificationExamId);
    List<Exam> findAllByOrderByYearDescNameAsc();
    boolean existsByQualificationExamId(Long qualificationExamId);
    boolean existsByQualificationExamIdAndYearAndName(Long qualificationExamId, int year, String name);
    boolean existsByQualificationExamIdAndYearAndNameAndIdNot(Long qualificationExamId, int year, String name, Long id);
}
