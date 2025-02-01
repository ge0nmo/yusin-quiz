package com.cpa.yusin.quiz.answer.controller.port;

import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerRegisterRequest;
import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerUpdateRequest;
import com.cpa.yusin.quiz.answer.controller.dto.response.AnswerDTO;
import com.cpa.yusin.quiz.answer.domain.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AnswerService
{
    long save(AnswerRegisterRequest request, long questionId);

    void update(AnswerUpdateRequest request, long questionId);

    Answer findById(long id);

    AnswerDTO getAnswerById(long id);
    Page<AnswerDTO> getAnswersByQuestionId(long questionId, Pageable pageable);
}
