package com.cpa.yusin.quiz.answer.controller.port;

import com.cpa.yusin.quiz.answer.controller.dto.request.AdminAnswerRegisterRequest;
import com.cpa.yusin.quiz.answer.controller.dto.request.AdminAnswerUpdateRequest;
import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerRegisterRequest;
import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerUpdateRequest;
import com.cpa.yusin.quiz.answer.controller.dto.response.AnswerDTO;
import com.cpa.yusin.quiz.answer.domain.Answer;
import com.cpa.yusin.quiz.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnswerService {
    long save(AnswerRegisterRequest request, long questionId, Member member);

    long save(AdminAnswerRegisterRequest request, long questionId, Member admin);

    void update(AnswerUpdateRequest request, long answerId, Member member);

    void updateInAdminPage(AdminAnswerUpdateRequest request, long answer);

    Answer findById(long id);

    AnswerDTO getAnswerById(long id);

    Page<AnswerDTO> getAnswersByQuestionId(long questionId, Pageable pageable);

    List<AnswerDTO> getAnswersByQuestionId(long questionId);

    void deleteAnswer(long answerId, Member member);
}
