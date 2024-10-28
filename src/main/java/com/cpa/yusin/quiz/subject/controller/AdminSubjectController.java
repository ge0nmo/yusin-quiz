package com.cpa.yusin.quiz.subject.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectUpdateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectCreateResponse;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/subject")
@RestController
public class AdminSubjectController
{
    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<GlobalResponse<SubjectCreateResponse>> save(@Valid @RequestBody SubjectCreateRequest request)
    {
        SubjectCreateResponse response = subjectService.save(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GlobalResponse<>(response));
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


    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteById(@Positive @PathVariable("id") long id)
    {
        boolean result = subjectService.deleteById(id);

        if(result)
           return ResponseEntity.status(HttpStatus.NO_CONTENT)
                   .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GlobalResponse<>("잠시 후 다시 시도해주세요."));
    }

}
