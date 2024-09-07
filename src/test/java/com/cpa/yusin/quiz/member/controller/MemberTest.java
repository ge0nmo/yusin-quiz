package com.cpa.yusin.quiz.member.controller;


import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.member.controller.dto.request.LoginRequest;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberCreateRequest;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberUpdateRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberCreateResponse;
import com.cpa.yusin.quiz.member.controller.port.AuthenticationService;
import com.cpa.yusin.quiz.member.controller.port.MemberService;
import com.cpa.yusin.quiz.member.domain.MemberDomain;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(TeardownExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureRestDocs
class MemberTest
{
    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthenticationService authenticationService;


    ObjectMapper om = new ObjectMapper();

    long memberId;

    MemberDomain member;

    @BeforeEach
    void setUp()
    {
        MemberCreateResponse response = authenticationService.signUp(MemberCreateRequest.builder()
                .email("David@naver.com").password("12341234").username("David").build());

        memberId = response.getId();

        authenticationService.signUp(MemberCreateRequest.builder()
                .email("Mike@gmail.com").password("12341234").username("Mike").build());

        authenticationService.signUp(MemberCreateRequest.builder()
                .email("Gale@github.com").password("12341234").username("Gale").build());


        member = memberRepository.save(MemberDomain.builder()
                .email("John@gmail.com")
                .password("12341234")
                .username("John")
                .platform(Platform.HOME)
                .role(Role.ADMIN)
                .build());
    }


    @Test
    void signUp() throws Exception
    {
        // given
        MemberCreateRequest request = MemberCreateRequest.builder()
                .username("Lee")
                .email("test1@naver.com")
                .password("123123")
                .build();

        String requestBody = om.writeValueAsString(request);

        // when
        ResultActions resultActions = mvc
                .perform(post("/api/v1/sign-up")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isCreated())
                .andDo(document("회원가입",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("비밀번호"),
                                fieldWithPath("username").description("회원 이름")
                        ),
                        responseFields(
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("회원 고유 식별자"),
                                fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("회원 이름"),
                                fieldWithPath("data.platform").type(JsonFieldType.STRING).description("가입 경로"),
                                fieldWithPath("data.role").type(JsonFieldType.STRING).description("권한"),

                                fieldWithPath("pageInfo").type(JsonFieldType.NUMBER).description("페이지 정보").optional()
                        ))
                );
    }

    @Test
    void signUpWhenEmailIsNotValid() throws Exception
    {
        // given
        MemberCreateRequest request = MemberCreateRequest.builder()
                //.username("Lee")
                .email("test1")
                //.password("123123")
                .build();

        String requestBody = om.writeValueAsString(request);

        // when
        ResultActions resultActions = mvc
                .perform(post("/api/v1/sign-up")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andDo(document("회원가입 - 이메일 형식 오류",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("비밀번호"),
                                fieldWithPath("username").description("회원 이름")
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

    @Test
    void signUpWhenEmailAlreadyExists() throws Exception
    {
        // given
        MemberCreateRequest request = MemberCreateRequest.builder()
                .username("Lee")
                .email("Mike@gmail.com")
                .password("123123")
                .build();

        String requestBody = om.writeValueAsString(request);

        // when
        ResultActions resultActions = mvc
                .perform(post("/api/v1/sign-up")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isInternalServerError())
                .andDo(document("회원가입 - 이메일 중복",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("비밀번호"),
                                fieldWithPath("username").description("회원 이름")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메세지")
                        ))
                );
    }


    @Test
    void login_success() throws Exception
    {
        // given
        String email = "David@naver.com";
        String password = "12341234";

        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        String requestBody = om.writeValueAsString(request);

        // when
        ResultActions resultActions = mvc
                .perform(post("/api/v1/login")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("로그인 - 성공",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("비밀번호")
                        ),

                        responseFields(
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("회원 고유 식별자"),
                                fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("액세스 토큰"),

                                fieldWithPath("pageInfo").type(JsonFieldType.NUMBER).description("페이지 정보").optional()
                        )
                ));

    }

    @Test
    void login_invalidField() throws Exception
    {
        // given
        String email = "David@naver.com";
        String password = "123412";

        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        String requestBody = om.writeValueAsString(request);

        // when
        ResultActions resultActions = mvc
                .perform(post("/api/v1/login")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isInternalServerError())
                .andDo(document("로그인 - 비밀번호 오류",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("비밀번호")
                        ),

                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메세지")
                        )
                ));

    }

    @Test
    void login_notExistingUser() throws Exception
    {
        // given
        String email = "David@naver.com222";
        String password = "123412";

        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        String requestBody = om.writeValueAsString(request);

        // when
        ResultActions resultActions = mvc
                .perform(post("/api/v1/login")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isInternalServerError())
                .andDo(document("로그인 - 존재하지 않는 유저",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("password").description("비밀번호")
                        ),

                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메세지")
                        )
                ));

    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void update() throws Exception
    {
        // given
        MemberUpdateRequest request = MemberUpdateRequest.builder()
                .username("GeonMo")
                .build();


        String requestBody = om.writeValueAsString(request);

        MemberDetails memberDetails = new MemberDetails(member, new HashMap<>());

        // when
        ResultActions resultActions = mvc
                .perform(patch("/api/v1/admin/members/" + member.getId())
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(memberDetails))
                );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("회원 수정 - 성공",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        requestFields(
                                fieldWithPath("username").description("유저 이름")
                        ),

                        responseFields(
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("회원 고유 식별자"),
                                fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("유저 이름"),
                                fieldWithPath("data.platform").type(JsonFieldType.STRING).description("회원가입 경로"),
                                fieldWithPath("data.role").type(JsonFieldType.STRING).description("유저 권한"),
                                fieldWithPath("data.subscriberExpiredAt").type(JsonFieldType.STRING).description("구독 만료일").optional(),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성 날짜"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정 날짜"),


                                fieldWithPath("pageInfo").type(JsonFieldType.NUMBER).description("페이지 정보").optional()
                        )

                ))

        ;



    }

}
