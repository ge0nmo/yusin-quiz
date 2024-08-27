package com.cpa.yusin.quiz.exam.service.port;

import com.cpa.yusin.quiz.exam.domain.ExamDomain;

import java.util.List;
import java.util.Optional;

public interface ExamRepository
{
    ExamDomain save(ExamDomain exam);

    Optional<ExamDomain> findById(long id);

    List<ExamDomain> findAllBySubjectId(long subjectId);

    void deleteById(long id);

    void deleteAllBySubjectId(long subjectId);

    boolean existsById(long id);
}
