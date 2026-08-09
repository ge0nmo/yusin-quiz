package com.cpa.yusin.quiz.problem.infrastructure;

import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamCode;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    @EntityGraph(attributePaths = {"exam", "exam.qualificationExam", "subjectMapping", "subjectMapping.subject", "choices"})
    @Query("select distinct p from Problem p " +
            "where (:qualificationExamId is null or p.exam.qualificationExam.id = :qualificationExamId) " +
            "and (:examId is null or p.exam.id = :examId) " +
            "and (:subjectId is null or p.subjectMapping.subject.id = :subjectId) " +
            "order by p.exam.year desc, p.number asc")
    List<Problem> searchAdmin(@Param("qualificationExamId") Long qualificationExamId,
                              @Param("examId") Long examId,
                              @Param("subjectId") Long subjectId);

    @EntityGraph(attributePaths = {"exam", "exam.qualificationExam", "subjectMapping", "subjectMapping.subject", "choices"})
    @Query("select p from Problem p where p.id = :id")
    Optional<Problem> findDetailById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"exam", "exam.qualificationExam", "subjectMapping", "subjectMapping.subject", "choices"})
    @Query("select distinct p from Problem p " +
            "where p.exam.qualificationExam.code = :code " +
            "and p.subjectMapping.subject.id = :subjectId " +
            "and p.status = com.cpa.yusin.quiz.common.domain.ContentStatus.PUBLISHED " +
            "and p.exam.status = com.cpa.yusin.quiz.common.domain.ContentStatus.PUBLISHED " +
            "and p.exam.qualificationExam.status = com.cpa.yusin.quiz.common.domain.ContentStatus.PUBLISHED " +
            "and p.subjectMapping.status = com.cpa.yusin.quiz.common.domain.ContentStatus.PUBLISHED " +
            "and p.subjectMapping.subject.status = com.cpa.yusin.quiz.common.domain.ContentStatus.PUBLISHED " +
            "order by p.exam.year desc, p.number asc")
    List<Problem> findPublished(@Param("code") QualificationExamCode code, @Param("subjectId") Long subjectId);

    @Query("select count(p) from Problem p " +
            "where p.exam.qualificationExam.code = :code and p.subjectMapping.subject.id = :subjectId " +
            "and p.status = com.cpa.yusin.quiz.common.domain.ContentStatus.PUBLISHED " +
            "and p.exam.status = com.cpa.yusin.quiz.common.domain.ContentStatus.PUBLISHED " +
            "and p.exam.qualificationExam.status = com.cpa.yusin.quiz.common.domain.ContentStatus.PUBLISHED " +
            "and p.subjectMapping.status = com.cpa.yusin.quiz.common.domain.ContentStatus.PUBLISHED " +
            "and p.subjectMapping.subject.status = com.cpa.yusin.quiz.common.domain.ContentStatus.PUBLISHED")
    long countPublished(@Param("code") QualificationExamCode code, @Param("subjectId") Long subjectId);

    boolean existsByExamId(Long examId);
    boolean existsBySubjectMappingId(Long mappingId);
    long countByExamId(Long examId);
    boolean existsByExamIdAndSubjectMappingIdAndNumber(Long examId, Long subjectMappingId, int number);
    boolean existsByExamIdAndSubjectMappingIdAndNumberAndIdNot(Long examId, Long subjectMappingId, int number, Long id);

    @Query("select coalesce(max(p.number), 0) from Problem p " +
            "where p.exam.id = :examId and p.subjectMapping.id = :mappingId")
    int findMaxNumber(@Param("examId") Long examId, @Param("mappingId") Long mappingId);
}
