package com.cpa.yusin.quiz.answer.integration;

import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerRegisterRequest;
import com.cpa.yusin.quiz.answer.controller.dto.request.AnswerUpdateRequest;
import com.cpa.yusin.quiz.answer.domain.Answer;
import com.cpa.yusin.quiz.answer.service.port.AnswerRepository;
import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.domain.ExamStatus;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import com.cpa.yusin.quiz.question.domain.Question;
import com.cpa.yusin.quiz.question.service.port.QuestionRepository;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith({ RestDocumentationExtension.class, TeardownExtension.class })
@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureRestDocs
class AnswerTest {
        @Autowired
        private MockMvc mvc;

        @Autowired
        QuestionRepository questionRepository;

        @Autowired
        SubjectRepository subjectRepository;

        @Autowired
        ExamRepository examRepository;

        @Autowired
        ProblemRepository problemRepository;

        @Autowired
        AnswerRepository answerRepository;

        @Autowired
        MemberRepository memberRepository;

        @Autowired
        JdbcTemplate jdbcTemplate;

        Subject subject;
        Exam exam;
        Problem problem;
        Question question;
        Answer answer;
        Member member;
        MemberDetails memberDetails;

        ObjectMapper mapper;

        @BeforeEach
        void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
                this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .apply(documentationConfiguration(restDocumentation))
                                .apply(springSecurity())
                                .build();

                subject = subjectRepository.save(Subject.builder()
                                .id(1L)
                                .name("영어")
                                .build());

                exam = examRepository.save(Exam.builder()
                                .id(1L)
                                .year(2024)
                                .name("1차")
                                .subjectId(subject.getId())
                                .status(ExamStatus.PUBLISHED)
                                .build());

                problem = problemRepository.save(Problem.builder()
                                .number(1)
                                .content("The walking tour was a big ___ to some people")
                                .explanation("설명")
                                .exam(exam)
                                .build());

                member = memberRepository.save(Member.builder()
                                .email("test@test.com")
                                .username("일반 사용자")
                                .password("encodedPass")
                                .role(Role.USER)
                                .platform(Platform.HOME)
                                .build());

                memberDetails = new MemberDetails(member, null);

                question = questionRepository.save(Question.builder()
                                .id(1L)
                                .title("정답이 4번인 이유")
                                .content("왜 4번이죠?")
                                .member(member)
                                .answerCount(0)
                                .problem(problem)
                                .build());

                answer = answerRepository.save(Answer.builder()
                                .id(1L)
                                .content("4번이기 때문입니다")
                                .member(member)
                                .question(question)
                                .build());

