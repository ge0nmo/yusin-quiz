package com.cpa.yusin.quiz.question.service.port;

import com.cpa.yusin.quiz.question.domain.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface QuestionRepository
{
    Question save(Question question);

    Optional<Question> findById(long id);

    Page<Question> findAllQuestions(Pageable pageable);

    Page<Question> findAllByProblemId(long problemId, Pageable pageable);
}
