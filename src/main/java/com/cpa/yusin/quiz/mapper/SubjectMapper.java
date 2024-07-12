package com.cpa.yusin.quiz.mapper;

import com.cpa.yusin.quiz.domain.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.domain.dto.response.SubjectResponse;
import com.cpa.yusin.quiz.domain.entity.Subject;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubjectMapper {

    public Subject toSubjectEntity(SubjectCreateRequest request) {
        if(request == null){
            return null;
        }

        return Subject.builder()
                .name(request.getName())
                .build();
    }

    public SubjectResponse toSubjectResponse(Subject subject) {
        if(subject == null){
            return null;
        }

        return SubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .build();
    }

    public List<SubjectResponse> toSubjectResponseList(List<Subject> subjectList) {
        if(subjectList == null || subjectList.isEmpty()){
            return Collections.emptyList();
        }

        return subjectList.stream()
                .map(this::toSubjectResponse)
                .collect(Collectors.toList());
    }

}
