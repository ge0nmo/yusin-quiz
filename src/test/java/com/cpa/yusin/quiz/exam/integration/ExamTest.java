package com.cpa.yusin.quiz.exam.integration;

import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamDeleteRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
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
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

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
class ExamTest
{
    @Autowired
    private MockMvc mvc;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private MemberRepository memberRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    Subject economics;

    Member admin;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();

        admin = memberRepository.save(Member.builder()
                .email("John@gmail.com")
                .password("12341234")
                .username("John")
                .platform(Platform.HOME)
                .role(Role.ADMIN)
                .build());


        economics = subjectRepository.save(Subject.builder()
                .id(1L)
                .name("경제학")
                .build());
    }


    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getById_success() throws Exception
    {
        // given
        Exam exam = examRepository.save(Exam.builder()
                .id(1L)
                .name("1차")
                .year(2024)
                .subjectId(economics.getId())
                .build());

        // when
        ResultActions resultActions = mvc.perform(get("/api/v1/exam/" + exam.getId())

        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("getExamById",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),


                        responseFields(
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("시험 고유 식별자"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("시험 이름"),
                                fieldWithPath("data.year").type(JsonFieldType.NUMBER).description("시험 연도")
                        ))
                );

    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getById_ExamNotFound() throws Exception
    {
        // given

        // when
        ResultActions resultActions = mvc.perform(get("/api/v1/exam/" + 1L)

        );

        // then
        resultActions
                .andExpect(status().isInternalServerError())
                .andDo(document("getExamById",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),


                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메세지")
                        ))
                );

    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getBySubjectIdAndYear_success() throws Exception
    {
        // given
        examRepository.save(Exam.builder()
                .id(1L)
                .name("1차")
                .year(2024)
                .subjectId(economics.getId())
                .build());

        examRepository.save(Exam.builder()
                .id(2L)
                .name("2차")
                .year(2024)
                .subjectId(economics.getId())
                .build());

        examRepository.save(Exam.builder()
                .id(3L)
                .name("3차")
                .year(2024)
                .subjectId(economics.getId())
                .build());

        // when
        ResultActions resultActions = mvc.perform(get("/api/v1/exam")
                .param("subjectId", economics.getId().toString())
                .param("year", String.valueOf(2024))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("getExamsBySubjectIdAndYear",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        queryParameters(
                                parameterWithName("subjectId").description("과목 고유 식별자"),
                                parameterWithName("year").description("시험 연도")
                        ),

                        responseFields(
                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("시험 고유 식별자"),
                                fieldWithPath("data[].name").type(JsonFieldType.STRING).description("시험 이름"),
                                fieldWithPath("data[].year").type(JsonFieldType.NUMBER).description("시험 연도")
                        ))
                );

    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getBySubjectIdAndYear_withoutYear() throws Exception
    {
        // given
        examRepository.save(Exam.builder()
                .id(1L)
                .name("1차")
                .year(2024)
                .subjectId(economics.getId())
                .build());

        examRepository.save(Exam.builder()
                .id(2L)
                .name("2차")
                .year(2024)
                .subjectId(economics.getId())
                .build());

        examRepository.save(Exam.builder()
                .id(3L)
                .name("1차")
                .year(2023)
                .subjectId(economics.getId())
                .build());

        // when
        ResultActions resultActions = mvc.perform(get("/api/v1/exam")
                .param("subjectId", economics.getId().toString())
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("getExamsBySubjectIdWithoutYear",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        queryParameters(
                                parameterWithName("subjectId").description("과목 고유 식별자"),
                                parameterWithName("year").description("시험 연도").optional()
                        ),

                        responseFields(
                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("시험 고유 식별자"),
                                fieldWithPath("data[].name").type(JsonFieldType.STRING).description("시험 이름"),
                                fieldWithPath("data[].year").type(JsonFieldType.NUMBER).description("시험 연도")
                        ))
                );

    }

    @Test
    void getYears() throws Exception
    {
        // given
        examRepository.save(Exam.builder()
                .id(1L)
                .name("1차")
                .year(2024)
                .subjectId(economics.getId())
                .build());

        examRepository.save(Exam.builder()
                .id(2L)
                .name("1차")
                .year(2023)
                .subjectId(economics.getId())
                .build());

        examRepository.save(Exam.builder()
                .id(3L)
                .name("1차")
                .year(2022)
                .subjectId(economics.getId())
                .build());

        // when
        ResultActions resultActions = mvc.perform(get("/api/v1/exam/year")
                .param("subjectId", economics.getId().toString())
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("getYearBySubjectId",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        queryParameters(
                                parameterWithName("subjectId").description("과목 고유 식별자")
                        ),

                        responseFields(
                                fieldWithPath("data[]").type(JsonFieldType.ARRAY).description("시험 연도 정보")
                        ))
                );

    }
}
