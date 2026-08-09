package com.cpa.yusin.quiz.answer.controller;

import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerRegisterRequest;
import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerUpdateRequest;
import com.cpa.yusin.quiz.answer.controller.dto.response.AnswerDTO;
import com.cpa.yusin.quiz.answer.controller.port.AnswerService;
import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.common.controller.dto.response.PageInfo;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class AnswerController {
    public final AnswerService answerService;

    @PostMapping("/question/{questionId}/answer")
    public ResponseEntity<GlobalResponse<Long>> save(@PathVariable("questionId") Long questionId,
            @Validated @RequestBody AnswerRegisterRequest request,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        long response = answerService.save(request, questionId, memberDetails.getMember());

        return new ResponseEntity<>(new GlobalResponse<>(response), HttpStatus.CREATED);
    }

    @GetMapping("/question/{questionId}/answer")
    public ResponseEntity<GlobalResponse<List<AnswerDTO>>> getAnswersByQuestionId(
            @PathVariable("questionId") long questionId,
            @PageableDefault Pageable pageable) {
        Page<AnswerDTO> response = answerService.getAnswersByQuestionId(questionId, pageable);

        return ResponseEntity.ok(new GlobalResponse<>(response.getContent(), PageInfo.of(response)));
    }

    @PatchMapping("/answer/{answerId}")
    public ResponseEntity<GlobalResponse<AnswerDTO>> update(
            @PathVariable("answerId") long answerId,
            @Valid @RequestBody AnswerUpdateRequest request,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        answerService.update(request, answerId, memberDetails.getMember());
        AnswerDTO response = answerService.getAnswerById(answerId);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @DeleteMapping("/answer/{answerId}")
    public ResponseEntity<Void> deleteById(@PathVariable("answerId") long answerId,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        answerService.deleteAnswer(answerId, memberDetails.getMember());

        return ResponseEntity.noContent().build();
    }
}
