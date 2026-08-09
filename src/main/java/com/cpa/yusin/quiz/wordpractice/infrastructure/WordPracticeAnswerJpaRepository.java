package com.cpa.yusin.quiz.wordpractice.infrastructure;

import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface WordPracticeAnswerJpaRepository extends JpaRepository<WordPracticeAnswer, Long> {

    Optional<WordPracticeAnswer> findByCycleIdAndProblemId(Long cycleId, Long problemId);

    List<WordPracticeAnswer> findAllByCycleIdAndProblemIdIn(Long cycleId, List<Long> problemIds);
}
