package com.cpa.yusin.quiz.subject.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/subject")
@RestController
public class SubjectController
{
    private final SubjectService subjectService;

    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<SubjectDTO>> getById(@Positive @PathVariable("id") long id)
    {
        return ResponseEntity
                .ok(new GlobalResponse<>(subjectService.getById(id)));
    }


    @GetMapping
    public ResponseEntity<GlobalResponse<List<SubjectDTO>>> getAll(@PageableDefault Pageable pageable)
    {
        return ResponseEntity
                .ok(subjectService.getAll(pageable.previousOrFirst()));
    }

}
