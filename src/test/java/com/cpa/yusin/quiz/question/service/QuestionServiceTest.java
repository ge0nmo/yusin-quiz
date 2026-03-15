package com.cpa.yusin.quiz.question.service;

import com.cpa.yusin.quiz.config.TestContainer;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.global.exception.QuestionException;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.question.controller.dto.request.QuestionRegisterRequest;
import com.cpa.yusin.quiz.question.controller.dto.request.QuestionUpdateRequest;
import com.cpa.yusin.quiz.question.controller.dto.response.QuestionDTO;
import com.cpa.yusin.quiz.question.domain.Question;
import com.cpa.yusin.quiz.question.service.dto.AdminQuestionSearchCondition;
import com.cpa.yusin.quiz.question.service.dto.AdminQuestionStatus;
import com.cpa.yusin.quiz.subject.domain.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionServiceTest {
        TestContainer testContainer;
        Problem problem;
        Member member;

        @BeforeEach
        void setUp() {
                testContainer = new TestContainer();
                Subject subject = testContainer.subjectRepository.save(Subject.builder()
                                .id(1L)
                                .name("회계학")
                                .build());
                Exam exam = testContainer.examRepository.save(Exam.builder()
                                .id(1L)
                                .name("1차")
                                .year(2025)
                                .subjectId(subject.getId())
                                .build());
                problem = testContainer.problemRepository.save(Problem.builder()
                                .id(1L)
                                .content("problem 1")
                                .exam(exam)
                                .explanation("explanation 1")
                                .number(1)
                                .build());

                member = testContainer.memberRepository.save(Member.builder()
                                .id(1L)
                                .email("test@test.com")
                                .username("testUser")
                                .password("encodedPass")
                                .role(Role.USER)
                                .platform(Platform.HOME)
                                .build());
        }

        @Test
        void save() {
                // given
                QuestionRegisterRequest request = QuestionRegisterRequest.builder()
                                .title("title 1")
                                .content("content 1")
                                .build();

                // when
                long questionId = testContainer.questionService.save(request, 1L, member);

                // then
                Question question = testContainer.questionRepository.findById(questionId).orElseThrow();
                assertThat(question.getTitle()).isEqualTo(request.getTitle());
                assertThat(question.getContent()).isEqualTo(request.getContent());
                assertThat(question.getMember().getId()).isEqualTo(member.getId());
        }

        @Test
        void update() {
                // given
                testContainer.questionRepository.save(Question.builder()
                                .id(1L)
                                .title("title")
                                .content("content")
                                .member(member)
                                .answerCount(0)
                                .problem(problem)
                                .build());

                QuestionUpdateRequest request = QuestionUpdateRequest.builder()
                                .title("updated title")
                                .content("updated content")
                                .build();

                // when
                testContainer.questionService.update(request, 1L, member);

                // then
                Question question = testContainer.questionRepository.findById(1L).orElseThrow();
                assertThat(question.getTitle()).isEqualTo("updated title");
                assertThat(question.getContent()).isEqualTo("updated content");
        }

        @Test
        void findById() {
                // given
                testContainer.questionRepository.save(Question.builder()
                                .id(1L)
                                .title("title")
                                .content("content")
                                .member(member)
                                .answerCount(0)
                                .problem(problem)
                                .build());

                // when
                Question response = testContainer.questionService.findById(1L);

                // then
                assertThat(response).isNotNull();
                assertThat(response.getTitle()).isEqualTo("title");
                assertThat(response.getContent()).isEqualTo("content");
        }

        @Test
        void findById_throwErrorIfNotFound() {
                // given

                // when

                // then
                assertThatThrownBy(() -> testContainer.questionService.findById(1L))
                                .isInstanceOf(QuestionException.class);
        }

        @Test
        void getById() {
                // given
                testContainer.questionRepository.save(Question.builder()
                                .id(1L)
                                .title("title")
                                .content("content")
                                .answerCount(0)
                                .problem(problem)
                                .member(member)
                                .build());

                // when
                QuestionDTO response = testContainer.questionService.getById(1L);

                // then
                assertThat(response).isNotNull();
                assertThat(response.getTitle()).isEqualTo("title");
                assertThat(response.getContent()).isEqualTo("content");
                assertThat(response.getMemberId()).isEqualTo(member.getId());
                assertThat(response.getUsername()).isEqualTo(member.getUsername());
        }

        @Test
        void getAllByProblemId() {
                // given
                testContainer.questionRepository.save(Question.builder()
                                .id(1L)
                                .title("title1")
                                .content("content1")
                                .problem(problem)
                                .answerCount(0)
                                .member(member)
                                .build());

                testContainer.questionRepository.save(Question.builder()
                                .id(2L)
                                .title("title2")
                                .content("content2")
                                .problem(problem)
                                .answerCount(0)
                                .member(member)
                                .build());

                testContainer.questionRepository.save(Question.builder()
                                .id(3L)
                                .title("title3")
                                .content("content3")
                                .answerCount(0)
                                .problem(Problem.builder().id(2L).build())
                                .member(member)
                                .build());

                // when
                Page<QuestionDTO> response = testContainer.questionService.getAllByProblemId(PageRequest.of(0, 10), 1L);
                // then
                assertThat(response).isNotNull();
                assertThat(response.getTotalElements()).isEqualTo(2);

        }

        @Test
        void findAllQuestions_shouldFilterByStatusAndTrimmedKeyword() {
                // given
                Member searchableMember = testContainer.memberRepository.save(Member.builder()
                                .id(2L)
                                .email("alpha@test.com")
                                .username("alpha-user")
                                .password("encodedPass")
                                .role(Role.USER)
                                .platform(Platform.HOME)
                                .build());

                testContainer.questionRepository.save(Question.builder()
                                .id(1L)
                                .title("세법 질문")
                                .content("환급 관련 문의")
                                .problem(problem)
                                .member(member)
                                .answeredByAdmin(true)
                                .answerCount(1)
                                .build());

                testContainer.questionRepository.save(Question.builder()
                                .id(2L)
                                .title("기출 질문")
                                .content("내용")
                                .problem(problem)
                                .member(searchableMember)
                                .answeredByAdmin(false)
                                .answerCount(0)
                                .build());

                testContainer.questionRepository.save(Question.builder()
                                .id(3L)
                                .title("다른 질문")
                                .content("매칭되지 않는 내용")
                                .problem(problem)
                                .member(member)
                                .answeredByAdmin(false)
                                .answerCount(0)
                                .build());

                // when
                Page<QuestionDTO> response = testContainer.questionService.findAllQuestions(
                                PageRequest.of(0, 10),
                                AdminQuestionSearchCondition.of(AdminQuestionStatus.UNANSWERED, "  alpha@test.com  "));

                // then
                assertThat(response.getTotalElements()).isEqualTo(1);
                assertThat(response.getContent()).singleElement()
                                .satisfies(question -> {
                                        assertThat(question.getId()).isEqualTo(2L);
                                        assertThat(question.isAnsweredByAdmin()).isFalse();
                                        assertThat(question.getUsername()).isEqualTo("alpha-user");
                                });
        }
}
