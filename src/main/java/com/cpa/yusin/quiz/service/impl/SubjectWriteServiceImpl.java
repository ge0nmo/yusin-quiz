package com.cpa.yusin.quiz.service.impl;

import com.cpa.yusin.quiz.domain.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.domain.dto.response.SubjectResponse;
import com.cpa.yusin.quiz.domain.entity.Subject;
import com.cpa.yusin.quiz.mapper.SubjectMapper;
import com.cpa.yusin.quiz.repository.SubjectRepository;
import com.cpa.yusin.quiz.service.SubjectWriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
public class SubjectWriteServiceImpl implements SubjectWriteService {
    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;

    @Transactional
    public SubjectResponse create(SubjectCreateRequest request){
        Subject subjectEntity = subjectMapper.toSubjectEntity(request);

        Subject savedSubject = subjectRepository.save(subjectEntity);

        return subjectMapper.toSubjectResponse(savedSubject);
    }
}
