package com.cpa.yusin.quiz.exam.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.content.controller.dto.AdminContentDto.ExamRequest;
import com.cpa.yusin.quiz.content.controller.dto.AdminContentDto.ExamResponse;
import com.cpa.yusin.quiz.content.service.AdminContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/exams")
@RequiredArgsConstructor
public class AdminExamController {
    private final AdminContentService contentService;

    @GetMapping
    public GlobalResponse<List<ExamResponse>> getAll(
            @RequestParam(required = false) Long qualificationExamId) {
        return GlobalResponse.success(contentService.getExams(qualificationExamId));
    }

    @GetMapping("/{id}")
    public GlobalResponse<ExamResponse> get(@PathVariable Long id) {
        return GlobalResponse.success(contentService.getExam(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GlobalResponse<ExamResponse> create(@Valid @RequestBody ExamRequest request) {
        return GlobalResponse.success(contentService.createExam(request));
    }

    @PutMapping("/{id}")
    public GlobalResponse<ExamResponse> update(@PathVariable Long id, @Valid @RequestBody ExamRequest request) {
        return GlobalResponse.success(contentService.updateExam(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        contentService.deleteExam(id);
    }
}
