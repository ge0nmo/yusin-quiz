package com.cpa.yusin.quiz.admin.presentation.subject.controller;

import com.cpa.yusin.quiz.common.controller.dto.request.DataTableRequest;
import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectUpdateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectCreateResponse;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/subject")
@RestController
public class AdminSubjectController
{
    private final SubjectService subjectService;

    @GetMapping
    public List<SubjectDTO> getSubject()
    {
        return subjectService.getAll();
    }


    @PostMapping
    public ResponseEntity<GlobalResponse<Long>> save(@Valid @RequestBody SubjectCreateRequest request)
    {
        log.info("name = {}", request.getName());
        long response = subjectService.saveAsAdmin(request);

        return ResponseEntity.ok(new GlobalResponse<>(response));
    }

    @PatchMapping("/{subjectId}")
    public void update(@Positive @PathVariable("subjectId") long subjectId, @Valid @RequestBody SubjectUpdateRequest request)
    {
        subjectService.update(subjectId, request);
    }

    @ResponseBody
    @DeleteMapping("/{id}")
    public void deleteSubject(@PathVariable("id") long id)
    {
        subjectService.deleteById(id);
    }
}
