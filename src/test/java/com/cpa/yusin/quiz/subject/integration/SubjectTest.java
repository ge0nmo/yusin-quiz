package com.cpa.yusin.quiz.subject.integration;

import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectUpdateRequest;
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

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, TeardownExtension.class})
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

    ObjectMapper objectMapper;

    Member admin;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation)
    {
        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();

        admin = memberRepository.save(Member.builder()
                .id(1L)
                .email("admin@gmail.com")
                .username("admin")
                .password("12341234")
                .platform(Platform.HOME)
                .role(Role.ADMIN)
                .build());

        objectMapper = new ObjectMapper();
    }


    @WithUserDetails(value = "admin@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getById_success() throws Exception
    {
        // given
        Subject subject = subjectRepository.save(Subject.builder()
                .id(1L)
                .name("회계학")
                .build());


        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/subject/" + subject.getId()));

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("과목 1개 조회",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        responseFields(
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("과목 ID"),
                                fieldWithPath("data.name").type(JsonFieldType.STRING).description("과목 이름")
                        )
                ));
    }

    @WithUserDetails(value = "admin@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getSubjects() throws Exception
    {
        // given
        subjectRepository.save(Subject.builder().id(1L).name("회계학").build());
        subjectRepository.save(Subject.builder().id(2L).name("경제학").build());
        subjectRepository.save(Subject.builder().id(3L).name("세법").build());
        subjectRepository.save(Subject.builder().id(4L).name("경영학").build());
        subjectRepository.save(Subject.builder().id(5L).name("상법").build());
        int page = 1;
        int size = 10;


        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/subject")
                        .queryParam("page", Integer.toString(page))
                        .queryParam("size", Integer.toString(size))

                );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("과목 전체 조회",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 크기")
                        ),

                        responseFields(
                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("과목 ID"),
                                fieldWithPath("data[].name").type(JsonFieldType.STRING).description("과목 이름"),

                                fieldWithPath("pageInfo").type(JsonFieldType.OBJECT).description("페이지 정보").optional(),
                                fieldWithPath("pageInfo.totalElements").type(JsonFieldType.NUMBER).description("총 데이터 수").optional(),
                                fieldWithPath("pageInfo.totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수").optional(),
                                fieldWithPath("pageInfo.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지").optional(),
                                fieldWithPath("pageInfo.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기").optional()
                        )
                ));
    }

}
