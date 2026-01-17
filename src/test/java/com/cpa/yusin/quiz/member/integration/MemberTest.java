package com.cpa.yusin.quiz.member.integration;


import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.member.controller.dto.request.LoginRequest;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberCreateRequest;
import com.cpa.yusin.quiz.member.controller.dto.request.MemberUpdateRequest;
import com.cpa.yusin.quiz.member.controller.dto.response.MemberCreateResponse;
import com.cpa.yusin.quiz.member.controller.port.AuthenticationService;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
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

import java.util.HashMap;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, TeardownExtension.class})
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

    Member member;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation)
    {
        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();

        MemberCreateResponse savedMember = authenticationService.signUp(MemberCreateRequest.builder()
                .email("David@naver.com")
                .password("12341234")
                .username("David")
                .build());


        memberId = savedMember.getId();

        memberRepository.save(Member.builder()
                .id(2L)
                .email("Mike@gmail.com")
                .password("12341234")
                .username("Mike")
                .role(Role.ADMIN)
                .platform(Platform.HOME)
                .build());

        memberRepository.save(Member.builder()
                .id(3L)
                .email("Gale@github.com")
                .password("12341234")
                .username("Gale")
                .role(Role.ADMIN)
                .platform(Platform.HOME)
                .build());

        member = memberRepository.save(Member.builder()
                .id(4L)
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
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("회원 ID"),
                                fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("회원 이름"),
                                fieldWithPath("data.platform").type(JsonFieldType.STRING).description("가입 경로"),
                                fieldWithPath("data.role").type(JsonFieldType.STRING).description("권한"),

                                fieldWithPath("pageInfo").type(JsonFieldType.OBJECT).description("페이지 정보").optional()
                        ))
                );
    }

    @Test
    void signUpWhenEmailIsNotValid() throws Exception
    {
        // given
        MemberCreateRequest request = MemberCreateRequest.builder()
                .username("Lee")
                .email("test1")
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
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("회원 ID"),
                                fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("data.role").type(JsonFieldType.STRING).description("회원 권한"),
                                fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("액세스 토큰"),
                                fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰"),

                                fieldWithPath("pageInfo").type(JsonFieldType.OBJECT).description("페이지 정보").optional()
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
    void update_success() throws Exception
    {
        // given
        MemberUpdateRequest request = MemberUpdateRequest.builder()
                .username("GeonMo")
                .build();


        String requestBody = om.writeValueAsString(request);

        MemberDetails memberDetails = new MemberDetails(member, new HashMap<>());

        // when
        ResultActions resultActions = mvc
                .perform(patch("/api/v1/members/" + member.getId())
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
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("회원 ID"),
                                fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("유저 이름"),
                                fieldWithPath("data.platform").type(JsonFieldType.STRING).description("회원가입 경로"),
                                fieldWithPath("data.role").type(JsonFieldType.STRING).description("유저 권한"),
                                fieldWithPath("data.subscriberExpiredAt").type(JsonFieldType.STRING).description("구독 만료일").optional(),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성 날짜"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정 날짜"),

                                fieldWithPath("pageInfo").type(JsonFieldType.OBJECT).description("페이지 정보").optional()
                        )
                ))
        ;

    }


    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getMemberById() throws Exception
    {
        // given
        MemberDetails memberDetails = new MemberDetails(member, new HashMap<>());

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/members/" + 1)
                        .with(user(memberDetails))
                );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("회원 1명 조회",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),


                        responseFields(
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("회원 ID"),
                                fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("data.username").type(JsonFieldType.STRING).description("유저 이름"),
                                fieldWithPath("data.platform").type(JsonFieldType.STRING).description("회원가입 경로"),
                                fieldWithPath("data.role").type(JsonFieldType.STRING).description("유저 권한"),
                                fieldWithPath("data.subscriberExpiredAt").type(JsonFieldType.STRING).description("구독 만료일").optional(),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성 날짜"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정 날짜"),


                                fieldWithPath("pageInfo").type(JsonFieldType.OBJECT).description("페이지 정보").optional()
                        )
                ))

        ;

    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getMembers_noKeyword() throws Exception
    {
        // given
        MemberDetails memberDetails = new MemberDetails(member, new HashMap<>());

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/members")
                        .with(user(memberDetails))
                );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("회원 전체 조회",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),


                        responseFields(
                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("회원 ID"),
                                fieldWithPath("data[].email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("data[].username").type(JsonFieldType.STRING).description("유저 이름"),
                                fieldWithPath("data[].platform").type(JsonFieldType.STRING).description("회원가입 경로"),
                                fieldWithPath("data[].role").type(JsonFieldType.STRING).description("유저 권한"),
                                fieldWithPath("data[].subscriberExpiredAt").type(JsonFieldType.STRING).description("구독 만료일").optional(),
                                fieldWithPath("data[].createdAt").type(JsonFieldType.STRING).description("생성 날짜"),
                                fieldWithPath("data[].updatedAt").type(JsonFieldType.STRING).description("수정 날짜"),


                                fieldWithPath("pageInfo").type(JsonFieldType.OBJECT).description("페이지 정보").optional(),
                                fieldWithPath("pageInfo.totalElements").type(JsonFieldType.NUMBER).description("총 데이터 수").optional(),
                                fieldWithPath("pageInfo.totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수").optional(),
                                fieldWithPath("pageInfo.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지").optional(),
                                fieldWithPath("pageInfo.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기").optional()
                        )
                ));
    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getMembers_keyword() throws Exception
    {
        // given
        MemberDetails memberDetails = new MemberDetails(member, new HashMap<>());
        String keyword = "mike";
        int page = 1;
        int size = 10;

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/members")
                        .queryParam("keyword", keyword)
                        .queryParam("page", Integer.toString(page))
                        .queryParam("size", Integer.toString(size))
                        .with(user(memberDetails))
                );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("회원 전체 조회 - keyword",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 크기"),
                                parameterWithName("keyword").description("검색 키워드").optional()
                        ),


                        responseFields(
                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("회원 ID"),
                                fieldWithPath("data[].email").type(JsonFieldType.STRING).description("이메일"),
                                fieldWithPath("data[].username").type(JsonFieldType.STRING).description("유저 이름"),
                                fieldWithPath("data[].platform").type(JsonFieldType.STRING).description("회원가입 경로"),
                                fieldWithPath("data[].role").type(JsonFieldType.STRING).description("유저 권한"),
                                fieldWithPath("data[].subscriberExpiredAt").type(JsonFieldType.STRING).description("구독 만료일").optional(),
                                fieldWithPath("data[].createdAt").type(JsonFieldType.STRING).description("생성 날짜"),
                                fieldWithPath("data[].updatedAt").type(JsonFieldType.STRING).description("수정 날짜"),


                                fieldWithPath("pageInfo").type(JsonFieldType.OBJECT).description("페이지 정보").optional(),
                                fieldWithPath("pageInfo.totalElements").type(JsonFieldType.NUMBER).description("총 데이터 수").optional(),
                                fieldWithPath("pageInfo.totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수").optional(),
                                fieldWithPath("pageInfo.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지").optional(),
                                fieldWithPath("pageInfo.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기").optional()
                        )
                ));
    }


    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void deleteById_success() throws Exception
    {
        // given
        MemberDetails memberDetails = new MemberDetails(member, new HashMap<>());

        // when
        ResultActions resultActions = mvc
                .perform(delete("/api/v1/members/" + 2)
                );

        // then
        resultActions
                .andExpect(status().isNoContent())
                .andDo(document("회원 삭제",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())

                ))
        ;

    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void deleteById_notExistingMember() throws Exception
    {
        // given
        MemberDetails memberDetails = new MemberDetails(member, new HashMap<>());

        // when
        ResultActions resultActions = mvc
                .perform(delete("/api/v1/members/" + 20)
                        .with(user(memberDetails))
                );

        // then
        resultActions
                .andExpect(status().isInternalServerError())
                .andDo(document("회원 삭제 - 실패",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메세지")
                        )
                ))
        ;

    }

}
