package com.cpa.yusin.quiz.wordpractice.infrastructure;

import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WordPracticeAnswerJpaRepository extends JpaRepository<WordPracticeAnswer, Long> {

    Optional<WordPracticeAnswer> findByCycleIdAndProblemId(Long cycleId, Long problemId);
}
