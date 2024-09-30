package com.cpa.yusin.quiz.exam.infrastructure;

import com.cpa.yusin.quiz.exam.domain.Exam;
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
        return examJpaRepository.findById(id);
    }

    @Override
    public List<Exam> findAllBySubjectId(long subjectId, int year)
    {
        return examJpaRepository.findAllBySubjectId(subjectId, year);
    }

    @Override
    public void deleteById(long id)
    {
        examJpaRepository.deleteById(id);
    }

    @Override
    public void deleteAllBySubjectId(long subjectId)
    {
        examJpaRepository.deleteAllBySubjectId(subjectId);
    }

    @Override
    public boolean existsById(long id)
    {
        return examJpaRepository.existsById(id);
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

}
