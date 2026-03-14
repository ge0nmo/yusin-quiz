package com.cpa.yusin.quiz.answer.controller;

import com.cpa.yusin.quiz.answer.controller.dto.request.AdminAnswerRegisterRequest;
import com.cpa.yusin.quiz.answer.controller.dto.request.AdminAnswerUpdateRequest;
import com.cpa.yusin.quiz.answer.controller.dto.response.AnswerDTO;
import com.cpa.yusin.quiz.answer.controller.port.AnswerService;
import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.common.controller.dto.response.PageInfo;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@RestController
public class AdminAnswerController {
    private final AnswerService answerService;

    @PostMapping("/question/{questionId}/answer")
    public ResponseEntity<GlobalResponse<Long>> createAnswer(@PathVariable("questionId") long questionId,
            @Validated @RequestBody AdminAnswerRegisterRequest request,
            Principal principal) {
        MemberDetails memberDetails = (MemberDetails) ((Authentication) principal).getPrincipal();

        long response = answerService.save(request, questionId, memberDetails.getMember());

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @PatchMapping("/answer/{answerId}")
    public ResponseEntity<GlobalResponse<Void>> updateAnswer(@PathVariable("answerId") long answerId,
            @Validated @RequestBody AdminAnswerUpdateRequest request) {
        answerService.updateInAdminPage(request, answerId);

        return ResponseEntity.ok(new GlobalResponse<>());
    }

    @GetMapping("/question/{questionId}/answer")
    public ResponseEntity<GlobalResponse<List<AnswerDTO>>> getAnswers(@PathVariable("questionId") long questionId,
            @PageableDefault Pageable pageable) {
        Page<AnswerDTO> response = answerService.getAnswersByQuestionId(questionId, pageable);

        return ResponseEntity.ok(new GlobalResponse<>(response.getContent(), PageInfo.of(response)));
    }

    @DeleteMapping("/answer/{answerId}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable("answerId") long answerId, Principal principal) {
        MemberDetails memberDetails = (MemberDetails) ((Authentication) principal).getPrincipal();
        answerService.deleteAnswer(answerId, memberDetails.getMember());

        return ResponseEntity.noContent().build();
    }
}
