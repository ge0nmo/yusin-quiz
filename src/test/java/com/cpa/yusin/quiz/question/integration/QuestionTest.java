package com.cpa.yusin.quiz.question.integration;

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
import static org.springframework.restdocs.request.RequestDocumentation.*;
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
                        .password("123123")
                        .problem(problem)
                        .build());
    }

    @Test
    void saveQuestion() throws Exception
    {
        // given
        QuestionRegisterRequest request = QuestionRegisterRequest.builder()
                .title("@Import 관련 질문")
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
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                                fieldWithPath("title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("content").type(JsonFieldType.STRING).description("내용")

                        ),

                        responseFields(
                                fieldWithPath("data").type(JsonFieldType.NUMBER).description("질문 고유 식별자")
                        )

                        ))
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
                                fieldWithPath("data.title").type(JsonFieldType.STRING).description("질문 제목"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("질문 내용")
                        )

                ))
        ;

    }

    @Test
    void getQuestion()
    {
    }

    @Test
    void getAllByProblemId() throws Exception
    {
        // given

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/")

                );

        // then
        resultActions
                .andExpect(status().isOk())
                ;

    }
}