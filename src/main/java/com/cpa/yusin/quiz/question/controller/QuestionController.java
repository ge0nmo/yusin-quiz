package com.cpa.yusin.quiz.question.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.common.controller.dto.response.PageInfo;
import com.cpa.yusin.quiz.question.controller.dto.request.QuestionRegisterRequest;
import com.cpa.yusin.quiz.question.controller.dto.request.QuestionUpdateRequest;
import com.cpa.yusin.quiz.question.controller.dto.response.QuestionDTO;
import com.cpa.yusin.quiz.question.controller.port.DeleteQuestionService;
import com.cpa.yusin.quiz.question.controller.port.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class QuestionController
{
    private final QuestionService questionService;
    private final DeleteQuestionService deleteQuestionService;

    @PostMapping("/problem/{problemId}/question")
    public ResponseEntity<GlobalResponse<Long>> saveQuestion(@PathVariable("problemId") long problemId,
                                                             @Valid @RequestBody QuestionRegisterRequest request)
    {
        long response = questionService.save(request, problemId);

        return new ResponseEntity<>(new GlobalResponse<>(response), HttpStatus.CREATED);
    }

    @PatchMapping("/question/{questionId}")
    public ResponseEntity<GlobalResponse<QuestionDTO>> updateQuestion(@PathVariable("questionId") long questionId,
                                                                      @Valid @RequestBody QuestionUpdateRequest request)
    {
        questionService.update(request, questionId);
        QuestionDTO response = questionService.getById(questionId);

        return new ResponseEntity<>(new GlobalResponse<>(response), HttpStatus.OK);
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<GlobalResponse<QuestionDTO>> getQuestion(@PathVariable("questionId") long questionId)
    {
        QuestionDTO response = questionService.getById(questionId);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @GetMapping("/problem/{problemId}/question")
    public ResponseEntity<GlobalResponse<List<QuestionDTO>>> getAllByProblemId(@PathVariable("problemId") long problemId,
                                                                               @PageableDefault Pageable pageable)
    {
        Page<QuestionDTO> response = questionService.getAllByProblemId(pageable.previousOrFirst(), problemId);

        return ResponseEntity.ok(new GlobalResponse<>(response.getContent(), PageInfo.of(response)));
    }

    @GetMapping("/question/{questionId}/verification")
    public ResponseEntity<?> verifyQuestionPassword(@PathVariable("questionId") long questionId,
                                                    @RequestParam("password") String password)
    {
        boolean response = questionService.verifyPassword(questionId, password);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @DeleteMapping("/question/{questionId}")
    public ResponseEntity<?> deleteById(@PathVariable("questionId") long questionId)
    {
        deleteQuestionService.execute(questionId);

        return new ResponseEntity<>(new GlobalResponse<>(true), HttpStatus.NO_CONTENT);
    }
}
