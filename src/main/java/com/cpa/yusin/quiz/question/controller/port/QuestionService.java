package com.cpa.yusin.quiz.question.controller.port;

import com.cpa.yusin.quiz.question.controller.dto.request.QuestionRegisterRequest;
import com.cpa.yusin.quiz.question.controller.dto.request.QuestionUpdateRequest;
import com.cpa.yusin.quiz.question.controller.dto.response.QuestionDTO;
import com.cpa.yusin.quiz.question.domain.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionService
{
    long save(QuestionRegisterRequest request, long problemId);

    void update(QuestionUpdateRequest request, long questionId);

    Question findById(long id);

    QuestionDTO getById(long id);

    Page<QuestionDTO> getAllByProblemId(Pageable pageable, long problemId);
}
