package com.cpa.yusin.quiz.subject.controller.mapper;

import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectCreateResponse;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SubjectMapperImpl implements SubjectMapper
{
    @Override
    public SubjectCreateResponse toSubjectCreateResponse(SubjectDomain domain)
    {
        if(domain == null)
            return null;

        return SubjectCreateResponse.builder()
                .id(domain.getId())
                .name(domain.getName())
                .build();
    }

    @Override
    public SubjectDTO toSubjectDTO(SubjectDomain domain)
    {
        if(domain == null)
            return null;

        return SubjectDTO.builder()
                .id(domain.getId())
                .name(domain.getName())
                .build();
    }

    @Override
    public List<SubjectDTO> toSubjectDTOs(List<SubjectDomain> domains)
    {
        if(domains == null || domains.isEmpty())
            return Collections.emptyList();

        return domains.stream()
                .map(this::toSubjectDTO)
                .toList();
    }
}
