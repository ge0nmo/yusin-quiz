package com.cpa.yusin.quiz.subject.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.SubjectException;
import com.cpa.yusin.quiz.subject.controller.port.DeleteSubjectService;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DeleteSubjectServiceImpl implements DeleteSubjectService
{
    private final SubjectRepository subjectRepository;

    @Transactional
    public void execute(long id)
    {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new SubjectException(ExceptionMessage.SUBJECT_NOT_FOUND));

        subject.delete();
        subjectRepository.save(subject);
    }
}
