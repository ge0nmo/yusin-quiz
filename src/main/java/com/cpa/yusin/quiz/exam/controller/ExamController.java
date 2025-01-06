package com.cpa.yusin.quiz.exam.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamDeleteRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamCreateResponse;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamDTO;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/exam")
@RestController
public class ExamController
{
    private final ExamService examService;

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<ExamDTO>> getById(@PathVariable("id") long id)
    {
        ExamDTO response = examService.getById(id);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<List<ExamDTO>>> getAllExamBySubjectIdAndYear(@RequestParam(value = "subjectId") long subjectId,
                                                                                      @RequestParam(value = "year", required = false) Integer year)
    {
        List<ExamDTO> response = examService.getAllBySubjectId(subjectId, year);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @GetMapping("/year")
    public ResponseEntity<?> getYear(@RequestParam(value = "subjectId") long subjectId)
    {
        List<Integer> response = examService.getAllYearsBySubjectId(subjectId);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

}
