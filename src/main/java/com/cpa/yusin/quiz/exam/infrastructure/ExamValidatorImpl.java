package com.cpa.yusin.quiz.exam.infrastructure;

import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.exam.service.port.ExamValidator;
import com.cpa.yusin.quiz.global.exception.ExamException;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ExamValidatorImpl implements ExamValidator
{
    private final ExamRepository examRepository;


    @Override
    public void validate(long subjectId, String name, int year)
    {
        if(examRepository.existsBySubjectIdAndNameAndYear(subjectId, name, year)){
            throw new ExamException(ExceptionMessage.EXAM_DUPLICATED);
        }
    }

    @Override
    public void validate(long examId, long subjectId, String name, int year)
    {
        if(examRepository.existsByIdNotAndSubjectIdAndNameAndYear(examId, subjectId, name, year)){
            throw new ExamException(ExceptionMessage.EXAM_DUPLICATED);
        }
    }

}
