package com.cpa.yusin.quiz.wordpractice.service.port;

import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeAnswer;

import java.util.Optional;
import java.util.List;

public interface WordPracticeAnswerRepository {

    /** 최초 답안을 저장한다. DB의 cycle/problem 유니크 제약이 이력 중복을 최종 방어한다. */
    WordPracticeAnswer save(WordPracticeAnswer answer);

    /** 검증이 끝난 현재 문제 묶음을 같은 트랜잭션에서 저장한다. */
    List<WordPracticeAnswer> saveAll(List<WordPracticeAnswer> answers);

    /** 같은 요청 재시도를 멱등하게 처리하기 위해 회차와 문제 기준의 기존 이력을 찾는다. */
    Optional<WordPracticeAnswer> findByCycleIdAndProblemId(Long cycleId, Long problemId);

    /** 배치 멱등 재시도 검증을 위해 요청 문제들의 기존 이력을 한 번에 읽는다. */
    List<WordPracticeAnswer> findAllByCycleIdAndProblemIds(Long cycleId, List<Long> problemIds);
}
