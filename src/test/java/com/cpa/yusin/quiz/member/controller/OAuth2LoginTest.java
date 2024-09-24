package com.cpa.yusin.quiz.member.controller;

import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.global.security.oauth2.CustomOAuth2Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(TeardownExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureRestDocs
class OAuth2LoginTest
{
    @Autowired
    MockMvc mvc;

    @MockBean
    CustomOAuth2Service customOAuth2Service;

    @Test
    void whenUnauthorized_thenRedirectToLogin() throws Exception
    {
        // given
        //when
        ResultActions resultActions =
                mvc.perform(MockMvcRequestBuilders.get("/api/v1/member/subject/" + 1L));

        // then
        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"))
                .andDo(document("oauth2Login",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        responseHeaders(
                                headerWithName("Location").description("Redirect URI")
                        )

                ))
        ;
    }

}
