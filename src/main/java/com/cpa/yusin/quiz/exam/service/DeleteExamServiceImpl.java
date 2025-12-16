package com.cpa.yusin.quiz.exam.service;

import com.cpa.yusin.quiz.exam.controller.port.DeleteExamService;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.global.exception.ExamException;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DeleteExamServiceImpl implements DeleteExamService
{
    private final ExamRepository examRepository;

    @Transactional
    @Override
    public void execute(long id)
    {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ExamException(ExceptionMessage.EXAM_NOT_FOUND));

        exam.delete();

        examRepository.save(exam);
    }
}
