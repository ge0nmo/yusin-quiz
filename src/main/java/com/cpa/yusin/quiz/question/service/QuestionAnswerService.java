package com.cpa.yusin.quiz.question.service;

import com.cpa.yusin.quiz.question.controller.port.QuestionService;
import com.cpa.yusin.quiz.question.domain.Question;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionAnswerService
{
    private final QuestionService questionService;

    @Transactional
    public void answerQuestion(long questionId)
    {
        Question question = questionService.findById(questionId);
        question.answerByAdmin();

        log.info("Question answered: {}", question.isAnsweredByAdmin());
    }

    public Question getQuestion(long questionId)
    {
        return questionService.findById(questionId);
    }
}
