package com.cpa.yusin.quiz.study.controller;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.study.controller.dto.request.ExamFinishRequest;
import com.cpa.yusin.quiz.study.controller.dto.request.ExamStartRequest;
import com.cpa.yusin.quiz.study.controller.dto.request.ExamSubmitRequest;
import com.cpa.yusin.quiz.study.controller.dto.response.ExamAnswerResponse;
import com.cpa.yusin.quiz.study.controller.dto.response.ExamFinishResponse;
import com.cpa.yusin.quiz.study.controller.dto.response.ExamStartResponse;
import com.cpa.yusin.quiz.study.controller.dto.response.SubmittedAnswerResponse;
import com.cpa.yusin.quiz.study.domain.StudySession;
import com.cpa.yusin.quiz.study.domain.SubmittedAnswer;
import com.cpa.yusin.quiz.study.service.StudySessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/study")
@RestController
public class StudySessionController {

    private final StudySessionService studySessionService;

    // 1. 시험 시작 (또는 이어풀기)
    @PostMapping("/exam/start")
    public ResponseEntity<ExamStartResponse> startExam(
            @AuthenticationPrincipal Member member,
            @RequestBody ExamStartRequest request) {

        StudySession session = studySessionService.startSession(member, request.getExamId(), request.getMode());
        List<SubmittedAnswer> answers = studySessionService.getSubmittedAnswers(session.getId());

        List<SubmittedAnswerResponse> answerResponses = answers.stream()
                .map(SubmittedAnswerResponse::from)
                .toList();

        return ResponseEntity.ok(ExamStartResponse.of(session, answerResponses));
    }

    // 2. 답안 제출 (실시간 저장)
    @PostMapping("/answer")
    public ResponseEntity<ExamAnswerResponse> saveAnswer(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid ExamSubmitRequest request) {

        ExamAnswerResponse response = studySessionService.saveAnswer(
                request.getSessionId(),
                request.getProblemId(),
                request.getChoiceId(),
                request.getIndex());

        return ResponseEntity.ok(response);
    }

    // 3. 시험 종료
    @PostMapping("/finish")
    public ResponseEntity<ExamFinishResponse> finishExam(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid ExamFinishRequest request) {

        int finalScore = studySessionService.completeSession(request.getSessionId());

        return ResponseEntity.ok(new ExamFinishResponse(finalScore));
    }
}
