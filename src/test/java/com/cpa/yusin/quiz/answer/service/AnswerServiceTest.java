package com.cpa.yusin.quiz.answer.service;

import com.cpa.yusin.quiz.answer.controller.dto.request.AdminAnswerRegisterRequest;
import com.cpa.yusin.quiz.config.MockSetup;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.question.domain.Question;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnswerServiceTest extends MockSetup {

    @Test
    @DisplayName("마지막 관리자 답변을 삭제하면 질문 answeredByAdmin 이 false 로 되돌아간다")
    void deleteAnswer_whenLastAdminAnswerRemoved_thenSyncAnsweredByAdminFalse() {
        Member admin = createAdmin("admin1@test.com");
        Question question = createQuestion("질문1");

        long answerId = testContainer.answerService.save(
                new AdminAnswerRegisterRequest("관리자 답변"),
                question.getId(),
                admin
        );

        testContainer.answerService.deleteAnswer(answerId, admin);

        Question updatedQuestion = testContainer.questionRepository.findById(question.getId()).orElseThrow();
        assertThat(updatedQuestion.isAnsweredByAdmin()).isFalse();
        assertThat(updatedQuestion.getAnswerCount()).isZero();
    }

    @Test
    @DisplayName("다른 관리자 답변이 남아 있으면 answeredByAdmin 상태를 유지한다")
    void deleteAnswer_whenOtherAdminAnswerExists_thenKeepAnsweredByAdminTrue() {
        Member firstAdmin = createAdmin("admin1@test.com");
        Member secondAdmin = createAdmin("admin2@test.com");
        Question question = createQuestion("질문2");

        long firstAnswerId = testContainer.answerService.save(
                new AdminAnswerRegisterRequest("첫 관리자 답변"),
                question.getId(),
                firstAdmin
        );
        testContainer.answerService.save(
                new AdminAnswerRegisterRequest("두 번째 관리자 답변"),
                question.getId(),
                secondAdmin
        );

        testContainer.answerService.deleteAnswer(firstAnswerId, firstAdmin);

        Question updatedQuestion = testContainer.questionRepository.findById(question.getId()).orElseThrow();
        assertThat(updatedQuestion.isAnsweredByAdmin()).isTrue();
        assertThat(updatedQuestion.getAnswerCount()).isEqualTo(1);
    }

    private Member createAdmin(String email) {
        return testContainer.memberRepository.save(Member.builder()
                .email(email)
                .password("password")
                .username(email)
                .platform(Platform.HOME)
                .role(Role.ADMIN)
                .build());
    }

    private Question createQuestion(String title) {
        return testContainer.questionRepository.save(Question.builder()
                .member(member1)
                .title(title)
                .content("질문 내용")
                .answeredByAdmin(false)
                .answerCount(0)
                .problem(physicsProblem1)
                .build());
    }
}
