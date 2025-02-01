package com.cpa.yusin.quiz.answer.controller;

import com.cpa.yusin.quiz.answer.controller.dto.response.AnswerDTO;
import com.cpa.yusin.quiz.answer.controller.port.AnswerService;
import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.common.controller.dto.response.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class AnswerController
{
    public final AnswerService answerService;

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

}
