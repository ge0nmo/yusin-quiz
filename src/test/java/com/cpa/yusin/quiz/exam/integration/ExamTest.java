package com.cpa.yusin.quiz.exam.integration;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.domain.ExamStatus;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
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

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({ RestDocumentationExtension.class, TeardownExtension.class })
@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureRestDocs
class ExamTest {
        @Autowired
        private MockMvc mvc;

        @Autowired
        private SubjectRepository subjectRepository;

        @Autowired
        private ExamRepository examRepository;

        @Autowired
        private ProblemRepository problemRepository;

        @Autowired
        private MemberRepository memberRepository;

        Subject economics;

        Member admin;

        @BeforeEach
        void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
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

                economics = subjectRepository.save(Subject.builder()
                                .id(1L)
                                .name("경제학")
                                .build());
        }

        @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        @Test
        void getBySubjectIdAndYear_success() throws Exception {
                // given
                Exam firstExam = examRepository.save(Exam.builder()
                                .id(1L)
                                .name("1차")
                                .year(2024)
                                .subjectId(economics.getId())
                                .status(ExamStatus.PUBLISHED)
                                .build());

                Exam secondExam = examRepository.save(Exam.builder()
                                .id(2L)
                                .name("2차")
                                .year(2024)
                                .subjectId(economics.getId())
                                .status(ExamStatus.PUBLISHED)
                                .build());

                examRepository.save(Exam.builder()
                                .id(3L)
                                .name("3차")
                                .year(2024)
                                .subjectId(economics.getId())
                                .status(ExamStatus.PUBLISHED)
                                .build());

                examRepository.save(Exam.builder()
                                .id(4L)
                                .name("숨김 시험")
                                .year(2024)
                                .subjectId(economics.getId())
                                .status(ExamStatus.DRAFT)
                                .build());

                problemRepository.save(Problem.builder()
                                .id(10L)
                                .number(1)
                                .content("활성 문제")
                                .explanation("해설")
                                .exam(firstExam)
                                .build());
                Problem removedProblem = problemRepository.save(Problem.builder()
                                .id(11L)
                                .number(2)
                                .content("삭제 문제")
                                .explanation("해설")
                                .exam(firstExam)
                                .build());
                removedProblem.delete();
                problemRepository.save(removedProblem);
                problemRepository.save(Problem.builder()
                                .id(12L)
                                .number(1)
                                .content("활성 문제 2")
                                .explanation("해설")
                                .exam(secondExam)
                                .build());

                // when
                ResultActions resultActions = mvc.perform(get("/api/v1/exam")
                                .param("subjectId", economics.getId().toString())
                                .param("year", String.valueOf(2024)));

                // then
                resultActions
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.length()").value(3))
                                .andExpect(jsonPath("$.data[0].status").value("PUBLISHED"))
                                .andExpect(jsonPath("$.data[0].questionCount").value(1))
                                .andExpect(jsonPath("$.data[1].questionCount").value(1))
                                .andExpect(jsonPath("$.data[2].questionCount").value(0))
                                .andDo(document("getExamsBySubjectIdAndYear",
                                                preprocessRequest(prettyPrint()),
                                                preprocessResponse(prettyPrint()),

                                                queryParameters(
                                                                parameterWithName("subjectId").description("과목 고유 식별자"),
                                                                parameterWithName("year").description("시험 연도")),

                                                responseFields(
                                                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER)
                                                                                .description("시험 고유 식별자"),
                                                                fieldWithPath("data[].name").type(JsonFieldType.STRING)
                                                                                .description("시험 이름"),
                                                                fieldWithPath("data[].year").type(JsonFieldType.NUMBER)
                                                                                .description("시험 연도"),
                                                                fieldWithPath("data[].questionCount").type(JsonFieldType.NUMBER)
                                                                                .description("사용자에게 실제로 노출되는 활성 문제 수"),
                                                                fieldWithPath("data[].status").type(JsonFieldType.STRING)
                                                                                .description("시험 공개 상태"))));

        }

        @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        @Test
        void getBySubjectIdAndYear_withoutYear() throws Exception {
                // given
                examRepository.save(Exam.builder()
                                .id(1L)
                                .name("1차")
                                .year(2024)
                                .subjectId(economics.getId())
                                .status(ExamStatus.PUBLISHED)
                                .build());

                examRepository.save(Exam.builder()
                                .id(2L)
                                .name("2차")
                                .year(2024)
                                .subjectId(economics.getId())
                                .status(ExamStatus.PUBLISHED)
                                .build());

                examRepository.save(Exam.builder()
                                .id(3L)
                                .name("1차")
                                .year(2023)
                                .subjectId(economics.getId())
                                .status(ExamStatus.PUBLISHED)
                                .build());

                examRepository.save(Exam.builder()
                                .id(4L)
                                .name("비공개")
                                .year(2022)
                                .subjectId(economics.getId())
                                .status(ExamStatus.DRAFT)
                                .build());
                problemRepository.save(Problem.builder()
                                .id(20L)
                                .number(1)
                                .content("연도 없는 조회 문제")
                                .explanation("해설")
                                .exam(examRepository.findById(1L).orElseThrow())
                                .build());

                // when
                ResultActions resultActions = mvc.perform(get("/api/v1/exam")
                                .param("subjectId", economics.getId().toString()));

                // then
                resultActions
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.length()").value(3))
                                .andExpect(jsonPath("$.data[0].status").value("PUBLISHED"))
                                .andDo(document("getExamsBySubjectIdWithoutYear",
                                                preprocessRequest(prettyPrint()),
                                                preprocessResponse(prettyPrint()),
                                                resource(examResource(
                                                                "사용자 시험 목록 조회",
                                                                "모바일 시험 선택 화면이 사용하는 공개 시험 목록 조회 응답입니다. questionCount 는 /api/v2/problem 과 동일한 활성 문제 기준입니다."
                                                )),

                                                queryParameters(
                                                                parameterWithName("subjectId").description("과목 고유 식별자"),
                                                                parameterWithName("year").description("시험 연도")
                                                                                .optional()),

                                                responseFields(
                                                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER)
                                                                                .description("시험 고유 식별자"),
                                                                fieldWithPath("data[].name").type(JsonFieldType.STRING)
                                                                                .description("시험 이름"),
                                                                fieldWithPath("data[].year").type(JsonFieldType.NUMBER)
                                                                                .description("시험 연도"),
                                                                fieldWithPath("data[].questionCount").type(JsonFieldType.NUMBER)
                                                                                .description("사용자에게 실제로 노출되는 활성 문제 수"),
                                                                fieldWithPath("data[].status").type(JsonFieldType.STRING)
                                                                                .description("시험 공개 상태"))));

        }

        @Test
        void getYears() throws Exception {
                // given
                examRepository.save(Exam.builder()
                                .id(1L)
                                .name("1차")
                                .year(2024)
                                .subjectId(economics.getId())
                                .status(ExamStatus.PUBLISHED)
                                .build());

                examRepository.save(Exam.builder()
                                .id(2L)
                                .name("1차")
                                .year(2023)
                                .subjectId(economics.getId())
                                .status(ExamStatus.PUBLISHED)
                                .build());

                examRepository.save(Exam.builder()
                                .id(3L)
                                .name("1차")
                                .year(2022)
                                .subjectId(economics.getId())
                                .status(ExamStatus.PUBLISHED)
                                .build());

                examRepository.save(Exam.builder()
                                .id(4L)
                                .name("비공개")
                                .year(2021)
                                .subjectId(economics.getId())
                                .status(ExamStatus.DRAFT)
                                .build());

                // when
                ResultActions resultActions = mvc.perform(get("/api/v1/exam/year")
                                .param("subjectId", economics.getId().toString()));

                // then
                resultActions
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.length()").value(3))
                                .andExpect(jsonPath("$.data[0]").value(2024))
                                .andExpect(jsonPath("$.data[1]").value(2023))
                                .andExpect(jsonPath("$.data[2]").value(2022))
                                .andDo(document("getYearBySubjectId",
                                                preprocessRequest(prettyPrint()),
                                                preprocessResponse(prettyPrint()),

                                                queryParameters(
                                                                parameterWithName("subjectId")
                                                                                .description("과목 고유 식별자")),

                                                responseFields(
                                                                fieldWithPath("data[]").type(JsonFieldType.ARRAY)
                                                                                .description("시험 연도 정보"))));

        }

        @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        @Test
        void adminShouldListDraftAndPublishedExams() throws Exception {
                examRepository.save(Exam.builder()
                                .id(1L)
                                .name("공개 시험")
                                .year(2024)
                                .subjectId(economics.getId())
                                .status(ExamStatus.PUBLISHED)
                                .build());

                examRepository.save(Exam.builder()
                                .id(2L)
                                .name("임시 시험")
                                .year(2024)
                                .subjectId(economics.getId())
                                .status(ExamStatus.DRAFT)
                                .build());

                mvc.perform(get("/api/admin/subject/{subjectId}/exam", economics.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.length()").value(2))
                                .andExpect(jsonPath("$.data[0].status").exists())
                                .andExpect(jsonPath("$.data[1].status").exists());
        }

        @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        @Test
        void adminShouldCreateDraftExam() throws Exception {
                mvc.perform(post("/api/admin/exam")
                                .param("subjectId", economics.getId().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "name": "2026 모의고사",
                                                  "year": 2026,
                                                  "status": "DRAFT"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").isNumber());

                mvc.perform(get("/api/admin/subject/{subjectId}/exam", economics.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.length()").value(1))
                                .andExpect(jsonPath("$.data[0].name").value("2026 모의고사"))
                                .andExpect(jsonPath("$.data[0].status").value("DRAFT"));
        }

        @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        @Test
        void adminShouldUpdateExamStatus() throws Exception {
                Exam exam = examRepository.save(Exam.builder()
                                .id(1L)
                                .name("상태 변경 대상")
                                .year(2025)
                                .subjectId(economics.getId())
                                .status(ExamStatus.DRAFT)
                                .build());

                mvc.perform(patch("/api/admin/exam/{id}", exam.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "name": "상태 변경 대상",
                                                  "year": 2025,
                                                  "status": "PUBLISHED"
                                                }
                                                """))
                                .andExpect(status().isOk());

                mvc.perform(get("/api/admin/subject/{subjectId}/exam", economics.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data[0].status").value("PUBLISHED"));
        }

        private ResourceSnippetParameters examResource(String summary, String description) {
                return ResourceSnippetParameters.builder()
                                .tag("Exam")
                                .summary(summary)
                                .description(description)
                                .build();
        }
}
