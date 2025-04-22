package com.cpa.yusin.quiz.question.service;

import com.cpa.yusin.quiz.answer.service.AnswerChecker;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.QuestionException;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.question.controller.dto.request.QuestionRegisterRequest;
import com.cpa.yusin.quiz.question.controller.dto.request.QuestionUpdateRequest;
import com.cpa.yusin.quiz.question.controller.dto.response.QuestionDTO;
import com.cpa.yusin.quiz.question.controller.mapper.QuestionMapper;
import com.cpa.yusin.quiz.question.controller.port.QuestionService;
import com.cpa.yusin.quiz.question.domain.Question;
import com.cpa.yusin.quiz.question.service.port.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class QuestionServiceImpl implements QuestionService
{
    private final QuestionRepository questionRepository;
    private final ProblemService problemService;
    private final QuestionMapper questionMapper;
    private final AnswerChecker answerChecker;

    @Transactional
    @Override
    public long save(QuestionRegisterRequest request, long problemId)
    {
        Problem problem = problemService.findById(problemId);

        Question question = questionMapper.toQuestionEntity(request, problem);

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
    public void update(QuestionUpdateRequest request, long questionId)
    {
        Question question = findById(questionId);

        question.update(request.getTitle(), request.getContent());
    }

    @Override
    public Question findById(long id)
    {
        return questionRepository.findById(id)
                .orElseThrow(() -> new QuestionException(ExceptionMessage.QUESTION_NOT_FOUND));
    }

    @Override
    public QuestionDTO getById(long id)
    {
        Question question = findById(id);
        return questionMapper.toQuestionDTO(question);
    }

    @Override
    public Page<QuestionDTO> findAllQuestions(Pageable pageable)
    {
        return questionRepository.findAllQuestions(pageable)
                .map(questionMapper::toQuestionDTO);
    }

    @Override
    public Page<QuestionDTO> getAllByProblemId(Pageable pageable, long problemId)
    {
        return questionRepository.findAllByProblemId(problemId, pageable)
                .map(questionMapper::toQuestionDTO);
    }

    @Override
    public boolean verifyPassword(long questionId, String password)
    {
        Question question = findById(questionId);
        return question.verify(password);
    }

    @Transactional
    @Override
    public void deleteById(long questionId) {
        Question question = findById(questionId);
        answerChecker.hasAnswer(question.getId());

        questionRepository.deleteById(questionId);
    }
}
