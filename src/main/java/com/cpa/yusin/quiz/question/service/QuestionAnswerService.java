package com.cpa.yusin.quiz.question.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.QuestionException;
import com.cpa.yusin.quiz.question.domain.Question;
import com.cpa.yusin.quiz.question.service.port.QuestionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionAnswerService
{
    private final QuestionRepository questionRepository;

    @Transactional
    public void answerQuestion(long questionId)
    {
        Question question = getQuestion(questionId);
        question.answerByAdmin();

    }


    public Question getQuestion(long questionId)
    {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionException(ExceptionMessage.QUESTION_NOT_FOUND));
    }
}
