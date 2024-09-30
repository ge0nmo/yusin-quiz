package com.cpa.yusin.quiz.subject.service.port;

import com.cpa.yusin.quiz.subject.domain.Subject;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository
{
    Subject save(Subject subject);

    Optional<Subject> findById(long id);

    List<Subject> findAll();

    boolean existsById(long id);

    void deleteById(long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(long id, String name);
}
