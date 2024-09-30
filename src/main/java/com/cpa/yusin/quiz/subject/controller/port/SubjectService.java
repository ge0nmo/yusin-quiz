package com.cpa.yusin.quiz.subject.controller.port;

import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectUpdateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectCreateResponse;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.domain.Subject;

import java.util.List;

public interface SubjectService
{
    SubjectCreateResponse save(SubjectCreateRequest request);

    void update(long id, SubjectUpdateRequest request);

    SubjectDTO getById(long id);

    Subject findById(long id);

    List<SubjectDTO> getAll();

    boolean deleteById(long id);
}
