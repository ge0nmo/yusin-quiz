package com.cpa.yusin.quiz.subject.controller.port;

import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectCreateResponse;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;

import java.util.List;

public interface SubjectService
{
    SubjectCreateResponse save(SubjectCreateRequest request);

    SubjectDTO getById(long id);

    SubjectDomain findById(long id);

    List<SubjectDTO> findAll();

    boolean deleteById(long id);
}
