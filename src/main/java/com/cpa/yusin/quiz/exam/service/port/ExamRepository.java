package com.cpa.yusin.quiz.exam.service.port;

import com.cpa.yusin.quiz.exam.domain.Exam;

import java.util.List;
import java.util.Optional;

public interface ExamRepository
{
    Exam save(Exam exam);

    Optional<Exam> findById(long id);

    Optional<Exam> findPublishedById(long id);

    List<Exam> findAllBySubjectId(long subjectId, Integer year);

    List<Exam> findAllBySubjectId(long subjectId);

    List<Exam> findAllPublishedBySubjectId(long subjectId, Integer year);

    List<Exam> findAllPublishedBySubjectId(long subjectId);

    boolean existsBySubjectIdAndNameAndYear(long subjectId, String name, int year);

    boolean existsByIdNotAndSubjectIdAndNameAndYear(long examId, long subjectId, String name, int year);

    List<Integer> getYearsBySubjectId(long subjectId);

    List<Integer> getPublishedYearsBySubjectId(long subjectId);
}
