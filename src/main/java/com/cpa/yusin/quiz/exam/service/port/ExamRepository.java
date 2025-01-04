package com.cpa.yusin.quiz.exam.service.port;

import com.cpa.yusin.quiz.exam.domain.Exam;

import java.util.List;
import java.util.Optional;

public interface ExamRepository
{
    Exam save(Exam exam);

    Optional<Exam> findById(long id);

    List<Exam> findAllBySubjectId(long subjectId, int year);

    List<Exam> findAllBySubjectId(long subjectId);

    void deleteById(long id);

    void deleteAllBySubjectId(long subjectId);

    boolean existsById(long id);

    boolean existsBySubjectIdAndNameAndYear(long subjectId, String name, int year);

    boolean existsByIdNotAndSubjectIdAndNameAndYear(long examId, long subjectId, String name, int year);

    List<Integer> getYearsBySubjectId(long subjectId);
}
