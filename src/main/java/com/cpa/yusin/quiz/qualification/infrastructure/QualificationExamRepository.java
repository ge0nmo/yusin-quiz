package com.cpa.yusin.quiz.qualification.infrastructure;

import com.cpa.yusin.quiz.qualification.domain.QualificationExam;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QualificationExamRepository extends JpaRepository<QualificationExam, Long> {
    Optional<QualificationExam> findByCode(QualificationExamCode code);
    boolean existsByCode(QualificationExamCode code);
    List<QualificationExam> findAllByOrderByNameAsc();
}
