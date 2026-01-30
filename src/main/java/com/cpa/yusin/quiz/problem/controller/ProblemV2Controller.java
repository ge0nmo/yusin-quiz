package com.cpa.yusin.quiz.problem.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemV2Response;
import com.cpa.yusin.quiz.problem.controller.port.GetProblemV2Service;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v2/problem")
@RestController
public class ProblemV2Controller
{
    private final GetProblemV2Service getProblemV2Service;

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<ProblemV2Response>> getById(@PathVariable long id)
    {
        ProblemV2Response response = getProblemV2Service.getById(id);

        return ResponseEntity.ok(GlobalResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<List<ProblemV2Response>>> getAllByExamId(@RequestParam("examId") long examId)
    {
        List<ProblemV2Response> response = getProblemV2Service.getAllByExamId(examId);

        return ResponseEntity.ok(GlobalResponse.success(response));
    }

}
