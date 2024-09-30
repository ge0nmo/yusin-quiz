package com.cpa.yusin.quiz.subject.infrastructure;

import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class SubjectRepositoryImpl implements SubjectRepository
{
    private final SubjectJpaRepository subjectJpaRepository;

    @Override
    public Subject save(Subject subject)
    {
        return subjectJpaRepository.save(subject);
    }

    @Override
    public Optional<Subject> findById(long id)
    {
        return subjectJpaRepository.findById(id);
    }

    @Override
    public List<Subject> findAll()
    {
        return subjectJpaRepository.findAll();
    }

    @Override
    public boolean existsById(long id)
    {
        return subjectJpaRepository.existsById(id);
    }

    @Override
    public void deleteById(long id)
    {
        subjectJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name)
    {
        return subjectJpaRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(long id, String name)
    {
        return subjectJpaRepository.existsByNameAndIdNot(id, name);
    }
}
