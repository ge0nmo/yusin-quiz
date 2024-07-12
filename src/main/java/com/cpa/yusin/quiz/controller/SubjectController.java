package com.cpa.yusin.quiz.controller;

import com.cpa.yusin.quiz.domain.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.domain.dto.response.GlobalResponse;
import com.cpa.yusin.quiz.domain.dto.response.SubjectResponse;
import com.cpa.yusin.quiz.service.SubjectWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/subjects")
@RestController
public class SubjectController {
    private final SubjectWriteService subjectWriteService;

    @PostMapping
    public ResponseEntity<GlobalResponse<SubjectResponse>> createSubject(@RequestBody SubjectCreateRequest request){
        SubjectResponse response = subjectWriteService.create(request);

        return new ResponseEntity<>(new GlobalResponse<>(response), HttpStatus.CREATED);
    }
}
