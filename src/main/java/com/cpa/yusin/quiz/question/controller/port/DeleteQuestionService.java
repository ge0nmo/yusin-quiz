package com.cpa.yusin.quiz.question.controller.port;

import com.cpa.yusin.quiz.member.domain.Member;

public interface DeleteQuestionService {
    void execute(long id, Member member);
}
