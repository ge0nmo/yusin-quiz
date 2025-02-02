package com.cpa.yusin.quiz.answer.integration;

import com.cpa.yusin.quiz.answer.domain.Answer;
import com.cpa.yusin.quiz.answer.service.port.AnswerRepository;
import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, TeardownExtension.class})
@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureRestDocs
class AnswerTest
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
    Answer answer;

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

        question = questionRepository.save(Question.builder()
                .id(1L)
                .title("정답이 4번인 이유")
                .content("왜 4번이죠?")
                .password("123123")
                .problem(problem)
                .build());

        answer = answerRepository.save(Answer.builder()
                        .id(1L)
                        .content("4번이기 때문입니다")
                        .username("관리자")
                        .password("123123")
                        .question(question)
                        .build());

        mapper = new ObjectMapper();

    }


    @Test
    void getAnswerById() throws Exception
    {
        // given
        long answerId = 1L;

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/answer/{answerId}", answerId));

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("getAnswer",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        responseFields(
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("답변 고유 식별자"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("답변 유저 이름"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("답변 내용"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("답변 등록 시간")
                        )

                ));
    }

    @Test
    void getAnswersByQuestionId() throws Exception
    {
        // given
        answerRepository.save(Answer.builder().id(2L).username("회원1").password("123132").content("1번은 안되나요?").question(question).build());
        answerRepository.save(Answer.builder().id(3L).username("관리자").password("123132").content("안됩니다").question(question).build());
        answerRepository.save(Answer.builder().id(4L).username("회원1").password("123132").content("네 감사합니다").question(question).build());

        int pageNumber = 1;
        int pageSize = 10;

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/question/{questionId}/answer", question.getId())
                        .queryParam("page", Integer.toString(pageNumber))
                        .queryParam("size", Integer.toString(pageSize))
                );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("getAnswers",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        responseFields(
                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("답변 고유 식별자"),
                                fieldWithPath("data[].username").type(JsonFieldType.STRING).description("답변 등록 유저"),
                                fieldWithPath("data[].content").type(JsonFieldType.STRING).description("답변 내용"),
                                fieldWithPath("data[].createdAt").type(JsonFieldType.STRING).description("답변 등록 시간"),


                                fieldWithPath("pageInfo.totalElements").type(JsonFieldType.NUMBER).description("총 데이터 수"),
                                fieldWithPath("pageInfo.totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수"),
                                fieldWithPath("pageInfo.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지"),
                                fieldWithPath("pageInfo.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기")
                        )

                ));

    }
}