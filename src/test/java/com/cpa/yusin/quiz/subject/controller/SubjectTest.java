package com.cpa.yusin.quiz.subject.controller;

import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectUpdateRequest;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(TeardownExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureRestDocs
public class SubjectTest
{
    @Autowired
    private MockMvc mvc;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private MemberRepository memberRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    MemberDomain admin;

    @BeforeEach
    void setUp()
    {
        admin = memberRepository.save(MemberDomain.builder()
                .id(1L)
                .email("admin@gmail.com")
                .username("admin")
                .password("12341234")
                .platform(Platform.HOME)
                .role(Role.ADMIN)
                .build());
    }

    @WithUserDetails(value = "admin@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void save_success() throws Exception
    {
        // given
        SubjectCreateRequest request = SubjectCreateRequest.builder()
                .name("회계학")
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions resultActions = mvc
                .perform(post("/api/v1/admin/subject")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isCreated())
                .andDo(document("과목 등록 - 성공",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description("과목 이름")
                        ),

                        responseFields(
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("과목 고유 식별자"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("과목 이름")
                        )
                ));
    }

    @WithUserDetails(value = "admin@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void save_duplicatedName() throws Exception
    {
        // given
        subjectRepository.save(SubjectDomain.builder()
                .id(1L)
                .name("회계학")
                .build());

        SubjectCreateRequest request = SubjectCreateRequest.builder()
                .name("회계학")
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions resultActions = mvc
                .perform(post("/api/v1/admin/subject")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isInternalServerError())
                .andDo(document("과목 등록 - 중복된 이름",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description("과목 이름")
                        ),

                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메세지")
                        )
                ));
    }

    @WithUserDetails(value = "admin@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void update() throws Exception
    {
        // given
        SubjectDomain subject = subjectRepository.save(SubjectDomain.builder()
                .id(1L)
                .name("회계학")
                .build());

        SubjectUpdateRequest request = SubjectUpdateRequest.builder()
                .name("경제학")
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions resultActions = mvc
                .perform(patch("/api/v1/admin/subject/" + subject.getId())
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("과목 수정 - 성공",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description("과목 이름")
                        ),

                        responseFields(
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("과목 고유 식별자"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("과목 이름")
                        )
                ));
    }


}
