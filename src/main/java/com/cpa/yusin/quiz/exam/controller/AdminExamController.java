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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/exam")
@RestController
public class AdminExamController
{
    private final ExamService examService;

    @PostMapping
    public ResponseEntity<GlobalResponse<ExamCreateResponse>> save(@Positive @RequestParam(value = "subjectId") long subjectId,
                                                                   @Validated @RequestBody ExamCreateRequest request)
    {
        ExamCreateResponse response = examService.save(subjectId, request);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GlobalResponse<ExamDTO>> update(@Positive @PathVariable("id") long id,
                                                          @Validated @RequestBody ExamUpdateRequest request)
    {
        examService.update(id, request);
        ExamDTO response = examService.getById(id);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<ExamDTO>> getById(@PathVariable("id") long id)
    {
        ExamDTO response = examService.getById(id);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<List<ExamDTO>>> getAllExamBySubjectId(@RequestParam(value = "subjectId") long subjectId,
                                                                               @RequestParam(value = "year") int year)
    {
        List<ExamDTO> response = examService.getAllBySubjectId(subjectId, year);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @DeleteMapping
    public ResponseEntity<GlobalResponse<String>> deleteById(@RequestBody ExamDeleteRequest request)
    {
        examService.deleteById(request.getExamIds());

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new GlobalResponse<>("삭제가 완료 되었습니다."));
    }
}