                mapper = new ObjectMapper();

        }

        @Test
        void save() throws Exception {
                // given
                AnswerRegisterRequest request = AnswerRegisterRequest.builder()
                                .content("이전 영상을 참고해주세요")
                                .build();

                long questionId = question.getId();

                // when
                ResultActions resultActions = mvc
                                .perform(post("/api/v1/question/{questionId}/answer", questionId)
                                                .with(user(memberDetails))
                                                .content(mapper.writeValueAsString(request))
                                                .contentType(MediaType.APPLICATION_JSON));

                // then
                resultActions
                                .andExpect(status().isCreated())
                                .andDo(document("saveAnswer",
                                                preprocessRequest(prettyPrint()),
                                                preprocessResponse(prettyPrint()),

                                                requestFields(
                                                                fieldWithPath("content").type(JsonFieldType.STRING)
                                                                                .description("내용")

                                                ),

                                                responseFields(
                                                                fieldWithPath("data").type(JsonFieldType.NUMBER)
                                                                                .description("답변 고유 식별자"))

                                ));

        }

        @Test
        void getAnswersByQuestionId() throws Exception {
                // given
                answerRepository.save(
                                Answer.builder().id(2L).member(member).content("1번은 안되나요?").question(question).build());
                answerRepository.save(
                                Answer.builder().id(3L).member(member).content("안됩니다").question(question).build());
                answerRepository.save(
                                Answer.builder().id(4L).member(member).content("네 감사합니다").question(question).build());

                int pageNumber = 0;
                int pageSize = 10;

                // when
                ResultActions resultActions = mvc
                                .perform(get("/api/v1/question/{questionId}/answer", question.getId())
                                                .queryParam("page", Integer.toString(pageNumber))
                                                .queryParam("size", Integer.toString(pageSize)));

                // then
                resultActions
                                .andExpect(status().isOk())
                                .andDo(document("getAnswers",
                                                preprocessRequest(prettyPrint()),
                                                preprocessResponse(prettyPrint()),

                                                responseFields(
                                                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER)
                                                                                .description("답변 고유 식별자"),
                                                                fieldWithPath("data[].memberId")
                                                                                .type(JsonFieldType.NUMBER)
                                                                                .description("작성자 회원 ID"),
                                                                fieldWithPath("data[].username")
                                                                                .type(JsonFieldType.STRING)
                                                                                .description("답변 등록 유저"),
                                                                fieldWithPath("data[].content")
                                                                                .type(JsonFieldType.STRING)
                                                                                .description("답변 내용"),
                                                                fieldWithPath("data[].createdAt")
                                                                                .type(JsonFieldType.STRING)
                                                                                .description("답변 등록 시간"),
                                                                fieldWithPath("data[].isAdmin")
                                                                                .type(JsonFieldType.BOOLEAN)
                                                                                .description("관리자 작성 여부"),

                                                                fieldWithPath("pageInfo.totalElements")
                                                                                .type(JsonFieldType.NUMBER)
                                                                                .description("총 데이터 수"),
                                                                fieldWithPath("pageInfo.totalPages")
                                                                                .type(JsonFieldType.NUMBER)
                                                                                .description("총 페이지 수"),
                                                                fieldWithPath("pageInfo.currentPage")
                                                                                .type(JsonFieldType.NUMBER)
                                                                                .description("현재 페이지"),
                                                                fieldWithPath("pageInfo.pageSize")
                                                                                .type(JsonFieldType.NUMBER)
                                                                                .description("페이지 크기"))

                                ));

        }

        @Test
        void getAnswersUsesRequestedZeroBasedPageAndCreatedAtDescendingOnly() throws Exception {
                Answer middle = answerRepository.save(Answer.builder()
                                .member(member)
                                .content("두 번째 답변")
                                .question(question)
                                .build());
                Member admin = memberRepository.save(Member.builder()
                                .email("admin@test.com")
                                .username("관리자")
                                .password("encodedPass")
                                .role(Role.ADMIN)
                                .platform(Platform.HOME)
                                .build());
                Answer newest = answerRepository.save(Answer.builder()
                                .member(admin)
                                .content("관리자 답변")
                                .question(question)
                                .build());

                LocalDateTime base = LocalDateTime.of(2026, 8, 9, 12, 0);
                updateAnswerCreatedAt(answer.getId(), base.minusMinutes(2));
                updateAnswerCreatedAt(middle.getId(), base.minusMinutes(1));
                updateAnswerCreatedAt(newest.getId(), base);

                mvc.perform(get("/api/v1/question/{questionId}/answer", question.getId())
                                .queryParam("page", "0")
                                .queryParam("size", "2")
                                .queryParam("sort", "id,asc"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data[0].id").value(newest.getId()))
                                .andExpect(jsonPath("$.data[0].isAdmin").value(true))
                                .andExpect(jsonPath("$.data[1].id").value(middle.getId()))
                                .andExpect(jsonPath("$.data[1].isAdmin").value(false))
                                .andExpect(jsonPath("$.pageInfo.currentPage").value(1));

                mvc.perform(get("/api/v1/question/{questionId}/answer", question.getId())
                                .queryParam("page", "1")
                                .queryParam("size", "2"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.length()").value(1))
                                .andExpect(jsonPath("$.data[0].id").value(answer.getId()))
                                .andExpect(jsonPath("$.pageInfo.currentPage").value(2));
        }

        @Test
        void updateAnswer() throws Exception {
                AnswerUpdateRequest request = AnswerUpdateRequest.builder()
                                .content("수정된 답변")
                                .build();

                mvc.perform(patch("/api/v1/answer/{answerId}", answer.getId())
                                .with(user(memberDetails))
                                .content(mapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value(answer.getId()))
                                .andExpect(jsonPath("$.data.content").value("수정된 답변"))
                                .andExpect(jsonPath("$.data.isAdmin").value(false))
                                .andDo(document("updateAnswer",
                                                preprocessRequest(prettyPrint()),
                                                preprocessResponse(prettyPrint()),
                                                requestFields(
                                                                fieldWithPath("content").type(JsonFieldType.STRING)
                                                                                .description("수정할 내용")),
                                                responseFields(
                                                                fieldWithPath("data.id").type(JsonFieldType.NUMBER)
                                                                                .description("답변 고유 식별자"),
                                                                fieldWithPath("data.memberId").type(JsonFieldType.NUMBER)
                                                                                .description("작성자 회원 ID"),
                                                                fieldWithPath("data.username").type(JsonFieldType.STRING)
                                                                                .description("답변 등록 유저"),
                                                                fieldWithPath("data.content").type(JsonFieldType.STRING)
                                                                                .description("수정된 답변 내용"),
                                                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING)
                                                                                .description("답변 등록 시간"),
                                                                fieldWithPath("data.isAdmin").type(JsonFieldType.BOOLEAN)
                                                                                .description("관리자 작성 여부"))));
        }

        @Test
        void updateAnswerRejectsNonOwner() throws Exception {
                Member otherMember = memberRepository.save(Member.builder()
                                .email("other@test.com")
                                .username("다른 사용자")
                                .password("encodedPass")
                                .role(Role.USER)
                                .platform(Platform.HOME)
                                .build());
                AnswerUpdateRequest request = AnswerUpdateRequest.builder()
                                .content("권한 없는 수정")
                                .build();

                mvc.perform(patch("/api/v1/answer/{answerId}", answer.getId())
                                .with(user(new MemberDetails(otherMember, null)))
                                .content(mapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isForbidden());
        }

        @Test
        void deleteAnswer() throws Exception {
                // given
                long answerId = answer.getId();

                // when
                ResultActions resultActions = mvc
                                .perform(delete("/api/v1/answer/{answerId}", answerId)
                                                .with(user(memberDetails)));

                // then
                resultActions
                                .andExpect(status().isNoContent())
                                .andExpect(content().string(""))
                                .andDo(document("deleteAnswer",
                                                preprocessRequest(prettyPrint()),
                                                preprocessResponse(prettyPrint())
                                ));

        }

        private void updateAnswerCreatedAt(long answerId, LocalDateTime createdAt) {
                jdbcTemplate.update("UPDATE answer SET created_at = ?, updated_at = ? WHERE id = ?",
                                createdAt, createdAt, answerId);
        }
}
