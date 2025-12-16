package com.cpa.yusin.quiz.web.controller;


import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.global.utils.DateUtils;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemCreateRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemDTO;
import com.cpa.yusin.quiz.problem.controller.port.DeleteProblemService;
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
    private final DeleteProblemService deleteProblemService;
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
    public List<ProblemDTO> get(@RequestParam("examId") long examId)
    {
        GlobalResponse<List<ProblemDTO>> response = problemService.getAllByExamId(examId);
        return response.getData();
    }

    @ResponseBody
    @PostMapping("/problem")
    public void save(@RequestParam("examId") long examId, @Validated @RequestBody ProblemCreateRequest request)
    {
        problemService.save(examId, request);
    }

    @ResponseBody
    @PatchMapping("/problem")
    public void update(@Validated @RequestBody ProblemRequest request,
                       @RequestParam("examId") long examId)
    {
        problemService.processSaveOrUpdate(request, examId);
    }

    @ResponseBody
    @DeleteMapping("/problem/{problemId}")
    public void delete(@PathVariable("problemId") long problemId)
    {
        deleteProblemService.execute(problemId);
    }
}
