package com.cpa.yusin.quiz.problem.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/problem")
@RestController
public class AdminProblemController
{
    private final ProblemService problemService;

    @PostMapping
    public ResponseEntity<GlobalResponse<List<ProblemResponse>>> saveOrUpdate(@RequestParam(value = "examId") long examId,
                                                                              @RequestBody List<ProblemRequest> requests)
    {
        problemService.saveOrUpdateProblem(examId, requests);
        List<ProblemResponse> response = problemService.getAllByExamId(examId);

        return ResponseEntity
                .ok(new GlobalResponse<>(response));
    }


    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<ProblemDTO>> getById(@PathVariable long id)
    {
        ProblemDTO response = problemService.getById(id);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<List<ProblemResponse>>> getAllByExamId(@RequestParam long examId)
    {
        List<ProblemResponse> response = problemService.getAllByExamId(examId);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

}
