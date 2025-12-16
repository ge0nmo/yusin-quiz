package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamDTO;
import com.cpa.yusin.quiz.exam.controller.port.DeleteExamService;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import jakarta.validation.constraints.Positive;
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
@Controller("webExamController")
public class ExamController
{
    private final ExamService examService;
    private final DeleteExamService deleteExamService;

    // View 반환
    @GetMapping("/exam")
    public String exam(Model model)
    {
        model.addAttribute("activePage", "exam");
        return "exam";
    }

    // 1. 과목별 시험 목록 조회 (Year는 선택값, null이면 전체)
    @ResponseBody
    @GetMapping("/subject/{subjectId}/exam")
    public List<ExamDTO> getExamList(@PathVariable("subjectId") long subjectId,
                                     @RequestParam(value = "year", required = false) Integer year)
    {
        return examService.getAllBySubjectId(subjectId, year);
    }

    // 2. 시험 저장
    @ResponseBody
    @PostMapping("/exam")
    public long save(@Positive @RequestParam(value = "subjectId") long subjectId,
                     @Validated @RequestBody ExamCreateRequest request)
    {
        ExamCreateResponse response = examService.save(subjectId, request);
        return response.getId();
    }

    // 3. 시험 수정
    @ResponseBody
    @PatchMapping("/exam/{id}")
    public ExamDTO update(@Positive @PathVariable("id") long examId, @RequestBody ExamUpdateRequest request)
    {
        examService.update(examId, request);
        return examService.getById(examId);
    }

    // 4. 시험 삭제
    @ResponseBody
    @DeleteMapping("/exam/{examId}")
    public void deleteExam(@PathVariable("examId") long examId)
    {
        deleteExamService.execute(examId);
    }

    // [추가됨] 5. 연도 목록 조회 API
    // exam.js의 loadYears()가 호출하는 주소입니다.
    @ResponseBody
    @GetMapping("/exam/year")
    public List<Integer> getYear(@RequestParam(value = "subjectId") long subjectId)
    {
        return examService.getAllYearsBySubjectId(subjectId);
    }
}