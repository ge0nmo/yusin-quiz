package com.cpa.yusin.quiz.wordpractice.service.port;

import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeAnswer;

import java.util.Optional;

public interface WordPracticeAnswerRepository {

    /** 최초 답안을 저장한다. DB의 cycle/problem 유니크 제약이 이력 중복을 최종 방어한다. */
    WordPracticeAnswer save(WordPracticeAnswer answer);

    /** 같은 요청 재시도를 멱등하게 처리하기 위해 회차와 문제 기준의 기존 이력을 찾는다. */
    Optional<WordPracticeAnswer> findByCycleIdAndProblemId(Long cycleId, Long problemId);
}
