package com.cpa.yusin.quiz.problem.controller;

import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class ProblemController
{
    private final ProblemService problemService;

}
