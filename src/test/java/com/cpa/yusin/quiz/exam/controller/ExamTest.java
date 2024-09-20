package com.cpa.yusin.quiz.exam.controller;

import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamCreateRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamDeleteRequest;
import com.cpa.yusin.quiz.exam.controller.dto.request.ExamUpdateRequest;
import com.cpa.yusin.quiz.exam.domain.ExamDomain;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.subject.domain.SubjectDomain;
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

import java.time.LocalDateTime;
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

    SubjectDomain economics;

    MemberDomain admin;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();

        admin = memberRepository.save(MemberDomain.builder()
                .email("John@gmail.com")
                .password("12341234")
                .username("John")
                .platform(Platform.HOME)
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());


        economics = subjectRepository.save(SubjectDomain.builder()
                .id(1L)
                .name("경제학")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void save_success() throws Exception
    {
        // given
        Long subjectId = economics.getId();

        ExamCreateRequest request = ExamCreateRequest.builder()
                .year(2024)
                .name("1차")
                .maxProblemCount(40)
                .build();

        // when
        ResultActions resultActions = mvc.perform(post("/api/v1/admin/exam")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .param("subjectId", subjectId.toString())
        );

        // then
        resultActions
                .andExpect(status().isCreated())
                .andDo(document("exam-create-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                            parameterWithName("subjectId").description("과목 고유 식별자")
                        ),

                        requestFields(
                                fieldWithPath("name").description("시험 이름"),
                                fieldWithPath("year").description("시험 연도"),
                                fieldWithPath("maxProblemCount").description("최대 문제 수")
                        ),

                        responseFields(
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("시험 고유 식별자"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("시험 이름"),
                                fieldWithPath("data.year").type(JsonFieldType.NUMBER).description("시험 연도"),
                                fieldWithPath("data.maxProblemCount").type(JsonFieldType.NUMBER).description("시험 문제 갯수")
                        )
                ))

        ;

    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void save_fail_NoFields() throws Exception
    {
        // given
        Long subjectId = economics.getId();

        ExamCreateRequest request = ExamCreateRequest.builder()
                .build();

        // when
        ResultActions resultActions = mvc.perform(post("/api/v1/admin/exam")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .param("subjectId", subjectId.toString())
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andDo(document("exam-create-fail-empty-name",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        queryParameters(
                                parameterWithName("subjectId").description("과목 고유 식별자")
                        ),

                        requestFields(
                                fieldWithPath("name").description("시험 이름"),
                                fieldWithPath("year").description("시험 연도"),
                                fieldWithPath("maxProblemCount").description("최대 문제 수")
                        ),

                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메세지"),
                                fieldWithPath("valueErrors[].descriptor").type(JsonFieldType.STRING).description("오류 항목"),
                                fieldWithPath("valueErrors[].rejectedValue").type(JsonFieldType.STRING).description("오류 내용"),
                                fieldWithPath("valueErrors[].reason").type(JsonFieldType.STRING).description("오류 원인")
                        ))
                );

    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void save_fail_duplicated() throws Exception
    {
        // given
        Long subjectId = economics.getId();

        examRepository.save(ExamDomain.builder()
                .id(1L)
                .name("1차")
                .year(2024)
                .maxProblemCount(40)
                .subjectId(economics.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        ExamCreateRequest request = ExamCreateRequest.builder()
                .name("1차")
                .year(2024)
                .maxProblemCount(40)
                .build();

        // when
        ResultActions resultActions = mvc.perform(post("/api/v1/admin/exam")
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .param("subjectId", subjectId.toString())
        );

        // then
        resultActions
                .andExpect(status().isInternalServerError())
                .andDo(document("exam-create-fail-duplicated",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        queryParameters(
                                parameterWithName("subjectId").description("과목 고유 식별자")
                        ),

                        requestFields(
                                fieldWithPath("name").description("시험 이름"),
                                fieldWithPath("year").description("시험 연도"),
                                fieldWithPath("maxProblemCount").description("최대 문제 수")
                        ),

                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메세지")
                        ))
                );

    }


    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void update_success() throws Exception
    {
        // given
        ExamDomain exam = examRepository.save(ExamDomain.builder()
                .id(1L)
                .name("1차")
                .year(2024)
                .maxProblemCount(40)
                .subjectId(economics.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        ExamUpdateRequest request = ExamUpdateRequest.builder()
                .name("3차")
                .year(2023)
                .maxProblemCount(30)
                .build();

        // when
        ResultActions resultActions = mvc.perform(patch("/api/v1/admin/exam/" + exam.getId())
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("exam-update-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("name").description("시험 이름"),
                                fieldWithPath("year").description("시험 연도"),
                                fieldWithPath("maxProblemCount").description("최대 문제 수")
                        ),

                        responseFields(
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("시험 고유 식별자"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("시험 이름"),
                                fieldWithPath("data.year").type(JsonFieldType.NUMBER).description("시험 연도"),
                                fieldWithPath("data.maxProblemCount").type(JsonFieldType.NUMBER).description("시험 문제 갯수")
                        ))
                );

    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void update_fail_noFields() throws Exception
    {
        // given
        ExamDomain exam = examRepository.save(ExamDomain.builder()
                .id(1L)
                .name("1차")
                .year(2024)
                .maxProblemCount(40)
                .subjectId(economics.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        ExamUpdateRequest request = ExamUpdateRequest.builder()
                .build();

        // when
        ResultActions resultActions = mvc.perform(patch("/api/v1/admin/exam/" + exam.getId())
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andDo(document("exam-update-fail-no-fields",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("name").description("시험 이름"),
                                fieldWithPath("year").description("시험 연도"),
                                fieldWithPath("maxProblemCount").description("최대 문제 수")
                        ),

                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메세지"),
                                fieldWithPath("valueErrors[].descriptor").type(JsonFieldType.STRING).description("오류 항목"),
                                fieldWithPath("valueErrors[].rejectedValue").type(JsonFieldType.STRING).description("오류 내용"),
                                fieldWithPath("valueErrors[].reason").type(JsonFieldType.STRING).description("오류 원인")
                        ))
                );

    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void update_fail_duplicated() throws Exception
    {
        // given
        ExamDomain exam = examRepository.save(ExamDomain.builder()
                .id(1L)
                .name("1차")
                .year(2024)
                .maxProblemCount(40)
                .subjectId(economics.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        examRepository.save(ExamDomain.builder()
                .id(2L)
                .name("2차")
                .year(2024)
                .maxProblemCount(40)
                .subjectId(economics.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        ExamUpdateRequest request = ExamUpdateRequest.builder()
                .name("2차")
                .year(2024)
                .maxProblemCount(40)
                .build();

        // when
        ResultActions resultActions = mvc.perform(patch("/api/v1/admin/exam/" + exam.getId())
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isInternalServerError())
                .andDo(document("exam-update-fail-duplicated",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("name").description("시험 이름"),
                                fieldWithPath("year").description("시험 연도"),
                                fieldWithPath("maxProblemCount").description("최대 문제 수")
                        ),

                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메세지")
                        ))
                );

    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getById_success() throws Exception
    {
        // given
        ExamDomain exam = examRepository.save(ExamDomain.builder()
                .id(1L)
                .name("1차")
                .year(2024)
                .maxProblemCount(40)
                .subjectId(economics.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // when
        ResultActions resultActions = mvc.perform(get("/api/v1/admin/exam/" + exam.getId())

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
                                fieldWithPath("data.year").type(JsonFieldType.NUMBER).description("시험 연도"),
                                fieldWithPath("data.maxProblemCount").type(JsonFieldType.NUMBER).description("시험 문제 갯수")
                        ))
                );

    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getById_ExamNotFound() throws Exception
    {
        // given

        // when
        ResultActions resultActions = mvc.perform(get("/api/v1/admin/exam/" + 1L)

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
        examRepository.save(ExamDomain.builder()
                .id(1L)
                .name("1차")
                .year(2024)
                .maxProblemCount(40)
                .subjectId(economics.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        examRepository.save(ExamDomain.builder()
                .id(2L)
                .name("2차")
                .year(2024)
                .maxProblemCount(40)
                .subjectId(economics.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        examRepository.save(ExamDomain.builder()
                .id(3L)
                .name("3차")
                .year(2024)
                .maxProblemCount(40)
                .subjectId(economics.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // when
        ResultActions resultActions = mvc.perform(get("/api/v1/admin/exam")
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
                                fieldWithPath("data[].year").type(JsonFieldType.NUMBER).description("시험 연도"),
                                fieldWithPath("data[].maxProblemCount").type(JsonFieldType.NUMBER).description("시험 문제 갯수")
                        ))
                );

    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void deleteById_success() throws Exception
    {
        // given
        ExamDomain exam1 = examRepository.save(ExamDomain.builder()
                .id(1L)
                .name("1차")
                .year(2024)
                .maxProblemCount(40)
                .subjectId(economics.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        ExamDomain exam2 = examRepository.save(ExamDomain.builder()
                .id(2L)
                .name("2차")
                .year(2024)
                .maxProblemCount(40)
                .subjectId(economics.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        ExamDomain exam3 = examRepository.save(ExamDomain.builder()
                .id(3L)
                .name("3차")
                .year(2024)
                .maxProblemCount(40)
                .subjectId(economics.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        ExamDeleteRequest request = ExamDeleteRequest.builder()
                .examIds(List.of(exam1.getId(), exam2.getId(), exam3.getId()))
                .build();

        // when
        ResultActions resultActions = mvc.perform(delete("/api/v1/admin/exam" )
                .content(mapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)

        );

        // then
        resultActions
                .andExpect(status().isNoContent())
                .andDo(document("deleteExamById",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("examIds").description("시험 고유 식별자 리스트")
                        )
                ));

    }


}
