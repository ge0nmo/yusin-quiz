package com.cpa.yusin.quiz.exam.infrastructure;

import com.cpa.yusin.quiz.exam.domain.ExamDomain;
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
    public ExamDomain save(ExamDomain exam)
    {
        return examJpaRepository.save(Exam.from(exam))
                .toModel();
    }

    @Override
    public Optional<ExamDomain> findById(long id)
    {
        return examJpaRepository.findById(id)
                .map(Exam::toModel);
    }

    @Override
    public List<ExamDomain> findAllBySubjectId(long subjectId, int year)
    {
        return examJpaRepository.findAllBySubjectId(subjectId, year).stream()
                .map(Exam::toModel)
                .toList();
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
