package com.cpa.yusin.quiz.subject.controller.dto.request;

import com.cpa.yusin.quiz.subject.domain.SubjectStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SubjectCreateRequest
{
    @NotBlank
    private String name;

    private SubjectStatus status;
}
