package com.cpa.yusin.quiz.question.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.QuestionException;
import com.cpa.yusin.quiz.question.domain.Question;
import com.cpa.yusin.quiz.question.service.port.QuestionRepository;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionAnswerService
{
    private final QuestionRepository questionRepository;
    private final SubjectService subjectService;

    @Transactional
    public void answerQuestionByAdmin(long questionId)
    {
        Question question = getQuestionForAdmin(questionId);
        question.answerByAdmin();
    }

    @Transactional
    public void updateAnswerCount(Question question, int count)
    {
        question.updateAnswerCount(count);
    }

    public Question getQuestion(long questionId)
    {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionException(ExceptionMessage.QUESTION_NOT_FOUND));
        subjectService.findPublishedById(question.getProblem().getExam().getSubjectId());
        return question;
    }

    public Question getQuestionForAdmin(long questionId)
    {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionException(ExceptionMessage.QUESTION_NOT_FOUND));
    }
}
