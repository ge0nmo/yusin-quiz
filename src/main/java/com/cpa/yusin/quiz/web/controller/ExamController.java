package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import com.cpa.yusin.quiz.subject.domain.Subject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
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
    public String member()
    {
        return "exam";
    }

    @ResponseBody
    @GetMapping("/subject/{subjectId}/exam")
    public List<Exam> getExamList(@PathVariable("subjectId") long subjectId)
    {
        log.info("subject id = {}", subjectId);
        List<Exam> response = examService.getAllBySubjectId(subjectId);
        log.info("response = {}", response);
        return response;
    }

    @ResponseBody
    @DeleteMapping("/exam/{id}")
    public void deleteExam(@PathVariable("id") long examId)
    {
        examService.deleteById(examId);
    }


    @ResponseBody
    @GetMapping("/subject/dropdown")
    public List<Subject> getSubjectList(@RequestParam("name") String name)
    {
        return subjectService.findAllByName(name);
    }
}
