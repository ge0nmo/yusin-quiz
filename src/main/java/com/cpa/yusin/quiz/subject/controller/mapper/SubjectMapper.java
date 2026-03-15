package com.cpa.yusin.quiz.subject.controller.mapper;

import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectCreateResponse;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.domain.SubjectStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SubjectMapper
{
    public Subject toSubjectEntity(SubjectCreateRequest request)
    {
        return Subject.builder()
                .name(request.getName())
                .status(request.getStatus() == null ? SubjectStatus.PUBLISHED : request.getStatus())
                .build();
    }

    public SubjectCreateResponse toSubjectCreateResponse(Subject subject)
    {
        if(subject == null)
            return null;

        return SubjectCreateResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .build();
    }

    public SubjectDTO toSubjectDTO(Subject subject)
    {
        if(subject == null)
            return null;

        return SubjectDTO.builder()
                .id(subject.getId())
                .name(subject.getName())
                .status(subject.getStatus())
                .build();
    }

    public List<SubjectDTO> toSubjectDTOs(List<Subject> subjects)
    {
        if(subjects == null || subjects.isEmpty())
            return Collections.emptyList();

        return subjects.stream()
                .map(this::toSubjectDTO)
                .toList();
    }
}
