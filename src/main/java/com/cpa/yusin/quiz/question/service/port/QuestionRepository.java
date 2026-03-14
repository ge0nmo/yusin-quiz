package com.cpa.yusin.quiz.question.service.port;

import com.cpa.yusin.quiz.question.domain.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface QuestionRepository
{
    Question save(Question question);

    Optional<Question> findById(long id);

    default Page<Question> findAllQuestions(Pageable pageable) {
        return findAllQuestions(pageable, null, null);
    }

    Page<Question> findAllQuestions(Pageable pageable, Boolean answeredByAdmin, String keyword);

    default Page<Question> findAllQuestions(Pageable pageable,
                                            Boolean answeredByAdmin,
                                            String keyword,
                                            LocalDateTime createdAtFrom,
                                            LocalDateTime createdAtTo) {
        return findAllQuestions(pageable, answeredByAdmin, keyword);
    }

    Page<Question> findAllByProblemId(long problemId, Pageable pageable);

    void deleteById(long id);
}
