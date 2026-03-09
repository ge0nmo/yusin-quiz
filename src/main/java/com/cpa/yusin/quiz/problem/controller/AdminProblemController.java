package com.cpa.yusin.quiz.problem.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.problem.controller.port.DeleteProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/problem")
@RestController
public class AdminProblemController
{
    private final DeleteProblemService deleteProblemService;

    @DeleteMapping("/{problemId}")
    public ResponseEntity<Void> delete(@PathVariable("problemId") long problemId)
    {
        deleteProblemService.execute(problemId);

        return ResponseEntity.noContent().build();
    }
}
