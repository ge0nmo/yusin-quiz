package com.cpa.yusin.quiz.web.controller;


import com.cpa.yusin.quiz.global.utils.DateUtils;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemResponse;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
@Controller("webProblemController")
public class ProblemController
{
    private final ProblemService problemService;
    private final DateUtils dateUtils;

    @GetMapping("/problem")
    public String problem(Model model)
    {
        List<Integer> examYearList = dateUtils.getExamYearList();
        model.addAttribute("examYearList", examYearList);

        return "problem";
    }

    @ResponseBody
    @GetMapping("/problem/list")
    public List<ProblemResponse> get(@RequestParam("examId") long examId)
    {
        return problemService.getAllByExamId(examId);
    }

    @ResponseBody
    @PostMapping("/problem")
    public void save(@RequestParam("examId") long examId, @Validated @RequestBody ProblemCreateRequest request)
    {
        problemService.save(examId, request);
    }

    @ResponseBody
    @PatchMapping("/problem/{problemId}")
    public void update(@PathVariable("problemId") long problemId)
    {

    }
}
