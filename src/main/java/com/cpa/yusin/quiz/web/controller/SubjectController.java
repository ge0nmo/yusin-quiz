package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
@Controller("webSubjectController")
public class SubjectController
{
    private final SubjectService subjectService;

    @GetMapping("/subject")
    public String subject(Model model)
    {
        model.addAttribute("activePage", "subject");
        return "subject";
    }
}
