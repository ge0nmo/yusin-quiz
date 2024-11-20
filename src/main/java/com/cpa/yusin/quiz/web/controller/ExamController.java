package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.common.controller.dto.request.DataTableRequest;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import com.cpa.yusin.quiz.subject.domain.Subject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
@Controller("webExamController")
public class ExamController
{
    private final SubjectService subjectService;
    private final ExamService examService;

    @GetMapping("/exam")
    public String member(Model model, @ModelAttribute("params") DataTableRequest request)
    {


        return "exam";
    }

    @ResponseBody
    @GetMapping("/subject/dropdown")
    public List<Subject> getSubjectList(@RequestParam("name") String name)
    {
        return subjectService.findAllByName(name);
    }
}
