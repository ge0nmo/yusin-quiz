package com.cpa.yusin.quiz.subject.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectCreateResponse;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SubjectServiceImpl implements SubjectService
{
    private final SubjectRepository subjectRepository;

    @Transactional
    @Override
    public SubjectCreateResponse save(SubjectCreateRequest request)
    {
        SubjectDomain domain = subjectRepository.save(SubjectDomain.from(request));

        return SubjectCreateResponse.from(domain);
    }

    @Override
    public SubjectDomain findById(long id)
    {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new GlobalException(ExceptionMessage.SUBJECT_NOT_FOUND));
    }


    @Override
    public SubjectDTO getById(long id)
    {
        return SubjectDTO.from(findById(id));
    }
}
