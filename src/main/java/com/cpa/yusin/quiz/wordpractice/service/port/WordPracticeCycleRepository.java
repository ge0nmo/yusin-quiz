package com.cpa.yusin.quiz.wordpractice.service.port;

import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeCycle;

import java.util.Optional;
import java.util.List;

public interface WordPracticeCycleRepository {

    WordPracticeCycle save(WordPracticeCycle cycle);

    Optional<WordPracticeCycle> findById(Long cycleId);

    Optional<WordPracticeCycle> findByIdWithLock(Long cycleId);

    Optional<WordPracticeCycle> findLatestByParticipantIdAndSubjectId(Long participantId, Long subjectId);

    /**
     * 최초 회차 생성 경합에서 사용한다. 참여자 잠금 뒤 최신 회차를 current read로 다시 확인해
     * 이미 커밋된 round 1을 재사용한다.
     */
    Optional<WordPracticeCycle> findLatestByParticipantIdAndSubjectIdWithLock(Long participantId, Long subjectId);

    /**
     * 말문제 진입 목록에서 subject별 진행률을 만들 때 사용한다.
     * 참여자의 과거 회차 전체를 메모리로 읽지 않고 subject별 최신 회차만 조회한다.
     */
    List<WordPracticeCycle> findLatestByParticipantId(Long participantId);
}
