package com.cpa.yusin.quiz.question.service;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.MemberException;
import com.cpa.yusin.quiz.global.exception.QuestionException;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.question.controller.dto.request.QuestionRegisterRequest;
import com.cpa.yusin.quiz.question.controller.dto.request.QuestionUpdateRequest;
import com.cpa.yusin.quiz.question.controller.dto.response.QuestionDTO;
import com.cpa.yusin.quiz.question.controller.mapper.QuestionMapper;
import com.cpa.yusin.quiz.question.controller.port.QuestionService;
import com.cpa.yusin.quiz.question.domain.Question;
import com.cpa.yusin.quiz.question.service.dto.AdminQuestionSearchCondition;
import com.cpa.yusin.quiz.question.service.port.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;
    private final ProblemService problemService;
    private final QuestionMapper questionMapper;
    private final ClockHolder clockHolder;

    @Transactional
    @Override
    public long save(QuestionRegisterRequest request, long problemId, Member member) {
        Problem problem = problemService.findById(problemId);

        Question question = questionMapper.toQuestionEntity(request, problem, member);

        question = questionRepository.save(question);

        return question.getId();
    }

    @Transactional
    @Override
    public Question save(Question question) {
        return questionRepository.save(question);
    }

    @Transactional
    @Override
    public void update(QuestionUpdateRequest request, long questionId, Member member) {
        Question question = findById(questionId);

        validateOwnership(question, member);

        question.update(request.getTitle(), request.getContent());
    }

    @Override
    public Question findById(long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new QuestionException(ExceptionMessage.QUESTION_NOT_FOUND));
    }

    @Override
    public QuestionDTO getById(long id) {
        Question question = findById(id);
        return questionMapper.toQuestionDTO(question);
    }

    @Override
    public Page<QuestionDTO> findAllQuestions(Pageable pageable, AdminQuestionSearchCondition searchCondition) {
        AdminQuestionSearchCondition normalizedCondition = searchCondition == null
                ? AdminQuestionSearchCondition.all()
                : AdminQuestionSearchCondition.of(
                        searchCondition.status(),
                        searchCondition.keyword(),
                        searchCondition.datePreset()
                );

        LocalDateTime createdAtFrom = null;
        LocalDateTime createdAtTo = null;
        if (normalizedCondition.requiresTodayBoundary()) {
            LocalDate today = clockHolder.getCurrentDateTime().toLocalDate();
            createdAtFrom = today.atStartOfDay();
            createdAtTo = createdAtFrom.plusDays(1);
        }

        return questionRepository.findAllQuestions(
                        pageable,
                        normalizedCondition.answeredByAdminFilter(),
                        normalizedCondition.keyword(),
                        createdAtFrom,
                        createdAtTo
                )
                .map(questionMapper::toQuestionDTO);
    }

    @Override
    public Page<QuestionDTO> getAllByProblemId(Pageable pageable, long problemId) {
        // The parent problem lookup is intentional: a deleted parent should be a
        // 404, not a silently empty child collection.
        problemService.findById(problemId);

        return questionRepository.findAllByProblemId(problemId, pageable)
                .map(questionMapper::toQuestionDTO);
    }

    private void validateOwnership(Question question, Member member) {
        if (!question.isOwner(member)) {
            throw new MemberException(ExceptionMessage.NO_AUTHORIZATION);
        }
    }
}
