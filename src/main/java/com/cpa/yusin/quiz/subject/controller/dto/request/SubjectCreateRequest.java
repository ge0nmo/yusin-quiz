package com.cpa.yusin.quiz.subject.controller.dto.request;

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
}
