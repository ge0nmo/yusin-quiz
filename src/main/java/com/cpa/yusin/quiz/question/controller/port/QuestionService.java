package com.cpa.yusin.quiz.question.controller.port;

import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.question.controller.dto.request.QuestionRegisterRequest;
import com.cpa.yusin.quiz.question.controller.dto.request.QuestionUpdateRequest;
import com.cpa.yusin.quiz.question.controller.dto.response.QuestionDTO;
import com.cpa.yusin.quiz.question.domain.Question;
import com.cpa.yusin.quiz.question.service.dto.AdminQuestionSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionService {
    long save(QuestionRegisterRequest request, long problemId, Member member);

    Question save(Question question);

    void update(QuestionUpdateRequest request, long questionId, Member member);

    Question findById(long id);

    Question findByIdForAdmin(long id);

    QuestionDTO getById(long id);

    QuestionDTO getByIdForAdmin(long id);

    default Page<QuestionDTO> findAllQuestions(Pageable pageable) {
        return findAllQuestions(pageable, AdminQuestionSearchCondition.all());
    }

    Page<QuestionDTO> findAllQuestions(Pageable pageable, AdminQuestionSearchCondition searchCondition);

    Page<QuestionDTO> getAllByProblemId(Pageable pageable, long problemId);
}
