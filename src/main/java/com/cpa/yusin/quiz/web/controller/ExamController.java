package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamDTO;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import com.cpa.yusin.quiz.subject.domain.Subject;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
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
    public String exam()
    {
        return "exam";
    }

    @ResponseBody
    @GetMapping("/subject/{subjectId}/exam")
    public List<ExamDTO> getExamList(@PathVariable("subjectId") long subjectId, @RequestParam("year") int year)
    {
        log.info("subject id = {}", subjectId);
        List<ExamDTO> response = examService.getAllBySubjectId(subjectId, year);
        log.info("response = {}", response);
        return response;
    }

    @ResponseBody
    @PostMapping("/exam")
    public long save(@Positive @RequestParam(value = "subjectId") long subjectId,
                     @Validated @RequestBody ExamCreateRequest request)
    {
        ExamCreateResponse response = examService.save(subjectId, request);

        return response.getId();
    }

    @ResponseBody
    @PatchMapping("/exam/{id}")
    public ExamDTO update(@Positive @PathVariable("id") long examId, @RequestBody ExamUpdateRequest request)
    {
        examService.update(examId, request);

        return examService.getById(examId);
    }

    @ResponseBody
    @DeleteMapping("/exam/{examId}")
    public void deleteExam(@PathVariable("examId") long examId)
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
