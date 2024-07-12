package com.cpa.yusin.quiz.service.impl;

import com.cpa.yusin.quiz.domain.dto.response.SubjectResponse;
import com.cpa.yusin.quiz.domain.entity.Subject;
import com.cpa.yusin.quiz.service.SubjectReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
@Service
public class SubjectReadServiceImpl implements SubjectReadService {

    @Override
    public Subject findBySubjectId(Long subjectId) {
        return null;
    }

    @Override
    public SubjectResponse getBySubjectId(Long subjectId) {
        return null;
    }

    @Override
    public Page<SubjectResponse> getBySubjects() {
        return null;
    }
}
