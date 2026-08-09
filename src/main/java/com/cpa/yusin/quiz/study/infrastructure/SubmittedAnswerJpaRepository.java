package com.cpa.yusin.quiz.study.infrastructure;

import com.cpa.yusin.quiz.study.domain.SubmittedAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmittedAnswerJpaRepository extends JpaRepository<SubmittedAnswer, Long> {
    Optional<SubmittedAnswer> findByStudySessionIdAndProblemId(Long studySessionId, Long problemId);

    List<SubmittedAnswer> findAllByStudySessionId(Long studySessionId);
}
