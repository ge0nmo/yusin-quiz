package com.cpa.yusin.quiz.question.infrastructure;

import com.cpa.yusin.quiz.question.domain.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuestionJpaRepository extends JpaRepository<Question, Long>
{
    @Query("SELECT q FROM Question q " +
            "WHERE q.problem.id = :problemId " +
            "AND q.isRemoved = false " +
            "ORDER BY q.createdAt DESC ")
    Page<Question> findAllByProblemId(@Param("problemId") long problemId,
                                      Pageable pageable);

    @Query("SELECT q FROM Question q " +
            "WHERE q.isRemoved = false " +
            "ORDER BY q.createdAt DESC ")
    Page<Question> findAllQuestions(Pageable pageable);


    @Query("SELECT q FROM Question q WHERE q.id = :id AND q.isRemoved = false ")
    Optional<Question> findByIdAndIsRemovedFalse(@Param("id") long id);
}
