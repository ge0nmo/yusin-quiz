package com.cpa.yusin.quiz.subject.infrastructure;

import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class SubjectRepositoryImpl implements SubjectRepository
{
    private final SubjectJpaRepository subjectJpaRepository;

    @Override
    public SubjectDomain save(SubjectDomain subjectDomain)
    {
        return subjectJpaRepository.save(Subject.from(subjectDomain))
                .toModel();
    }

    @Override
    public Optional<SubjectDomain> findById(long id)
    {
        return subjectJpaRepository.findById(id)
                .map(Subject::toModel);
    }
}
