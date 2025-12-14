package com.cpa.yusin.quiz.admin.presentation.question;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.common.controller.dto.response.PageInfo;
import com.cpa.yusin.quiz.question.controller.dto.response.QuestionDTO;
import com.cpa.yusin.quiz.question.controller.port.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/question")
public class AdminQuestionController
{
    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<?> getQuestions(@PageableDefault Pageable pageable)
    {
        Page<QuestionDTO> response = questionService.findAllQuestions(pageable);

        return ResponseEntity.ok(new GlobalResponse<>(response.getContent(), PageInfo.of(response)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestion(@PathVariable long id)
    {
        QuestionDTO response = questionService.getById(id);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable long id)
    {
        questionService.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
