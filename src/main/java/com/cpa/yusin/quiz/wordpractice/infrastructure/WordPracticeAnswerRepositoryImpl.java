package com.cpa.yusin.quiz.wordpractice.infrastructure;

import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeAnswer;
import com.cpa.yusin.quiz.wordpractice.service.port.WordPracticeAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WordPracticeAnswerRepositoryImpl implements WordPracticeAnswerRepository {

    private final WordPracticeAnswerJpaRepository answerJpaRepository;

    @Override
    /** 서비스의 최초 제출 경로가 호출하는 JPA 영속화 어댑터다. */
    public WordPracticeAnswer save(WordPracticeAnswer answer) {
        return answerJpaRepository.save(answer);
    }

    @Override
    /** HTTP 재시도와 동시 제출 뒤 기존 불변 이력을 읽는 어댑터다. */
    public Optional<WordPracticeAnswer> findByCycleIdAndProblemId(Long cycleId, Long problemId) {
        return answerJpaRepository.findByCycleIdAndProblemId(cycleId, problemId);
    }
}
