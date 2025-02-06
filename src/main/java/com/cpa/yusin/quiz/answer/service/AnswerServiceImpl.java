package com.cpa.yusin.quiz.answer.service;

import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerRegisterRequest;
import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerUpdateRequest;
import com.cpa.yusin.quiz.answer.controller.dto.response.AnswerDTO;
import com.cpa.yusin.quiz.answer.controller.mapper.AnswerMapper;
import com.cpa.yusin.quiz.answer.controller.port.AnswerService;
import com.cpa.yusin.quiz.answer.domain.Answer;
import com.cpa.yusin.quiz.answer.service.port.AnswerRepository;
import com.cpa.yusin.quiz.global.exception.AnswerException;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class AnswerServiceImpl implements AnswerService
{
    private final AnswerRepository answerRepository;
    private final AnswerMapper answerMapper;

    @Transactional
    @Override
    public long save(AnswerRegisterRequest request, long questionId)
    {
        return 0;
    }

    @Transactional
    @Override
    public void update(AnswerUpdateRequest request, long questionId)
    {
    }

    @Override
    public Answer findById(long id)
    {
        return answerRepository.findById(id)
                .orElseThrow(() -> new AnswerException(ExceptionMessage.ANSWER_NOT_FOUND));
    }

    @Override
    public AnswerDTO getAnswerById(long id)
    {
        Answer answer = findById(id);
        return answerMapper.toAnswerDTO(answer);
    }

    @Override
    public Page<AnswerDTO> getAnswersByQuestionId(long questionId, Pageable pageable)
    {
        return answerRepository.findByQuestionId(questionId, pageable)
                .map(answerMapper::toAnswerDTO);
    }

    @Override
    public void verifyPassword(long answerId, String password)
    {
        Answer answer = findById(answerId);
        answer.verifyPassword(password);
    }
}
