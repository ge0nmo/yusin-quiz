package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.common.controller.dto.request.DataTableRequest;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/subject")
@Controller("webSubjectController")
public class SubjectController
{
    private final SubjectService subjectService;

    @GetMapping
    public String subject(Model model, @ModelAttribute("params")DataTableRequest request)
    {
        model.addAttribute("activePage", "subject");

        Page<SubjectDTO> response = subjectService.getAll(PageRequest.of(request.getPage(), request.getSize()));
        model.addAttribute("response", response);

        return "subject";
    }

    @ResponseBody
    @DeleteMapping("/{id}")
    public void deleteSubject(@PathVariable("id") long id)
    {
        subjectService.deleteById(id);
    }
}
