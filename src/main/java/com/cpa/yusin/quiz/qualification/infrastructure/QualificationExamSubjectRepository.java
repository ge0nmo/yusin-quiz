package com.cpa.yusin.quiz.qualification.infrastructure;

import com.cpa.yusin.quiz.qualification.domain.QualificationExamSubject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QualificationExamSubjectRepository extends JpaRepository<QualificationExamSubject, Long> {
    List<QualificationExamSubject> findAllByQualificationExamIdOrderByDisplayOrderAscSubjectNameAsc(Long qualificationExamId);
    Optional<QualificationExamSubject> findByQualificationExamIdAndSubjectId(Long qualificationExamId, Long subjectId);
    void deleteAllByQualificationExamId(Long qualificationExamId);
    boolean existsBySubjectId(Long subjectId);
}
