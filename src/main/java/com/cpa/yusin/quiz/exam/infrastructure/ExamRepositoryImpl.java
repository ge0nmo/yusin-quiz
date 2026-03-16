package com.cpa.yusin.quiz.exam.infrastructure;

import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.domain.ExamStatus;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ExamRepositoryImpl implements ExamRepository
{
    private final ExamJpaRepository examJpaRepository;

    @Override
    public Exam save(Exam exam)
    {
        return examJpaRepository.save(exam);
    }

    @Override
    public Optional<Exam> findById(long id)
    {
        return examJpaRepository.findByIdAndIsRemovedFalse(id);
    }

    @Override
    public Optional<Exam> findPublishedById(long id) {
        return examJpaRepository.findPublishedById(id, ExamStatus.PUBLISHED);
    }

    @Override
    public List<Exam> findAllBySubjectId(long subjectId, Integer year)
    {
        return examJpaRepository.findExamsBySubjectIdAndYear(subjectId, year);
    }

    @Override
    public List<Exam> findAllBySubjectId(long subjectId)
    {
        return examJpaRepository.findAllBySubjectId(subjectId);
    }

    @Override
    public List<Exam> findAllPublishedBySubjectId(long subjectId, Integer year) {
        return examJpaRepository.findPublishedExamsBySubjectIdAndYear(subjectId, year, ExamStatus.PUBLISHED);
    }

    @Override
    public List<Exam> findAllPublishedBySubjectId(long subjectId) {
        return examJpaRepository.findAllPublishedBySubjectId(subjectId, ExamStatus.PUBLISHED);
    }

    @Override
    public boolean existsBySubjectIdAndNameAndYear(long subjectId, String name, int year)
    {
        return examJpaRepository.existsBySubjectIdAndNameAndYear(subjectId, name, year);
    }

    @Override
    public boolean existsByIdNotAndSubjectIdAndNameAndYear(long examId, long subjectId, String name, int year)
    {
        return examJpaRepository.existsByIdNotSubjectIdAndNameAndYear(examId, subjectId, name, year);
    }

    @Override
    public List<Integer> getYearsBySubjectId(long subjectId)
    {
        return examJpaRepository.getYearsBySubjectId(subjectId);
    }

    @Override
    public List<Integer> getPublishedYearsBySubjectId(long subjectId) {
        return examJpaRepository.getPublishedYearsBySubjectId(subjectId, ExamStatus.PUBLISHED);
    }
}
