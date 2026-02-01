package com.cpa.yusin.quiz.problem.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.port.DeleteProblemService;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/problem")
@RestController
public class AdminProblemController
{
    private final ProblemService problemService;
    private final DeleteProblemService deleteProblemService;

    @PostMapping
    public void save(@RequestParam("examId") long examId, @Validated @RequestBody ProblemCreateRequest request)
    {
        problemService.save(examId, request);
    }

    @PatchMapping
    public void update(@Validated @RequestBody ProblemRequest request,
                       @RequestParam("examId") long examId)
    {
        problemService.processSaveOrUpdate(request, examId);
    }

    @DeleteMapping("/{problemId}")
    public ResponseEntity<Void> delete(@PathVariable("problemId") long problemId)
    {
        deleteProblemService.execute(problemId);

        return ResponseEntity.noContent().build();
    }
}
