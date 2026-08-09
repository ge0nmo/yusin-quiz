package com.cpa.yusin.quiz.qualification.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.content.controller.dto.AdminContentDto.*;
import com.cpa.yusin.quiz.content.service.AdminContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/qualification-exams")
@RequiredArgsConstructor
public class AdminQualificationExamController {
    private final AdminContentService contentService;

    @GetMapping
    public GlobalResponse<List<QualificationExamResponse>> getAll() {
        return GlobalResponse.success(contentService.getQualificationExams());
    }

    @GetMapping("/{id}")
    public GlobalResponse<QualificationExamResponse> get(@PathVariable Long id) {
        return GlobalResponse.success(contentService.getQualificationExam(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GlobalResponse<QualificationExamResponse> create(@Valid @RequestBody QualificationExamCreateRequest request) {
        return GlobalResponse.success(contentService.createQualificationExam(request));
    }

    @PutMapping("/{id}")
    public GlobalResponse<QualificationExamResponse> update(@PathVariable Long id,
                                                             @Valid @RequestBody QualificationExamUpdateRequest request) {
        return GlobalResponse.success(contentService.updateQualificationExam(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        contentService.deleteQualificationExam(id);
    }
}
