package com.cpa.yusin.quiz.wordpractice.controller.port;

import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeSubjectResponse;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeCycleResponse;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeProblemBatchResponse;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeAnswerResponse;
import com.cpa.yusin.quiz.wordpractice.controller.dto.request.WordPracticeAnswerRequest;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeAnswerBatchResponse;

import java.util.List;

/** 말문제 빠른 풀이 HTTP 진입점이 사용하는 유스케이스 계약이다. */
public interface WordPracticeService {

    /** subject 카탈로그와 현재 회원 또는 익명 참여자의 진행률을 결합해 반환한다. */
    List<WordPracticeSubjectResponse> getSubjects(Long memberId, String guestToken);

    /** subject의 최신 회차를 재사용하거나 최초 회차를 생성해 반환한다. */
    WordPracticeCycleResponse startOrResumeCycle(Long memberId, String guestToken, Long subjectId);

    /** 현재 회차의 nextIndex부터 답안 제출 전 미리 볼 문제를 고정 순서로 반환한다. */
    WordPracticeProblemBatchResponse getNextProblems(Long memberId, String guestToken, Long cycleId, int count);

    /** 현재 순서의 문제에 대한 최초 답안을 저장하고 즉시 정오답 및 진행률을 반환한다. */
    WordPracticeAnswerResponse submitAnswer(Long memberId, String guestToken, Long cycleId, Long problemId, Long choiceId);

    /** 현재 문제 묶음 전체를 원자적으로 저장하며, 같은 payload 재전송은 기존 결과를 반환한다. */
    WordPracticeAnswerBatchResponse submitAnswerBatch(
            Long memberId,
            String guestToken,
            Long cycleId,
            List<WordPracticeAnswerRequest> answers
    );

    /** 완료된 최신 회차를 기준으로 새 카탈로그와 새 셔플 순서를 가진 다음 회차를 시작한다. */
    WordPracticeCycleResponse restartCycle(Long memberId, String guestToken, Long cycleId);
}
