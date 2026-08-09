package com.cpa.yusin.quiz.content.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.content.controller.dto.PublicContentDto.*;
import com.cpa.yusin.quiz.content.service.PublicContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/qualification-exams/{code}")
@RequiredArgsConstructor
public class PublicContentController {
    private final PublicContentService contentService;

    @GetMapping("/subjects")
    public GlobalResponse<List<SubjectResponse>> getSubjects(@PathVariable String code) {
        return GlobalResponse.success(contentService.getSubjects(code));
    }

    @GetMapping("/subjects/{subjectId}/problems")
    public GlobalResponse<List<ProblemResponse>> getProblems(@PathVariable String code,
                                                              @PathVariable Long subjectId) {
        return GlobalResponse.success(contentService.getProblems(code, subjectId));
    }

    @PostMapping("/problems/{problemId}/check")
    public GlobalResponse<CheckResponse> check(@PathVariable String code, @PathVariable Long problemId,
                                                @Valid @RequestBody CheckRequest request) {
        return GlobalResponse.success(contentService.check(code, problemId, request));
    }

    @PostMapping("/solutions")
    public GlobalResponse<List<SolutionResponse>> getSolutions(@PathVariable String code,
                                                                @Valid @RequestBody SolutionsRequest request) {
        return GlobalResponse.success(contentService.getSolutions(code, request));
    }
}
