package com.cpa.yusin.quiz.qualification.infrastructure;

import com.cpa.yusin.quiz.qualification.domain.QualificationExam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QualificationExamRepository extends JpaRepository<QualificationExam, Long> {
    Optional<QualificationExam> findByCode(String code);
    boolean existsByCode(String code);
    List<QualificationExam> findAllByOrderByNameAsc();
}
