package com.cpa.yusin.quiz.web.controller;

import com.cpa.yusin.quiz.common.controller.dto.request.DataTableRequest;
import com.cpa.yusin.quiz.common.controller.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.common.controller.dto.response.PageInfo;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/subject")
@Controller("webSubjectController")
public class SubjectController
{
    private final SubjectService subjectService;

    @GetMapping
    public String subject()
    {
        return "subject";
    }

    @ResponseBody
    @GetMapping("/all")
    public ResponseEntity<GlobalResponse<List<SubjectDTO>>> getSubjectList(@PageableDefault Pageable pageable)
    {
        Page<SubjectDTO> subjectList = subjectService.getAll(pageable);
        GlobalResponse<List<SubjectDTO>> response = new GlobalResponse<>(subjectList.getContent(), PageInfo.of(subjectList));
        return ResponseEntity.ok(response);
    }

    @ResponseBody
    @PostMapping
    public SubjectCreateResponse save(@Valid @RequestBody SubjectCreateRequest request)
    {
        log.info("name = {}", request.getName());
        return subjectService.save(request);
    }

    @ResponseBody
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
