package com.cpa.yusin.quiz.subject.controller.dto.request;

import com.cpa.yusin.quiz.subject.domain.SubjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectUpdateRequest
{
    private String name;
    private SubjectStatus status;
}
