package com.cpa.yusin.quiz.ebook.infrastructure;

import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamCode;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import java.util.List;

/** 책 전용 조회입니다. 공개 앱의 문제/정답 노출 계약은 변경하지 않습니다. */
public interface EbookRepository extends Repository<Problem, Long> {
    String ELIGIBLE = " from Problem p where p.status = com.cpa.yusin.quiz.common.domain.ContentStatus.PUBLISHED " +
            "and p.exam.status = com.cpa.yusin.quiz.common.domain.ContentStatus.PUBLISHED " +
            "and p.exam.qualificationExam.status = com.cpa.yusin.quiz.common.domain.ContentStatus.PUBLISHED " +
            "and p.subjectMapping.status = com.cpa.yusin.quiz.common.domain.ContentStatus.PUBLISHED " +
            "and p.subjectMapping.subject.status = com.cpa.yusin.quiz.common.domain.ContentStatus.PUBLISHED ";
    String SCOPE = "and p.exam.qualificationExam.code = :code " +
            "and p.subjectMapping.subject.id in :subjects and p.exam.year in :years ";

    interface CatalogProjection {
        QualificationExamCode getQualificationCode();
        String getQualificationName();
        Long getSubjectId();
        String getSubjectName();
        Integer getYear();
        Long getQuestionCount();
    }

    @Query("select p.exam.qualificationExam.code as qualificationCode, p.exam.qualificationExam.name as qualificationName, " +
            "p.subjectMapping.subject.id as subjectId, p.subjectMapping.subject.name as subjectName, " +
            "p.exam.year as year, count(p) as questionCount " + ELIGIBLE +
            "group by p.exam.qualificationExam.code, p.exam.qualificationExam.name, " +
            "p.subjectMapping.subject.id, p.subjectMapping.subject.name, p.exam.year " +
            "order by p.exam.qualificationExam.code, p.exam.year desc, p.subjectMapping.subject.id")
    List<CatalogProjection> catalog();

    @Query("select count(p) " + ELIGIBLE + SCOPE)
    long countSelected(@Param("code") QualificationExamCode code, @Param("subjects") List<Long> subjects,
                       @Param("years") List<Integer> years);

    @EntityGraph(attributePaths = {"exam", "exam.qualificationExam", "subjectMapping", "subjectMapping.subject", "choices"})
    @Query("select distinct p " + ELIGIBLE + SCOPE)
    List<Problem> snapshot(@Param("code") QualificationExamCode code, @Param("subjects") List<Long> subjects,
                           @Param("years") List<Integer> years);
}
