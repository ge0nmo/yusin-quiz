package com.cpa.yusin.quiz.subject.controller;

import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.content.controller.dto.AdminContentDto.SubjectRequest;
import com.cpa.yusin.quiz.content.controller.dto.AdminContentDto.SubjectResponse;
import com.cpa.yusin.quiz.content.service.AdminContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/subjects")
@RequiredArgsConstructor
public class AdminSubjectController {
    private final AdminContentService contentService;

    @GetMapping
    public GlobalResponse<List<SubjectResponse>> getAll() {
        return GlobalResponse.success(contentService.getSubjects());
    }

    @GetMapping("/{id}")
    public GlobalResponse<SubjectResponse> get(@PathVariable Long id) {
        return GlobalResponse.success(contentService.getSubject(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GlobalResponse<SubjectResponse> create(@Valid @RequestBody SubjectRequest request) {
        return GlobalResponse.success(contentService.createSubject(request));
    }

    @PutMapping("/{id}")
    public GlobalResponse<SubjectResponse> update(@PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        return GlobalResponse.success(contentService.updateSubject(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        contentService.deleteSubject(id);
    }
}
