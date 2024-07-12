package com.cpa.yusin.quiz.service;

import com.cpa.yusin.quiz.domain.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.domain.dto.response.SubjectResponse;

public interface SubjectWriteService {
    SubjectResponse create(SubjectCreateRequest request);
}
