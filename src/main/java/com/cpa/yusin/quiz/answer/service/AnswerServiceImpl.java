package com.cpa.yusin.quiz.answer.service;

import com.cpa.yusin.quiz.answer.controller.dto.request.AdminAnswerRegisterRequest;
import com.cpa.yusin.quiz.answer.controller.dto.request.AdminAnswerUpdateRequest;
import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerRegisterRequest;
import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerUpdateRequest;
import com.cpa.yusin.quiz.answer.controller.dto.response.AnswerDTO;
import com.cpa.yusin.quiz.answer.controller.mapper.AnswerMapper;
import com.cpa.yusin.quiz.answer.controller.port.AnswerService;
import com.cpa.yusin.quiz.answer.domain.Answer;
import com.cpa.yusin.quiz.answer.service.port.AnswerRepository;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.global.exception.AnswerException;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.MemberException;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.question.domain.Question;
import com.cpa.yusin.quiz.question.service.QuestionAnswerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class AnswerServiceImpl implements AnswerService {
    private final AnswerRepository answerRepository;
    private final AnswerMapper answerMapper;
    private final QuestionAnswerService questionAnswerService;
    private final ExamService examService;

    @Transactional
    @Override
    public long save(AnswerRegisterRequest request, long questionId, Member member) {
        Question question = questionAnswerService.getQuestion(questionId);
        Answer answer = answerMapper.toAnswerEntity(request, question, member);
        questionAnswerService.updateAnswerCount(question, 1);

        return answerRepository.save(answer).getId();
    }

    @Transactional
    @Override
    public long save(AdminAnswerRegisterRequest request, long questionId, Member admin) {
        Question question = questionAnswerService.getQuestionForAdmin(questionId);
        Answer answer = answerMapper.toAnswerEntity(request, admin, question);

        answer = answerRepository.save(answer);

        questionAnswerService.answerQuestionByAdmin(questionId);

        return answer.getId();
    }

    @Transactional
    @Override
    public void update(AnswerUpdateRequest request, long answerId, Member member) {
        Answer answer = findById(answerId);

        validateOwnership(answer, member);

        answer.update(request.getContent());
        answerRepository.save(answer);
    }

    @Transactional
    @Override
    public void updateInAdminPage(AdminAnswerUpdateRequest request, long answerId) {
        Answer answer = findByIdForAdmin(answerId);
        answer.update(request.content());

        answerRepository.save(answer);
    }

    @Override
    public Answer findById(long id) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new AnswerException(ExceptionMessage.ANSWER_NOT_FOUND));
        examService.findPublishedById(answer.getQuestion().getProblem().getExam().getId());
        return answer;
    }

    @Override
    public Answer findByIdForAdmin(long id) {
        return answerRepository.findById(id)
                .orElseThrow(() -> new AnswerException(ExceptionMessage.ANSWER_NOT_FOUND));
    }

    @Override
    public AnswerDTO getAnswerById(long id) {
        Answer answer = findById(id);
        return answerMapper.toAnswerDTO(answer);
    }

    @Override
    public Page<AnswerDTO> getAnswersByQuestionId(long questionId, Pageable pageable) {
        questionAnswerService.getQuestion(questionId);

        return answerRepository.findByQuestionId(questionId, pageable)
                .map(answerMapper::toAnswerDTO);
    }

    @Override
    public Page<AnswerDTO> getAnswersByQuestionIdForAdmin(long questionId, Pageable pageable) {
        questionAnswerService.getQuestionForAdmin(questionId);

        return answerRepository.findByQuestionId(questionId, pageable)
                .map(answerMapper::toAnswerDTO);
    }

    @Override
    public List<AnswerDTO> getAnswersByQuestionId(long questionId) {
        // Keep the list endpoint consistent with the paged variant and with question
        // detail visibility rules.
        questionAnswerService.getQuestion(questionId);

        return answerRepository.findByQuestionId(questionId).stream()
                .map(answerMapper::toAnswerDTO)
                .toList();
    }

    @Transactional
    @Override
    public void deleteAnswer(long answerId, Member member) {
        Answer answer = Role.ADMIN.equals(member.getRole())
                ? findByIdForAdmin(answerId)
                : findById(answerId);

        validateOwnership(answer, member);

        Question question = answer.getQuestion();
        questionAnswerService.updateAnswerCount(question, -1);
        if (Role.ADMIN.equals(answer.getMember().getRole())
                && !answerRepository.hasOtherAdminAnswers(question.getId(), answerId)) {
            question.syncAnsweredByAdmin(false);
        }

        answerRepository.deleteById(answerId);
    }

    private void validateOwnership(Answer answer, Member member) {
        if (!answer.isOwner(member)) {
            throw new MemberException(ExceptionMessage.NO_AUTHORIZATION);
        }
    }
}
