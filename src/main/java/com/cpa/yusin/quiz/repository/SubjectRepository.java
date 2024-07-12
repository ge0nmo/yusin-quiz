package com.cpa.yusin.quiz.repository;

import com.cpa.yusin.quiz.domain.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
}
