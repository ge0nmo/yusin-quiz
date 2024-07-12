package com.cpa.yusin.quiz.service;


import com.cpa.yusin.quiz.domain.dto.response.SubjectResponse;
import com.cpa.yusin.quiz.domain.entity.Subject;
import org.springframework.data.domain.Page;

public interface SubjectReadService {
    Subject findBySubjectId(Long subjectId);

    SubjectResponse getBySubjectId(Long subjectId);

    Page<SubjectResponse> getBySubjects();
}
