package com.cpa.yusin.quiz.question.integration;

import com.cpa.yusin.quiz.answer.service.port.AnswerRepository;
import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import com.cpa.yusin.quiz.question.controller.dto.request.QuestionRegisterRequest;
import com.cpa.yusin.quiz.question.controller.dto.request.QuestionUpdateRequest;
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
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, TeardownExtension.class})
@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureRestDocs
class QuestionTest
{
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

    Subject subject;
    Exam exam;
    Problem problem;
    Question question;

    ObjectMapper mapper;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation)
    {
        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
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
                .build());

        problem = problemRepository.save(Problem.builder()
                        .number(1)
                        .content("The walking tour was a big ___ to some people")
                        .explanation("설명")
                        .exam(exam)
                        .build());

        mapper = new ObjectMapper();


        question = questionRepository.save(Question.builder()
                        .id(1L)
                        .title("정답이 4번인 이유")
                        .content("왜 4번이죠?")
                        .username("유저1")
                        .password("123123")
                        .answerCount(0)
                        .problem(problem)
                        .build());
    }

    @Test
    void saveQuestion() throws Exception
    {
        // given
        QuestionRegisterRequest request = QuestionRegisterRequest.builder()
                .title("@Import 관련 질문")
                .username("유저1")
                .password("123123")
                .content("AtTargetAtWithinTest class에 대한 질문이 있어 글 남깁니다.\n" +
                        "\n" +
                        "@Import({AtTargetAtWithinTest.Config.class}) 를 쓰지 않고,\n" +
                        "\n" +
                        " \n" +
                        "\n" +
                        "static class Config 에 @TestConfiguration을 써도 동작하더라구요, 혹시 작동방식에있어서 차이가 있을까요?")
                .build();

        // when
        ResultActions resultActions = mvc
                .perform(post("/api/v1/problem/{problemId}/question", problem.getId())
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)

                );

        // then
        resultActions
                .andExpect(status().isCreated())
                .andDo(document("saveQuestion",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("username").type(JsonFieldType.STRING).description("질문 등록자 이름"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                                fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("내용")

                        ),

                        responseFields(
                                fieldWithPath("data").type(JsonFieldType.NUMBER).description("질문 고유 식별자")
                        )))
                ;
    }

    @Test
    void updateQuestion() throws Exception
    {
        // given
        QuestionUpdateRequest request = QuestionUpdateRequest.builder()
                .title("제목 수정")
                .content("내용 수정")
                .build();

        // when
        ResultActions resultActions = mvc
                .perform(patch("/api/v1/question/{questionId}", question.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
                );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("updateQuestion",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("내용")

                        ),

                        responseFields(
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("질문 고유 식별자"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("질문 등록자 이름"),
                                fieldWithPath("data.title").type(JsonFieldType.STRING).description("질문 제목"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("질문 내용"),
                                fieldWithPath("data.answerCount").type(JsonFieldType.NUMBER).description("질문의 답변 수"),
                                fieldWithPath("data.answeredByAdmin").type(JsonFieldType.BOOLEAN).description("질문 답변 여부"),
                                fieldWithPath("data.problemId").type(JsonFieldType.NUMBER).description("문제 ID"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("질문 등록 시간")

                        )

                ))
        ;

    }

    @Test
    void getQuestion() throws Exception
    {
        // given
        long questionId = 1L;

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/question/{questionId}", questionId));

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("getQuestion",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        responseFields(
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("질문 고유 식별자"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("질문 등록자 이름"),
                                fieldWithPath("data.title").type(JsonFieldType.STRING).description("질문 제목"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("질문 내용"),
                                fieldWithPath("data.answerCount").type(JsonFieldType.NUMBER).description("질문의 답변 수"),
                                fieldWithPath("data.answeredByAdmin").type(JsonFieldType.BOOLEAN).description("질문 답변 여부"),
                                fieldWithPath("data.problemId").type(JsonFieldType.NUMBER).description("문제 ID"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("질문 등록 시간")
                        )

                ));
    }

    @Test
    void getAllByProblemId() throws Exception
    {
        // given
        questionRepository.save(Question.builder().id(2L).title("2번은 왜 정답이 아니죠?").answerCount(0).content("2번은 왜 정답이 아니죠?").username("유저1").password("123123").problem(problem).build());
        questionRepository.save(Question.builder().id(3L).title("3번은 왜 정답이 아니죠?").answerCount(0).content("3번은 왜 정답이 아니죠?").username("유저1").password("123123").problem(problem).build());
        questionRepository.save(Question.builder().id(4L).title("4번은 왜 정답이 아니죠?").answerCount(0).content("4번은 왜 정답이 아니죠?").username("유저1").password("123123").problem(problem).build());
        questionRepository.save(Question.builder().id(5L).title("5번은 왜 정답이 아니죠?").answerCount(0).content("5번은 왜 정답이 아니죠?").username("유저1").password("123123").problem(problem).build());
        questionRepository.save(Question.builder().id(6L).title("6번은 왜 정답이 아니죠?").answerCount(0).content("6번은 왜 정답이 아니죠?").username("유저1").password("123123").problem(problem).build());
        questionRepository.save(Question.builder().id(7L).title("7번은 왜 정답이 아니죠?").answerCount(0).content("7번은 왜 정답이 아니죠?").username("유저1").password("123123").problem(problem).build());
        questionRepository.save(Question.builder().id(8L).title("8번은 왜 정답이 아니죠?").answerCount(0).content("8번은 왜 정답이 아니죠?").username("유저1").password("123123").problem(problem).build());

        int pageSize = 10;
        int pageNumber = 0;


        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/problem/{problemId}/question", problem.getId())
                        .queryParam("page", Integer.toString(pageNumber))
                        .queryParam("size", Integer.toString(pageSize))
                );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("getQuestions",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        responseFields(
                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("질문 고유 식별자"),
                                fieldWithPath("data[].username").type(JsonFieldType.STRING).description("질문 등록자 이름"),
                                fieldWithPath("data[].title").type(JsonFieldType.STRING).description("질문 제목"),
                                fieldWithPath("data[].content").type(JsonFieldType.STRING).description("질문 내용"),
                                fieldWithPath("data[].answerCount").type(JsonFieldType.NUMBER).description("질문의 답변 수"),
                                fieldWithPath("data[].answeredByAdmin").type(JsonFieldType.BOOLEAN).description("질문 답변 여부"),
                                fieldWithPath("data[].problemId").type(JsonFieldType.NUMBER).description("문제 ID"),
                                fieldWithPath("data[].createdAt").type(JsonFieldType.STRING).description("질문 등록 시간"),


                                fieldWithPath("pageInfo.totalElements").type(JsonFieldType.NUMBER).description("총 데이터 수"),
                                fieldWithPath("pageInfo.totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수"),
                                fieldWithPath("pageInfo.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지"),
                                fieldWithPath("pageInfo.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기")
                        )

                ));



    }

    @Test
    void verifyPassword() throws Exception
    {
        // given
        String password = "123123";
        long id = 1L;

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/question/{questionId}/verification", id)
                        .queryParam("password", password))
                ;

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("questionVerification",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        responseFields(
                                fieldWithPath("data").type(JsonFieldType.BOOLEAN).description("인증 성공 여부")
                        )

                ));

    }

    @Test
    void deleteById() throws Exception {
        // given
        long questionId = question.getId();

        // when
        ResultActions resultActions = mvc.perform(delete("/api/v1/question/{questionId}", questionId));


        // then
        resultActions
                .andExpect(status().isNoContent())
                .andDo(document("deleteQuestion",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        responseFields(
                                fieldWithPath("data").type(JsonFieldType.BOOLEAN).description("삭제 성공 여부")
                        )

                ));

    }
}