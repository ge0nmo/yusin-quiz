package com.cpa.yusin.quiz.question.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.common.controller.dto.response.PageInfo;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.question.controller.dto.response.QuestionDTO;
import com.cpa.yusin.quiz.question.controller.port.DeleteQuestionService;
import com.cpa.yusin.quiz.question.controller.port.QuestionService;
import com.cpa.yusin.quiz.question.service.dto.AdminQuestionDatePreset;
import com.cpa.yusin.quiz.question.service.dto.AdminQuestionSearchCondition;
import com.cpa.yusin.quiz.question.service.dto.AdminQuestionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/question")
public class AdminQuestionController {
    private final QuestionService questionService;
    private final DeleteQuestionService deleteQuestionService;

    @GetMapping
    public ResponseEntity<GlobalResponse<List<QuestionDTO>>> getQuestions(
            @PageableDefault(sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(defaultValue = "ALL") AdminQuestionStatus status,
            @RequestParam(defaultValue = "ALL") AdminQuestionDatePreset datePreset,
            @RequestParam(required = false) String keyword) {
        Page<QuestionDTO> response = questionService.findAllQuestions(
                pageable,
                AdminQuestionSearchCondition.of(status, keyword, datePreset)
        );

        return ResponseEntity.ok(new GlobalResponse<>(response.getContent(), PageInfo.of(response)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<QuestionDTO>> getQuestion(@PathVariable long id) {
        QuestionDTO response = questionService.getByIdForAdmin(id);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable long id, Principal principal) {
        MemberDetails memberDetails = (MemberDetails) ((Authentication) principal).getPrincipal();
        deleteQuestionService.execute(id, memberDetails.getMember());

        return ResponseEntity.noContent().build();
    }
}
