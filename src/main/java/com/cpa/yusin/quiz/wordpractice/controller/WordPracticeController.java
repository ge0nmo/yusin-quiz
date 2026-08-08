package com.cpa.yusin.quiz.wordpractice.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeSubjectResponse;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeCycleResponse;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeProblemBatchResponse;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeAnswerResponse;
import com.cpa.yusin.quiz.wordpractice.controller.dto.request.WordPracticeAnswerRequest;
import jakarta.validation.Valid;
import com.cpa.yusin.quiz.wordpractice.controller.port.WordPracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 비회원과 회원이 함께 사용하는 말문제 빠른 풀이 API의 진입점이다.
 * subject 진행률, 회차 생성·이어풀기, 문제 묶음, 최초 답안, 명시적 재시작을 제공한다.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/problem/word-practice")
public class WordPracticeController {

    private final WordPracticeService wordPracticeService;

    /**
     * subject 목록과 최신 회차의 진행률을 조회한다.
     * 인증 회원이 있으면 guest header는 서비스에서 무시해 회원 기록만 노출한다.
     */
    @GetMapping("/subjects")
    public ResponseEntity<GlobalResponse<List<WordPracticeSubjectResponse>>> getSubjects(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken
    ) {
        Long memberId = memberDetails == null ? null : memberDetails.getMember().getId();
        return ResponseEntity.ok(GlobalResponse.success(wordPracticeService.getSubjects(memberId, guestToken)));
    }

    /**
     * 선택한 subject의 최신 회차를 이어 풀거나, 없으면 첫 회차를 만든다.
     * 이 요청에서만 token 없는 익명 사용자에게 guest token을 발급할 수 있다.
     */
    @PostMapping("/subjects/{subjectId}/cycle")
    public ResponseEntity<GlobalResponse<WordPracticeCycleResponse>> startOrResumeCycle(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @PathVariable Long subjectId
    ) {
        Long memberId = memberDetails == null ? null : memberDetails.getMember().getId();
        return ResponseEntity.ok(GlobalResponse.success(
                wordPracticeService.startOrResumeCycle(memberId, guestToken, subjectId)));
    }

    /**
     * 현재 회차의 답안 제출 대기 문제를 5·10·15개 단위로 조회한다.
     * 인증 회원이 있으면 guest header보다 회원 참여자를 우선해 소유권을 확인한다.
     */
    @GetMapping("/cycles/{cycleId}/problems")
    public ResponseEntity<GlobalResponse<WordPracticeProblemBatchResponse>> getNextProblems(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @PathVariable Long cycleId,
            @RequestParam int count
    ) {
        Long memberId = memberDetails == null ? null : memberDetails.getMember().getId();
        return ResponseEntity.ok(GlobalResponse.success(
                wordPracticeService.getNextProblems(memberId, guestToken, cycleId, count)));
    }

    /** 현재 고정 순서의 문제에 첫 답안을 제출하고 즉시 피드백을 반환한다. */
    @PostMapping("/cycles/{cycleId}/answers")
    public ResponseEntity<GlobalResponse<WordPracticeAnswerResponse>> submitAnswer(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @PathVariable Long cycleId,
            @Valid @RequestBody WordPracticeAnswerRequest request
    ) {
        Long memberId = memberDetails == null ? null : memberDetails.getMember().getId();
        return ResponseEntity.ok(GlobalResponse.success(
                wordPracticeService.submitAnswer(memberId, guestToken, cycleId, request.problemId(), request.choiceId())));
    }

    /** 완료 화면에서 사용자가 명시적으로 선택했을 때만 다음 회차를 새 순서로 생성한다. */
    @PostMapping("/cycles/{cycleId}/restart")
    public ResponseEntity<GlobalResponse<WordPracticeCycleResponse>> restartCycle(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @PathVariable Long cycleId
    ) {
        Long memberId = memberDetails == null ? null : memberDetails.getMember().getId();
        return ResponseEntity.ok(GlobalResponse.success(
                wordPracticeService.restartCycle(memberId, guestToken, cycleId)));
    }
}
