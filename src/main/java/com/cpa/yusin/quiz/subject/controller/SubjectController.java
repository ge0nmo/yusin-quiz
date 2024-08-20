package com.cpa.yusin.quiz.subject.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectCreateResponse;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<SubjectDTO>> findById(@Positive @PathVariable("id") long id)
    {
        return ResponseEntity
                .ok(new GlobalResponse<>(subjectService.getById(id)));
    }

}
