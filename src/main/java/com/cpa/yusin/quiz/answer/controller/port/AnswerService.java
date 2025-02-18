package com.cpa.yusin.quiz.answer.controller.port;

import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerRegisterRequest;
import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerUpdateRequest;
import com.cpa.yusin.quiz.answer.controller.dto.response.AnswerDTO;
import com.cpa.yusin.quiz.answer.domain.Answer;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.web.dto.AdminAnswerRegisterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnswerService
{
    long save(AnswerRegisterRequest request, long questionId);

    long save(AdminAnswerRegisterRequest request, long questionId, Member admin);

    void update(AnswerUpdateRequest request, long questionId);

    Answer findById(long id);

    AnswerDTO getAnswerById(long id);
    Page<AnswerDTO> getAnswersByQuestionId(long questionId, Pageable pageable);
    List<AnswerDTO> getAnswersByQuestionId(long questionId);

    void verifyPassword(long answerId, String password);

    void deleteAnswer(long answerId);
}
