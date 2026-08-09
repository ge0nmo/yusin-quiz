package com.cpa.yusin.quiz.problem.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.content.controller.dto.AdminContentDto.*;
import com.cpa.yusin.quiz.content.service.AdminContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/problems")
@RequiredArgsConstructor
public class AdminProblemController {
    private final AdminContentService contentService;

    @GetMapping
    public GlobalResponse<List<ProblemSummaryResponse>> getAll(
            @RequestParam(required = false) Long qualificationExamId,
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Long subjectId) {
        return GlobalResponse.success(contentService.getProblems(qualificationExamId, examId, subjectId));
    }

    @GetMapping("/{id}")
    public GlobalResponse<ProblemDetailResponse> get(@PathVariable Long id) {
        return GlobalResponse.success(contentService.getProblem(id));
    }

    @GetMapping("/next-number")
    public GlobalResponse<NextProblemNumberResponse> getNextNumber(@RequestParam Long examId,
                                                                   @RequestParam Long subjectId) {
        return GlobalResponse.success(contentService.getNextProblemNumber(examId, subjectId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GlobalResponse<ProblemDetailResponse> create(@Valid @RequestBody ProblemRequest request) {
        return GlobalResponse.success(contentService.createProblem(request));
    }

    @PutMapping("/{id}")
    public GlobalResponse<ProblemDetailResponse> update(@PathVariable Long id,
                                                         @Valid @RequestBody ProblemRequest request) {
        return GlobalResponse.success(contentService.updateProblem(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        contentService.deleteProblem(id);
    }
}
