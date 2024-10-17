package com.cpa.yusin.quiz.subscription.integration;

import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.payment.domain.Payment;
import com.cpa.yusin.quiz.payment.domain.type.PaymentStatus;
import com.cpa.yusin.quiz.payment.service.port.PaymentRepository;
import com.cpa.yusin.quiz.subject.controller.dto.request.SubjectCreateRequest;
import com.cpa.yusin.quiz.subscription.domain.Subscription;
import com.cpa.yusin.quiz.subscription.domain.type.SubscriptionStatus;
import com.cpa.yusin.quiz.subscription.service.port.SubscriptionRepository;
import com.cpa.yusin.quiz.subscriptionPlan.domain.SubscriptionPlan;
import com.cpa.yusin.quiz.subscriptionPlan.service.port.SubscriptionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, TeardownExtension.class})
@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureRestDocs
public class SubscriptionTest
{
    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Autowired
    PaymentRepository paymentRepository;

    ObjectMapper objectMapper;

    Member member;
    SubscriptionPlan plan;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation)
    {
        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();

        member = memberRepository.save(Member.builder()
                .id(1L)
                .email("member@gmail.com")
                .username("member")
                .password("12341234")
                .platform(Platform.HOME)
                .role(Role.USER)
                .build());

        plan = subscriptionPlanRepository.save(SubscriptionPlan.builder()
                .id(1L)
                .name("1개월 구독")
                .price(BigDecimal.valueOf(1000))
                .durationMonth(1)
                .build());

        Payment payment1 = paymentRepository.save(Payment.builder()
                .id(1L)
                .amount(plan.getPrice())
                .paidAmount(plan.getPrice())
                .status(PaymentStatus.COMPLETED)
                .merchantUid("PID-aaaa")
                .portOnePaymentId(UUID.randomUUID().toString())
                .failureReason("")
                .build());

        Payment payment2 = paymentRepository.save(Payment.builder()
                .id(2L)
                .amount(plan.getPrice())
                .paidAmount(plan.getPrice())
                .status(PaymentStatus.COMPLETED)
                .merchantUid("PID-bbbb")
                .portOnePaymentId(UUID.randomUUID().toString())
                .failureReason("")
                .build());

        Payment payment3 = paymentRepository.save(Payment.builder()
                .id(3L)
                .amount(plan.getPrice())
                .paidAmount(plan.getPrice())
                .status(PaymentStatus.COMPLETED)
                .merchantUid("PID-cccc")
                .portOnePaymentId(UUID.randomUUID().toString())
                .failureReason("")
                .build());


        subscriptionRepository.save(Subscription.builder()
                .id(1L)
                .status(SubscriptionStatus.EXPIRED)
                .startDate(LocalDateTime.now().minusMonths(3))
                .member(member)
                .payment(payment1)
                .plan(plan)
                .expiredDate(LocalDateTime.now().minusMonths(2))
                .build());

        subscriptionRepository.save(Subscription.builder()
                .id(2L)
                .status(SubscriptionStatus.EXPIRED)
                .startDate(LocalDateTime.now().minusMonths(2))
                .member(member)
                .payment(payment2)
                .plan(plan)
                .expiredDate(LocalDateTime.now().minusMonths(1))
                .build());

        subscriptionRepository.save(Subscription.builder()
                .id(3L)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now().minusMonths(1))
                .member(member)
                .payment(payment3)
                .plan(plan)
                .expiredDate(LocalDateTime.now().plusDays(3))
                .build());


        objectMapper = new ObjectMapper();
    }


    @WithUserDetails(value = "member@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getSubscriptionHistory() throws Exception
    {
        // given
        MemberDetails memberDetails = new MemberDetails(member, new HashMap<>());
        int page = 1;
        int size = 10;

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/subscription/history")
                        .with(user(memberDetails))
                        .queryParam("page", Integer.toString(page))
                        .queryParam("size", Integer.toString(size))
                );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("subscriptionHistory",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 크기")
                        ),

                        responseFields(
                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("구독 ID"),
                                fieldWithPath("data[].status").type(JsonFieldType.STRING).description("구독 상태"),
                                fieldWithPath("data[].startDate").type(JsonFieldType.STRING).description("구독 시작 날짜"),
                                fieldWithPath("data[].expiredDate").type(JsonFieldType.STRING).description("구독 만료 날짜"),

                                fieldWithPath("data[].subscriptionPlan.id").type(JsonFieldType.NUMBER).description("구독 플랜 ID"),
                                fieldWithPath("data[].subscriptionPlan.name").type(JsonFieldType.STRING).description("구독 플랜 이름"),
                                fieldWithPath("data[].subscriptionPlan.durationMonth").type(JsonFieldType.NUMBER).description("구독 기간(개월)"),
                                fieldWithPath("data[].subscriptionPlan.price").type(JsonFieldType.NUMBER).description("구독 가격"),

                                fieldWithPath("data[].payment.id").type(JsonFieldType.NUMBER).description("결제 ID"),
                                fieldWithPath("data[].payment.amount").type(JsonFieldType.NUMBER).description("결제 금액"),
                                fieldWithPath("data[].payment.status").type(JsonFieldType.STRING).description("결제 상태"),
                                fieldWithPath("data[].payment.portOnePaymentId").type(JsonFieldType.STRING).description("Port One 결제 ID"),
                                fieldWithPath("data[].payment.merchantUid").type(JsonFieldType.STRING).description("상점 결제 고유 식별자"),
                                fieldWithPath("data[].payment.paymentProvider").type(JsonFieldType.STRING).description("결제 제공자").optional(),
                                fieldWithPath("data[].payment.failureReason").type(JsonFieldType.STRING).description("결제 실패 사유").optional(),

                                fieldWithPath("pageInfo").type(JsonFieldType.OBJECT).description("페이지 정보").optional(),
                                fieldWithPath("pageInfo.totalElements").type(JsonFieldType.NUMBER).description("총 데이터 수").optional(),
                                fieldWithPath("pageInfo.totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수").optional(),
                                fieldWithPath("pageInfo.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지").optional(),
                                fieldWithPath("pageInfo.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기").optional()
                        )
                ))
        ;
    }


}
