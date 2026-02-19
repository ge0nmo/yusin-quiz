package com.cpa.yusin.quiz.question.service;

import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.MemberException;
import com.cpa.yusin.quiz.global.exception.QuestionException;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.question.controller.port.DeleteQuestionService;
import com.cpa.yusin.quiz.question.domain.Question;
import com.cpa.yusin.quiz.question.service.port.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DeleteQuestionServiceImpl implements DeleteQuestionService {
    private final QuestionRepository questionRepository;

    @Transactional
    @Override
    public void execute(long id, Member member) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new QuestionException(ExceptionMessage.QUESTION_NOT_FOUND));

        if (!question.isOwner(member)) {
            throw new MemberException(ExceptionMessage.NO_AUTHORIZATION);
        }

        question.delete();
        questionRepository.save(question);
    }
}
