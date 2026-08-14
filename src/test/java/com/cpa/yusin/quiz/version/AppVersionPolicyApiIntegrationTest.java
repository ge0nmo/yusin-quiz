package com.cpa.yusin.quiz.version;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.cpa.yusin.quiz.file.controller.port.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.version-policy.qualification-exams.APPRAISER.ANDROID.latest-version=2.0.0",
        "app.version-policy.qualification-exams.APPRAISER.ANDROID.minimum-version=2.0.0",
        "app.version-policy.qualification-exams.APPRAISER.ANDROID.store-url=https://play.google.com/store/apps/details?id=com.yusin.quiz",
        "app.version-policy.qualification-exams.APPRAISER.IOS.latest-version=2.0.0",
        "app.version-policy.qualification-exams.APPRAISER.IOS.minimum-version=0.0.0",
        "app.version-policy.qualification-exams.APPRAISER.IOS.store-url="
})
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
class AppVersionPolicyApiIntegrationTest {
    @Autowired WebApplicationContext context;
    @MockBean FileService fileService;

    @Test
    void returnsConfiguredAndroidPolicy(RestDocumentationContextProvider restDocumentation) throws Exception {
        MockMvc mockMvc = mockMvc(restDocumentation);

        mockMvc.perform(get("/api/v1/qualification-exams/{code}/app-version-policy", "APPRAISER")
                        .param("platform", "android"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.latestVersion").value("2.0.0"))
                .andExpect(jsonPath("$.data.minimumVersion").value("2.0.0"))
                .andExpect(jsonPath("$.data.storeUrl")
                        .value("https://play.google.com/store/apps/details?id=com.yusin.quiz"))
                .andDo(document("public-app-version-policy",
                        resource(ResourceSnippetParameters.builder()
                                .tag("Public App Version")
                                .summary("앱 버전 정책 조회")
                                .build())));
    }

    @Test
    void unreleasedIosPolicyDoesNotForceAnUpdate(RestDocumentationContextProvider restDocumentation) throws Exception {
        MockMvc mockMvc = mockMvc(restDocumentation);

        mockMvc.perform(get("/api/v1/qualification-exams/{code}/app-version-policy", "APPRAISER")
                        .param("platform", "ios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.latestVersion").value("2.0.0"))
                .andExpect(jsonPath("$.data.minimumVersion").value("0.0.0"))
                .andExpect(jsonPath("$.data.storeUrl").value(""));
    }

    @Test
    void rejectsUnsupportedPlatform(RestDocumentationContextProvider restDocumentation) throws Exception {
        mockMvc(restDocumentation)
                .perform(get("/api/v1/qualification-exams/{code}/app-version-policy", "APPRAISER")
                        .param("platform", "windows"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DATA"));
    }

    @Test
    void rejectsUnknownQualificationCode(RestDocumentationContextProvider restDocumentation) throws Exception {
        mockMvc(restDocumentation)
                .perform(get("/api/v1/qualification-exams/{code}/app-version-policy", "LAWYER")
                        .param("platform", "android"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("QUALIFICATION_EXAM_NOT_FOUND"));
    }

    private MockMvc mockMvc(RestDocumentationContextProvider restDocumentation) {
        return MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
    }
}
