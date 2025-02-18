package com.cpa.yusin.quiz.answer.infrastructure;

import com.cpa.yusin.quiz.answer.domain.Answer;
import com.cpa.yusin.quiz.answer.service.port.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class AnswerRepositoryImpl implements AnswerRepository
{
    private final AnswerJpaRepository answerJpaRepository;

    @Override
    public Answer save(Answer answer)
    {
        return answerJpaRepository.save(answer);
    }

    @Override
    public Optional<Answer> findById(Long id)
    {
        return answerJpaRepository.findById(id);
    }

    @Override
    public Page<Answer> findByQuestionId(long questionId, Pageable pageable)
    {
        return answerJpaRepository.findByQuestionId(questionId, pageable);
    }

    @Override
    public List<Answer> findByQuestionId(long questionId) {
        return answerJpaRepository.findByQuestionId(questionId);
    }

    @Override
    public void deleteById(long id) {
        answerJpaRepository.deleteById(id);
    }
}
