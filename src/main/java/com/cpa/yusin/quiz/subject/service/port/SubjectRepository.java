package com.cpa.yusin.quiz.subject.service.port;

import com.cpa.yusin.quiz.subject.domain.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository
{
    Subject save(Subject subject);

    Optional<Subject> findById(long id);

    Page<Subject> findAll(Pageable pageable);

    List<Subject> findByName(String name);

    boolean existsById(long id);

    void deleteById(long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(long id, String name);

    Page<Subject> findAllOrderByName(Pageable pageable);
}
