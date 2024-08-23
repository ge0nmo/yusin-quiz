package com.cpa.yusin.quiz.subject.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectUpdateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectCreateResponse;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/subject")
@RestController
public class SubjectController
{
    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<GlobalResponse<SubjectCreateResponse>> save(@RequestBody SubjectCreateRequest request)
    {
        SubjectCreateResponse response = subjectService.save(request);

        return ResponseEntity
                .ok(new GlobalResponse<>(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GlobalResponse<SubjectDTO>> update(@Positive @PathVariable("id") long id,
                                                             @RequestBody SubjectUpdateRequest request)
    {
        subjectService.update(id, request);
        SubjectDTO response = subjectService.getById(id);

        return ResponseEntity
                .ok(new GlobalResponse<>(response));
    }


    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<SubjectDTO>> getById(@Positive @PathVariable("id") long id)
    {
        return ResponseEntity
                .ok(new GlobalResponse<>(subjectService.getById(id)));
    }


    @GetMapping
    public ResponseEntity<GlobalResponse<List<SubjectDTO>>> getAll()
    {
        return ResponseEntity
                .ok(new GlobalResponse<>(subjectService.getAll()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GlobalResponse<String>> deleteById(@Positive @PathVariable("id") long id)
    {
        boolean result = subjectService.deleteById(id);
        String response;
        if(result)
            response = "삭제가 완료 되었습니다.";
        else
            response = "삭제에 실패했습니다.";

        return ResponseEntity
                .ok(new GlobalResponse<>(response));
    }

}
