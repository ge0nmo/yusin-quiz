package com.cpa.yusin.quiz.question.infrastructure;

import com.cpa.yusin.quiz.question.domain.Question;
import com.cpa.yusin.quiz.question.service.port.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class QuestionRepositoryImpl implements QuestionRepository
{
    private final QuestionJpaRepository questionJpaRepository;

    @Override
    public Question save(Question question)
    {
        return questionJpaRepository.save(question);
    }


    @Override
    public Optional<Question> findById(long id)
    {
        return questionJpaRepository.findById(id);
    }

    @Override
    public Page<Question> findAllQuestions(Pageable pageable)
    {
        return questionJpaRepository.findAllQuestions(pageable);
    }

    @Override
    public Page<Question> findAllByProblemId(long problemId, Pageable pageable)
    {
        return questionJpaRepository.findAllByProblemId(problemId, pageable);
    }
}
