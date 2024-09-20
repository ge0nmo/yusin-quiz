package com.cpa.yusin.quiz.exam.domain;

import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ExamDomain
{
    private Long id;
    private String name;
    private int year;
    private int maxProblemCount;
    private Long subjectId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ExamDomain from(ExamCreateRequest request, SubjectDomain subjectDomain)
    {
        return ExamDomain.builder()
                .name(request.getName())
                .year(request.getYear())
                .maxProblemCount(request.getMaxProblemCount())
                .subjectId(subjectDomain.getId())
                .build();
    }

    public void update(ExamUpdateRequest request)
    {
        this.name = request.getName();
        this.year = request.getYear();
        this.maxProblemCount = request.getMaxProblemCount();
    }
}
