package com.cpa.yusin.quiz.subject.service;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.SubjectException;
import com.cpa.yusin.quiz.subject.controller.port.DeleteSubjectService;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DeleteSubjectServiceImpl implements DeleteSubjectService
{
    private final SubjectRepository subjectRepository;
    private final ClockHolder clockHolder;

    @Transactional
    public void execute(long id)
    {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new SubjectException(ExceptionMessage.SUBJECT_NOT_FOUND));

        subject.delete(clockHolder.getCurrentTime());
        subjectRepository.save(subject);
    }
}
