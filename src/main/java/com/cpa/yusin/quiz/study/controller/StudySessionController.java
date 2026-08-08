package com.cpa.yusin.quiz.study.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.study.controller.dto.request.ExamFinishRequest;
import com.cpa.yusin.quiz.study.controller.dto.request.ExamStartRequest;
import com.cpa.yusin.quiz.study.controller.dto.request.ExamSubmitRequest;
import com.cpa.yusin.quiz.study.controller.dto.response.ExamAnswerResponse;
import com.cpa.yusin.quiz.study.controller.dto.response.ExamFinishResponse;
import com.cpa.yusin.quiz.study.controller.dto.response.ExamStartResponse;
import com.cpa.yusin.quiz.study.controller.dto.response.SubmittedAnswerResponse;
import com.cpa.yusin.quiz.study.controller.dto.response.StudyProgressAbandonResponse;
import com.cpa.yusin.quiz.study.controller.dto.response.StudySummaryResponse;
import com.cpa.yusin.quiz.study.domain.ExamMode;
import com.cpa.yusin.quiz.study.domain.StudySession;
import com.cpa.yusin.quiz.study.domain.SubmittedAnswer;
import com.cpa.yusin.quiz.study.service.StudySessionService;
import com.cpa.yusin.quiz.study.service.dto.StudySessionCompletionSummary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/study")
@RestController
public class StudySessionController {

        private final StudySessionService studySessionService;

        // 1. 시험 시작 (또는 이어풀기)
        @PostMapping("/exam/start")
        public ResponseEntity<GlobalResponse<ExamStartResponse>> startExam(
                        @AuthenticationPrincipal MemberDetails memberDetails,
                        @RequestBody ExamStartRequest request) {

                StudySession session = studySessionService.startSession(memberDetails.getMember().getId(),
                                request.getExamId(),
                                request.getMode());
                List<SubmittedAnswer> answers = studySessionService.getSubmittedAnswers(session.getId());

                List<SubmittedAnswerResponse> answerResponses = answers.stream()
                                .map(SubmittedAnswerResponse::from)
                                .toList();

                return ResponseEntity.ok(GlobalResponse.success(ExamStartResponse.of(session, answerResponses)));
        }

        // 2. 답안 제출 (실시간 저장)
        @PostMapping("/answer")
        public ResponseEntity<GlobalResponse<ExamAnswerResponse>> saveAnswer(
                        @AuthenticationPrincipal MemberDetails memberDetails,
                        @RequestBody @Valid ExamSubmitRequest request) {
                ExamAnswerResponse response = studySessionService.saveAnswer(
                                memberDetails.getMember().getId(),
                                request.getSessionId(),
                                request.getProblemId(),
                                request.getChoiceId(),
                                request.getIndex());

                return ResponseEntity.ok(GlobalResponse.success(response));
        }

        // 3. 시험 종료
        @PostMapping("/finish")
        public ResponseEntity<GlobalResponse<ExamFinishResponse>> finishExam(
                        @AuthenticationPrincipal MemberDetails memberDetails,
                        @RequestBody @Valid ExamFinishRequest request) {

                StudySessionCompletionSummary summary = studySessionService.completeSession(
                                memberDetails.getMember().getId(),
                                request.getSessionId());

                return ResponseEntity.ok(GlobalResponse.success(new ExamFinishResponse(
                                summary.finalScore(),
                                summary.correctCount(),
                                summary.totalCount(),
                                summary.answeredCount(),
                                summary.unansweredCount()
                )));
        }

        // 4. 학습 요약 조회
        @GetMapping("/summary")
        public ResponseEntity<GlobalResponse<StudySummaryResponse>> getSummary(
                        @AuthenticationPrincipal MemberDetails memberDetails) {
                StudySummaryResponse response = studySessionService.getStudySummary(memberDetails.getMember().getId());
                return ResponseEntity.ok(GlobalResponse.success(response));
        }

        // 5. 전체 진행 중 풀이 초기화
        @DeleteMapping("/progress")
        public ResponseEntity<GlobalResponse<StudyProgressAbandonResponse>> abandonAllProgress(
                        @AuthenticationPrincipal MemberDetails memberDetails) {
                StudyProgressAbandonResponse response = studySessionService.abandonProgress(
                                memberDetails.getMember().getId(), null, null);
                return ResponseEntity.ok(GlobalResponse.success(response));
        }

        // 6. 특정 시험 진행 중 풀이 초기화
        @DeleteMapping("/progress/{examId}")
        public ResponseEntity<GlobalResponse<StudyProgressAbandonResponse>> abandonExamProgress(
                        @AuthenticationPrincipal MemberDetails memberDetails,
                        @PathVariable("examId") Long examId,
                        @RequestParam(name = "mode", required = false) ExamMode mode) {
                StudyProgressAbandonResponse response = studySessionService.abandonProgress(
                                memberDetails.getMember().getId(), examId, mode);
                return ResponseEntity.ok(GlobalResponse.success(response));
        }
}
