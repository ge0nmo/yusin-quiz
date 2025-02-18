package com.cpa.yusin.quiz.answer.service.port;

import com.cpa.yusin.quiz.answer.domain.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository
{
    Answer save(Answer answer);

    Optional<Answer> findById(Long id);

    Page<Answer> findByQuestionId(long questionId, Pageable pageable);

    List<Answer> findByQuestionId(long questionId);
}
