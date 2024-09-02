package com.cpa.yusin.quiz.subject.domain;

import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectUpdateRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class SubjectDomain
{
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SubjectDomain from(SubjectCreateRequest request)
    {
        return SubjectDomain.builder()
                .name(request.getName())
                .build();
    }

    public void update(SubjectUpdateRequest request)
    {
        this.name = request.getName();
    }
}
