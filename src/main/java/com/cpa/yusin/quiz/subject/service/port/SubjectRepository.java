package com.cpa.yusin.quiz.subject.service.port;

import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import com.cpa.yusin.quiz.subject.infrastructure.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubjectRepository
{
    SubjectDomain save(SubjectDomain subjectDomain);


    Optional<SubjectDomain> findById(long id);


}
