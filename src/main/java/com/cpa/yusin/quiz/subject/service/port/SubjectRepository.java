package com.cpa.yusin.quiz.subject.service.port;

import com.cpa.yusin.quiz.subject.infrastructure.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
}
