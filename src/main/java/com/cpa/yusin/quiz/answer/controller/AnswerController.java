package com.cpa.yusin.quiz.answer.controller;

import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerRegisterRequest;
import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerUpdateRequest;
import com.cpa.yusin.quiz.answer.controller.dto.response.AnswerDTO;
import com.cpa.yusin.quiz.answer.controller.port.AnswerService;
import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.common.controller.dto.response.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class AnswerController
{
    public final AnswerService answerService;

    @PostMapping("/question/{questionId}/answer")
    public ResponseEntity<GlobalResponse<Long>> save(@PathVariable("questionId") Long questionId,
                                                     @Validated @RequestBody AnswerRegisterRequest request)
    {
        long response = answerService.save(request, questionId);

        return new ResponseEntity<>(new GlobalResponse<>(response), HttpStatus.CREATED);
    }

    @PatchMapping("/answer/{answerId}")
    public ResponseEntity<GlobalResponse<AnswerDTO>> update(@PathVariable("answerId") Long answerId,
                                                            @Validated @RequestBody AnswerUpdateRequest request)
    {
        answerService.update(request, answerId);
        AnswerDTO response = answerService.getAnswerById(answerId);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @GetMapping("/answer/{answerId}")
    public ResponseEntity<GlobalResponse<AnswerDTO>> getAnswerById(@PathVariable("answerId") long answerId)
    {
        AnswerDTO response = answerService.getAnswerById(answerId);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @GetMapping("/question/{questionId}/answer")
    public ResponseEntity<GlobalResponse<List<AnswerDTO>>> getAnswersByQuestionId(@PathVariable("questionId") long questionId,
                                                                                  @PageableDefault Pageable pageable)
    {
        Page<AnswerDTO> response = answerService.getAnswersByQuestionId(questionId, pageable.previousOrFirst());

        return ResponseEntity.ok(new GlobalResponse<>(response.getContent(), PageInfo.of(response)));
    }

    @GetMapping("/answer/{answerId}/verification")
    public ResponseEntity<GlobalResponse<Boolean>> verifyPassword(@PathVariable("answerId") long answerId,
                                                                 @RequestParam("password") String password)
    {
        boolean response = answerService.verifyPassword(answerId, password);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @DeleteMapping("/answer/{answerId}")
    public ResponseEntity<?> deleteById(@PathVariable("answerId") long answerId)
    {
        answerService.deleteAnswer(answerId);

        return new ResponseEntity<>(new GlobalResponse<>(true), HttpStatus.NO_CONTENT);
    }
}
