package com.cpa.yusin.quiz.exam.domain;

import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExamDomain
{
    private Long id;
    private String name;
    private int year;
    private int maxProblemCount;
    private SubjectDomain subjectDomain;

    public static ExamDomain from(ExamCreateRequest request, SubjectDomain subjectDomain)
    {
        return ExamDomain.builder()
                .name(request.getName())
                .year(request.getYear())
                .maxProblemCount(request.getMaxProblemCount())
                .subjectDomain(subjectDomain)
                .build();
    }

    public void update(ExamUpdateRequest request)
    {
        this.name = request.getName();
        this.year = request.getYear();
        this.maxProblemCount = request.getMaxProblemCount();
    }
}
