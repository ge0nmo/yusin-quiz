package com.cpa.yusin.quiz.subject.infrastructure;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.GlobalException;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import com.cpa.yusin.quiz.subject.service.port.SubjectValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SubjectValidatorImpl implements SubjectValidator
{
    private final SubjectRepository subjectRepository;

    @Override
    public void validateName(String name)
    {
        if(subjectRepository.existsByName(name))
            throw new GlobalException(ExceptionMessage.SUBJECT_NAME_EXISTS);
    }

    @Override
    public void validateName(long id, String name)
    {
        if(subjectRepository.existsByNameAndIdNot(id, name))
            throw new GlobalException(ExceptionMessage.SUBJECT_NAME_EXISTS);
    }
}
