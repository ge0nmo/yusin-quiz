package com.cpa.yusin.quiz.product.integration;

import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.product.controller.dto.request.ProductRegisterRequest;
import com.cpa.yusin.quiz.product.controller.dto.request.ProductUpdateRequest;
import com.cpa.yusin.quiz.product.domain.Product;
import com.cpa.yusin.quiz.product.service.port.ProductRepository;
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
class ProductIntegrationTest
{
    @Autowired
    private ProductRepository productRepository;

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
        ProductRegisterRequest request = ProductRegisterRequest.builder()
                .price(BigDecimal.valueOf(3000))
                .durationMonth(1)
                .build();

        // when
        ResultActions resultActions = mvc.perform(post("/api/v1/admin/product")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isCreated())
                .andDo(document("saveProductSuccess",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("durationMonth").description("상품 기간"),
                                fieldWithPath("price").description("상품 가격")
                        ),

                        responseFields(
                                fieldWithPath("data.id").description("상품 고유 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.durationMonth").description("상품 기간").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.price").description("상품 가격").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.createdAt").description("상품 등록 날짜").type(JsonFieldType.STRING),
                                fieldWithPath("data.updatedAt").description("상품 수정 날짜").type(JsonFieldType.STRING)
                        )
                ))
        ;
    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void save_throwErrorWhenFieldsAreNull() throws Exception
    {
        // given
        ProductRegisterRequest request = ProductRegisterRequest.builder()
                .build();

        // when
        ResultActions resultActions = mvc.perform(post("/api/v1/admin/product")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andDo(document("saveProductNullValue",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("durationMonth").description("상품 기간"),
                                fieldWithPath("price").description("상품 가격")
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
        productRepository.save(Product.builder().id(1L).price(BigDecimal.valueOf(3000)).durationMonth(1).build());

        long productId = 1L;

        ProductUpdateRequest request = ProductUpdateRequest.builder()
                .durationMonth(3)
                .price(BigDecimal.valueOf(5000))
                .build();

        // when
        ResultActions resultActions = mvc.perform(patch("/api/v1/admin/product/" + productId)
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
                                fieldWithPath("durationMonth").description("상품 기간"),
                                fieldWithPath("price").description("상품 가격")
                        ),

                        responseFields(
                                fieldWithPath("data.id").description("상품 고유 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.durationMonth").description("상품 기간").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.price").description("상품 가격").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.createdAt").description("상품 등록 날짜").type(JsonFieldType.STRING),
                                fieldWithPath("data.updatedAt").description("상품 수정 날짜").type(JsonFieldType.STRING)
                        )
                ))
        ;
    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void update_throwExceptionWhenFieldsAreNull() throws Exception
    {
        // given
        Product savedProduct = productRepository.save(Product.builder().id(1L).price(BigDecimal.valueOf(3000)).durationMonth(1).build());

        ProductUpdateRequest request = ProductUpdateRequest.builder()
                .build();

        // when
        ResultActions resultActions = mvc.perform(patch("/api/v1/admin/product/" + savedProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andDo(document("updateProductNullValue",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("durationMonth").description("상품 기간"),
                                fieldWithPath("price").description("상품 가격")
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
        Product savedProduct = productRepository.save(Product.builder().id(1L).price(BigDecimal.valueOf(3000)).durationMonth(1).build());

        // when
        ResultActions resultActions = mvc.perform(get("/api/v1/admin/product/" + savedProduct.getId())
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("getProductById",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        responseFields(
                                fieldWithPath("data.id").description("상품 고유 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.durationMonth").description("상품 기간").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.price").description("상품 가격").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.createdAt").description("상품 등록 날짜").type(JsonFieldType.STRING),
                                fieldWithPath("data.updatedAt").description("상품 수정 날짜").type(JsonFieldType.STRING)
                        )
                ))
        ;
    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getAllProducts() throws Exception
    {
        // given
        productRepository.save(Product.builder().id(1L).price(BigDecimal.valueOf(3000)).durationMonth(1).build());
        productRepository.save(Product.builder().id(2L).price(BigDecimal.valueOf(6000)).durationMonth(3).build());
        productRepository.save(Product.builder().id(3L).price(BigDecimal.valueOf(10000)).durationMonth(6).build());
        productRepository.save(Product.builder().id(4L).price(BigDecimal.valueOf(15000)).durationMonth(12).build());

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/admin/product"));

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("getAllProducts",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        responseFields(
                                fieldWithPath("data[].id").description("상품 고유 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].durationMonth").description("상품 기간").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].price").description("상품 가격").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].createdAt").description("상품 등록 날짜").type(JsonFieldType.STRING),
                                fieldWithPath("data[].updatedAt").description("상품 수정 날짜").type(JsonFieldType.STRING)
                        )
                ))
        ;
    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void deleteById() throws Exception
    {
        // given
        Product savedProduct = productRepository.save(Product.builder().id(1L).price(BigDecimal.valueOf(3000)).durationMonth(1).build());

        // when
        ResultActions resultActions = mvc
                .perform(delete("/api/v1/admin/product/" + savedProduct.getId()));

        // then
        resultActions
                .andExpect(status().isNoContent())
                .andDo(document("deleteProductById",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));

    }
}