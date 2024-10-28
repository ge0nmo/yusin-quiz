package com.cpa.yusin.quiz.subscriptionPlan.integration;

import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.request.SubscriptionPlanRegisterRequest;
import com.cpa.yusin.quiz.subscriptionPlan.controller.dto.request.SubscriptionPlanUpdateRequest;
import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import com.cpa.yusin.quiz.subscriptionPlan.service.port.SubscriptionPlanRepository;
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

import java.math.BigDecimal;

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
class SubscriptionPlanIntegrationTest
{
    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    MemberRepository memberRepository;

    ObjectMapper objectMapper;

    MockMvc mvc;

    Member admin;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation)
    {
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

        objectMapper = new ObjectMapper();
    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void save() throws Exception
    {
        // given
        SubscriptionPlanRegisterRequest request = SubscriptionPlanRegisterRequest.builder()
                .name("1개월 플랜")
                .price(BigDecimal.valueOf(3000))
                .durationMonth(1)
                .build();

        // when
        ResultActions resultActions = mvc.perform(post("/api/v1/admin/plan")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isCreated())
                .andDo(document("savePlanSuccess",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description("구독 플랜 이름"),
                                fieldWithPath("durationMonth").description("구독 플랜 기간"),
                                fieldWithPath("price").description("구독 플랜 가격")
                        ),

                        responseFields(
                                fieldWithPath("data.id").description("구독 플랜 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.name").description("구독 플랜 이름").type(JsonFieldType.STRING),
                                fieldWithPath("data.durationMonth").description("구독 플랜 기간").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.price").description("구독 플랜 가격").type(JsonFieldType.NUMBER)
                        )
                ))
        ;
    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void save_throwErrorWhenFieldsAreNull() throws Exception
    {
        // given
        SubscriptionPlanRegisterRequest request = SubscriptionPlanRegisterRequest.builder()
                .build();

        // when
        ResultActions resultActions = mvc.perform(post("/api/v1/admin/plan")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andDo(document("savePlanNullValue",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description("구독 플랜 이름"),
                                fieldWithPath("durationMonth").description("구독 플랜 기간"),
                                fieldWithPath("price").description("구독 플랜 가격")
                        ),

                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메세지"),
                                fieldWithPath("valueErrors[].descriptor").type(JsonFieldType.STRING).description("오류 항목"),
                                fieldWithPath("valueErrors[].rejectedValue").type(JsonFieldType.STRING).description("오류 내용"),
                                fieldWithPath("valueErrors[].reason").type(JsonFieldType.STRING).description("오류 원인")
                        ))
                )
        ;
    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void update() throws Exception
    {
        // given
        subscriptionPlanRepository.save(
                SubscriptionPlan.builder()
                        .id(1L).name("1개월 플랜")
                        .price(BigDecimal.valueOf(3000))
                        .durationMonth(1)
                        .build());

        long productId = 1L;

        SubscriptionPlanUpdateRequest request = SubscriptionPlanUpdateRequest.builder()
                .name("3개월 플랜")
                .durationMonth(3)
                .price(BigDecimal.valueOf(5000))
                .build();

        // when
        ResultActions resultActions = mvc.perform(patch("/api/v1/admin/plan/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("updateProductSuccess",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("name").description("구독 플랜 이름"),
                                fieldWithPath("durationMonth").description("구독 플랜 기간"),
                                fieldWithPath("price").description("구독 플랜 가격")
                        ),

                        responseFields(
                                fieldWithPath("data.id").description("구독 플랜 고유 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.name").description("구독 플랜 이름").type(JsonFieldType.STRING),
                                fieldWithPath("data.durationMonth").description("구독 플랜 기간").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.price").description("구독 플랜 가격").type(JsonFieldType.NUMBER)
                        )
                ))
        ;
    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void update_throwExceptionWhenFieldsAreNull() throws Exception
    {
        // given
        SubscriptionPlan savedSubscriptionPlan = subscriptionPlanRepository.save(
                SubscriptionPlan.builder()
                        .id(1L)
                        .name("1개월 플랜")
                        .price(BigDecimal.valueOf(3000))
                        .durationMonth(1)
                        .build());

        SubscriptionPlanUpdateRequest request = SubscriptionPlanUpdateRequest.builder()
                .build();

        // when
        ResultActions resultActions = mvc.perform(patch("/api/v1/admin/plan/" + savedSubscriptionPlan.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andDo(document("updatePlanNullValue",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("durationMonth").description("구독 플랜 기간"),
                                fieldWithPath("name").description("구독 플랜 이름"),
                                fieldWithPath("price").description("구독 플랜 가격")
                        ),

                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("상태"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("메세지"),
                                fieldWithPath("valueErrors[].descriptor").type(JsonFieldType.STRING).description("오류 항목"),
                                fieldWithPath("valueErrors[].rejectedValue").type(JsonFieldType.STRING).description("오류 내용"),
                                fieldWithPath("valueErrors[].reason").type(JsonFieldType.STRING).description("오류 원인")
                        )
                ))
        ;
    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getById() throws Exception
    {
        // given
        SubscriptionPlan savedSubscriptionPlan = subscriptionPlanRepository.save(
                SubscriptionPlan.builder()
                        .id(1L)
                        .name("1개월 플랜")
                        .price(BigDecimal.valueOf(3000))
                        .durationMonth(1)
                        .build());

        // when
        ResultActions resultActions = mvc.perform(get("/api/v1/plan/" + savedSubscriptionPlan.getId())
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("getPlanById",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        responseFields(
                                fieldWithPath("data.id").description("구독 플랜 고유 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.name").description("구독 플랜 이름").type(JsonFieldType.STRING),
                                fieldWithPath("data.durationMonth").description("구독 플랜 기간").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.price").description("구독 플랜 가격").type(JsonFieldType.NUMBER)
                        )
                ))
        ;
    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getAllProducts() throws Exception
    {
        // given
        subscriptionPlanRepository.save(SubscriptionPlan.builder().id(1L).name("1개월 플랜").price(BigDecimal.valueOf(3000)).durationMonth(1).build());
        subscriptionPlanRepository.save(SubscriptionPlan.builder().id(2L).name("3개월 플랜").price(BigDecimal.valueOf(6000)).durationMonth(3).build());
        subscriptionPlanRepository.save(SubscriptionPlan.builder().id(3L).name("6개월 플랜").price(BigDecimal.valueOf(10000)).durationMonth(6).build());
        subscriptionPlanRepository.save(SubscriptionPlan.builder().id(4L).name("1년 플랜").price(BigDecimal.valueOf(15000)).durationMonth(12).build());

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/plan"));

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("getAllSubscriptionPlans",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        responseFields(
                                fieldWithPath("data[].id").description("구독 플랜 고유 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].name").description("구독 플랜 이름").type(JsonFieldType.STRING),
                                fieldWithPath("data[].durationMonth").description("구독 플랜 기간").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].price").description("구독 플랜 가격").type(JsonFieldType.NUMBER)
                        )
                ))
        ;
    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void deleteById() throws Exception
    {
        // given
        SubscriptionPlan savedSubscriptionPlan = subscriptionPlanRepository.save(
                SubscriptionPlan.builder()
                        .id(1L)
                        .price(BigDecimal.valueOf(3000))
                        .name("1개월 플랜")
                        .durationMonth(1)
                        .build());

        // when
        ResultActions resultActions = mvc
                .perform(delete("/api/v1/admin/plan/" + savedSubscriptionPlan.getId()));

        // then
        resultActions
                .andExpect(status().isNoContent())
                .andDo(document("deletePlanById",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

    }
}