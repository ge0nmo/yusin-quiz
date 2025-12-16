package com.cpa.yusin.quiz.exam.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.response.ExamDTO;
import com.cpa.yusin.quiz.exam.controller.port.DeleteExamService;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin")
@RestController
public class AdminExamController
{
    private final ExamService examService;
    private final DeleteExamService deleteExamService;

    @GetMapping("/subject/{subjectId}/exam")
    public ResponseEntity<GlobalResponse<List<ExamDTO>>> getExamList(@PathVariable("subjectId") long subjectId,
                                                                    @RequestParam(value = "year", required = false) Integer year)
    {
        List<ExamDTO> response = examService.getAllBySubjectId(subjectId, year);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @PostMapping("/exam")
    public ResponseEntity<GlobalResponse<Long>> save(@Positive @RequestParam(value = "subjectId") long subjectId,
                     @Validated @RequestBody ExamCreateRequest request)
    {
        long response = examService.saveAsAdmin(subjectId, request);
        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    // 3. 시험 수정
    @PatchMapping("/exam/{id}")
    public ResponseEntity<GlobalResponse<Void>> update(@Positive @PathVariable("id") long examId, @RequestBody ExamUpdateRequest request)
    {
        examService.update(examId, request);
        return ResponseEntity.ok(new GlobalResponse<>(null));
    }

    // 4. 시험 삭제
    @DeleteMapping("/exam/{examId}")
    public ResponseEntity<?> deleteExam(@PathVariable("examId") long examId)
    {
        deleteExamService.execute(examId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exam/year")
    public ResponseEntity<GlobalResponse<List<Integer>>> getYear(@RequestParam(value = "subjectId") long subjectId)
    {
        List<Integer> response =  examService.getAllYearsBySubjectId(subjectId);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }
}