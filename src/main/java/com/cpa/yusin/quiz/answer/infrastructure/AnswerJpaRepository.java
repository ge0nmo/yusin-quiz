package com.cpa.yusin.quiz.answer.infrastructure;

import com.cpa.yusin.quiz.answer.domain.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerJpaRepository extends JpaRepository<Answer, Long>
{
    @Query("SELECT a FROM Answer a " +
            "WHERE a.question.id = :questionId " +
            "ORDER BY a.createdAt ASC ")
    Page<Answer> findByQuestionId(@Param("questionId") long questionId, Pageable pageable);
}
