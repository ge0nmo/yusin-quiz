package com.cpa.yusin.quiz.subject.service;

import com.cpa.yusin.quiz.common.service.CascadeDeleteService;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectUpdateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectCreateResponse;
import com.cpa.yusin.quiz.subject.controller.dto.response.SubjectDTO;
import com.cpa.yusin.quiz.subject.controller.mapper.SubjectMapper;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import com.cpa.yusin.quiz.subject.service.port.SubjectValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SubjectServiceImpl implements SubjectService
{
    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;
    private final SubjectValidator subjectValidator;
    private final CascadeDeleteService cascadeDeleteService;

    @Transactional
    @Override
    public SubjectCreateResponse save(SubjectCreateRequest request)
    {
        subjectValidator.validateName(request.getName());
        Subject subject = subjectMapper.toSubjectEntity(request);

        subject = subjectRepository.save(subject);

        return subjectMapper.toSubjectCreateResponse(subject);
    }

    @Transactional
    @Override
    public void update(long id, SubjectUpdateRequest request)
    {
        Subject subject = findById(id);
        subjectValidator.validateName(subject.getId(), request.getName());

        subject.update(request);
        subjectRepository.save(subject);
    }

    @Override
    public Subject findById(long id)
    {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new GlobalException(ExceptionMessage.SUBJECT_NOT_FOUND));
    }


    @Override
    public SubjectDTO getById(long id)
    {
        return subjectMapper.toSubjectDTO(findById(id));
    }

    @Override
    public List<SubjectDTO> getAll()
    {
        List<SubjectDTO> response = subjectMapper.toSubjectDTOs(subjectRepository.findAll());

        return response.stream()
                .sorted(Comparator.comparing(SubjectDTO::getName))
                .toList();
    }

    @Override
    @Transactional
    public boolean deleteById(long id)
    {
        findById(id);
        cascadeDeleteService.deleteSubjectBySubjectId(id);

        return !subjectRepository.existsById(id);
    }
}
