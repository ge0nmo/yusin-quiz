package com.cpa.yusin.quiz.content;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.cpa.yusin.quiz.common.domain.ContentStatus;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.infrastructure.ExamRepository;
import com.cpa.yusin.quiz.file.controller.port.FileService;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.infrastructure.MemberRepository;
import com.cpa.yusin.quiz.global.jwt.JwtService;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.infrastructure.ProblemRepository;
import com.cpa.yusin.quiz.qualification.domain.QualificationExam;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamCode;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamSubject;
import com.cpa.yusin.quiz.qualification.infrastructure.QualificationExamRepository;
import com.cpa.yusin.quiz.qualification.infrastructure.QualificationExamSubjectRepository;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.infrastructure.SubjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
class ContentApiIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired ObjectMapper objectMapper;
    @Autowired QualificationExamRepository qualificationExamRepository;
    @Autowired QualificationExamSubjectRepository mappingRepository;
    @Autowired SubjectRepository subjectRepository;
    @Autowired ExamRepository examRepository;
    @Autowired ProblemRepository problemRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;
    @MockBean FileService fileService;

    private MockMvc mockMvc;
    private Problem olderProblem;
    private Problem newerProblem;
    private Choice correctChoice;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
        problemRepository.deleteAll();
        examRepository.deleteAll();
        mappingRepository.deleteAll();
        subjectRepository.deleteAll();
        qualificationExamRepository.deleteAll();
        memberRepository.deleteAll();

        QualificationExam appraiser = qualificationExamRepository.save(
                new QualificationExam(QualificationExamCode.APPRAISER, ContentStatus.PUBLISHED));
        Subject accounting = subjectRepository.save(new Subject("회계학", ContentStatus.PUBLISHED));
        QualificationExamSubject mapping = mappingRepository.save(
                new QualificationExamSubject(appraiser, accounting, ContentStatus.PUBLISHED, 1));
        Exam exam2022 = examRepository.save(new Exam(appraiser, "2022년 1차", 2022, ContentStatus.PUBLISHED));
        Exam exam2025 = examRepository.save(new Exam(appraiser, "2025년 1차", 2025, ContentStatus.PUBLISHED));

        olderProblem = saveProblem(exam2022, mapping, 41, "오래된 문제");
        newerProblem = saveProblem(exam2025, mapping, 56, "새 문제");
        correctChoice = newerProblem.getChoices().stream().filter(Choice::isAnswer).findFirst().orElseThrow();
    }

    @Test
    void publicCatalogDoesNotLeakAnswersOrExplanationsAndUsesBackendOrder() throws Exception {
        newerProblem.update(newerProblem.getExam(), newerProblem.getSubjectMapping(), newerProblem.getNumber(),
                newerProblem.getStatus(), statementGroupContent(), newerProblem.getExplanation());
        problemRepository.saveAndFlush(newerProblem);

        mockMvc.perform(get("/api/v1/qualification-exams/{code}/subjects", "APPRAISER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("회계학"))
                .andExpect(jsonPath("$.data[0].problemCount").value(2))
                .andDo(document("public-subjects", api("Public Content", "자격시험의 공개 과목 목록 조회")));

        mockMvc.perform(get("/api/v1/qualification-exams/{code}/subjects/{subjectId}/problems",
                        "APPRAISER", newerProblem.getSubjectMapping().getSubject().getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value(newerProblem.getId()))
                .andExpect(jsonPath("$.data[1].id").value(olderProblem.getId()))
                .andExpect(jsonPath("$.data[0].content[0].type").value("statementGroup"))
                .andExpect(jsonPath("$.data[0].content[0].items[0].label").value("(가)"))
                .andExpect(jsonPath("$.data[0].content[0].items[0].content[0].spans[0].text")
                        .value("보고기간말 이전에"))
                .andExpect(jsonPath("$.data[0].choices", hasSize(5)))
                .andExpect(jsonPath("$.data[0].choices[0].isAnswer").doesNotExist())
                .andExpect(jsonPath("$.data[0].choices[0].explanation").doesNotExist())
                .andExpect(jsonPath("$.data[0].explanation").doesNotExist())
                .andDo(document("public-problems", api("Public Content", "과목의 공개 문제와 보기 전체 조회")));
    }

    @Test
    void checkReturnsOnlyCorrectnessAndSolutionsAreSeparate() throws Exception {
        mockMvc.perform(post("/api/v1/qualification-exams/{code}/problems/{problemId}/check",
                        "APPRAISER", newerProblem.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("selectedChoiceId", correctChoice.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.correct").value(true))
                .andExpect(jsonPath("$.data.correctChoiceId").doesNotExist())
                .andDo(document("public-check-answer", api("Public Content", "선택한 보기 정답 여부 확인")));

        mockMvc.perform(post("/api/v1/qualification-exams/{code}/solutions", "APPRAISER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("problemIds", List.of(newerProblem.getId())))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].problemId").value(newerProblem.getId()))
                .andExpect(jsonPath("$.data[0].correctChoiceId").value(correctChoice.getId()))
                .andExpect(jsonPath("$.data[0].choices", hasSize(5)))
                .andDo(document("public-solutions", api("Public Content", "완료 문제의 정답과 해설 조회")));
    }

    @Test
    void publicProblemAccessIsScopedByQualificationCode() throws Exception {
        mockMvc.perform(post("/api/v1/qualification-exams/{code}/problems/{problemId}/check",
                        "CUSTOMS_BROKER", newerProblem.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("selectedChoiceId", correctChoice.getId()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROBLEM_NOT_FOUND"));
    }

    @Test
    void adminLoginUsesLoginIdAndProtectsDashboard() throws Exception {
        memberRepository.save(new Member("quiz-admin", passwordEncoder.encode("safe-password"), Role.ADMIN));

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());

        String loginBody = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "loginId", "quiz-admin", "password", "safe-password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.loginId").value("quiz-admin"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(header().exists("Set-Cookie"))
                .andDo(document("admin-login", api("Admin Auth", "관리자 아이디 로그인")))
                .andReturn().getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(loginBody).path("data").path("accessToken").asText();
        String refreshToken = objectMapper.readTree(loginBody).path("data").path("refreshToken").asText();
        org.assertj.core.api.Assertions.assertThat(jwtService.isValidAccessToken(accessToken,
                memberRepository.findByLoginId("quiz-admin").orElseThrow())).isTrue();

        mockMvc.perform(post("/api/admin/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andDo(document("admin-refresh", api("Admin Auth", "관리자 토큰 갱신")));

        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.qualificationExamCount").value(1))
                .andExpect(jsonPath("$.data.subjectCount").value(1))
                .andExpect(jsonPath("$.data.examCount").value(2))
                .andExpect(jsonPath("$.data.problemCount").value(2))
                .andDo(document("admin-dashboard", api("Admin", "관리자 콘텐츠 집계 조회")));

        mockMvc.perform(post("/api/admin/logout"))
                .andExpect(status().isNoContent())
                .andDo(document("admin-logout", api("Admin Auth", "관리자 로그아웃")));
    }

    @Test
    void adminImageUploadContractRemainsAvailable() throws Exception {
        String token = loginAdmin();
        MockMultipartFile upload = new MockMultipartFile("file", "problem.png", "image/png", "image".getBytes());
        when(fileService.save(any())).thenReturn("https://bucket.s3.amazonaws.com/post/problem.png");
        when(fileService.generatePresignedUrl("post/problem.png")).thenReturn("https://signed/problem.png");

        mockMvc.perform(multipart("/api/admin/file").file(upload).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(content().string("https://signed/problem.png"))
                .andDo(document("admin-upload-file", api("Admin Content", "문제 이미지 업로드")));
    }

    @Test
    void adminCanCreateQualificationSubjectExamAndJsonOnlyProblem() throws Exception {
        String accessToken = loginAdmin();

        long subjectId = responseId(mockMvc.perform(post("/api/admin/subjects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "경제학", "status", "PUBLISHED"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("경제학"))
                .andDo(document("admin-create-subject", api("Admin Content", "전역 과목 생성")))
                .andReturn().getResponse().getContentAsString());

        long qualificationId = responseId(mockMvc.perform(post("/api/admin/qualification-exams")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "CUSTOMS_BROKER", "status", "PUBLISHED",
                                "subjects", List.of(Map.of("subjectId", subjectId, "status", "PUBLISHED", "displayOrder", 1))))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("CUSTOMS_BROKER"))
                .andExpect(jsonPath("$.data.name").value("관세사"))
                .andDo(document("admin-create-qualification-exam", api("Admin Content", "자격시험과 과목 연결 생성")))
                .andReturn().getResponse().getContentAsString());

        long examId = responseId(mockMvc.perform(post("/api/admin/exams")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "qualificationExamId", qualificationId, "name", "2026년 1차",
                                "year", 2026, "status", "PUBLISHED"))))
                .andExpect(status().isCreated())
                .andDo(document("admin-create-exam", api("Admin Content", "시험 회차 생성")))
                .andReturn().getResponse().getContentAsString());

        List<Map<String, Object>> choices = List.of(
                adminChoice(1, false), adminChoice(2, false), adminChoice(3, true),
                adminChoice(4, false), adminChoice(5, false));
        Map<String, Object> problemRequest = new java.util.LinkedHashMap<>();
        problemRequest.put("examId", examId);
        problemRequest.put("subjectId", subjectId);
        problemRequest.put("number", 1);
        problemRequest.put("status", "PUBLISHED");
        problemRequest.put("content", statementGroupContent());
        problemRequest.put("explanation", List.of());
        problemRequest.put("choices", choices);

        mockMvc.perform(post("/api/admin/problems")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(problemRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content[0].type").value("statementGroup"))
                .andExpect(jsonPath("$.data.content[0].items[0].label").value("(가)"))
                .andExpect(jsonPath("$.data.content[0].items[0].content[0].spans[0].text")
                        .value("보고기간말 이전에"))
                .andExpect(jsonPath("$.data.choices", hasSize(5)))
                .andExpect(jsonPath("$.data.choices[2].isAnswer").value(true))
                .andExpect(jsonPath("$.data.explanation", hasSize(0)))
                .andDo(document("admin-create-problem", api("Admin Content", "JSON 블록 문제 생성")));
    }

    @Test
    void adminRejectsQualificationCodesOutsideTheEnum() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(post("/api/admin/qualification-exams")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "LAWYER", "status", "DRAFT", "subjects", List.of()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminRejectsMalformedStatementGroupsOnCreateAndUpdate() throws Exception {
        String token = loginAdmin();
        Exam exam = newerProblem.getExam();
        Subject subject = newerProblem.getSubjectMapping().getSubject();

        Map<String, Object> createPayload = problemPayload(exam.getId(), subject.getId(), 57);
        createPayload.put("content", List.of(Map.of("type", "statementGroup", "items", List.of())));
        mockMvc.perform(post("/api/admin/problems").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STATEMENT_GROUP"));

        Map<String, Object> updatePayload = problemPayload(
                exam.getId(), subject.getId(), newerProblem.getNumber());
        updatePayload.put("content", List.of(Map.of(
                "type", "statementGroup",
                "items", List.of(Map.of("label", "(가)", "content", List.of())))));
        mockMvc.perform(put("/api/admin/problems/{id}", newerProblem.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_STATEMENT_GROUP"));

        org.assertj.core.api.Assertions.assertThat(problemRepository.findById(newerProblem.getId()).orElseThrow()
                .getContent().getFirst().get("text")).isEqualTo("새 문제");
    }

    @Test
    void problemNumberAndNextNumberAreScopedByExamAndSubject() throws Exception {
        Exam exam = newerProblem.getExam();
        QualificationExam qualification = exam.getQualificationExam();
        Subject economics = subjectRepository.save(new Subject("경제학", ContentStatus.PUBLISHED));
        QualificationExamSubject economicsMapping = mappingRepository.save(
                new QualificationExamSubject(qualification, economics, ContentStatus.PUBLISHED, 2));

        Problem sameNumberInAnotherSubject = saveProblem(exam, economicsMapping, newerProblem.getNumber(), "경제 문제");
        org.assertj.core.api.Assertions.assertThat(sameNumberInAnotherSubject.getId()).isNotNull();

        String accessToken = loginAdmin();
        mockMvc.perform(get("/api/admin/problems/next-number")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("examId", exam.getId().toString())
                        .param("subjectId", economics.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nextNumber").value(newerProblem.getNumber() + 1))
                .andDo(document("admin-next-problem-number",
                        api("Admin Content", "시험 회차와 과목 범위의 다음 문제 번호 조회")));

        mockMvc.perform(post("/api/admin/problems").header("Authorization", bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(problemPayload(
                                exam.getId(), economics.getId(), newerProblem.getNumber()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PROBLEM_NUMBER_EXISTS"));
    }

    @Test
    void adminReadUpdateAndDeleteContractsAreAvailable() throws Exception {
        String token = loginAdmin();
        QualificationExam qualification = newerProblem.getExam().getQualificationExam();
        Subject subject = newerProblem.getSubjectMapping().getSubject();
        Exam exam = newerProblem.getExam();

        mockMvc.perform(get("/api/admin/qualification-exams").header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andDo(document("admin-list-qualification-exams",
                        api("Admin Content", "자격시험 목록 조회")));
        mockMvc.perform(get("/api/admin/qualification-exams/{id}", qualification.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andDo(document("admin-get-qualification-exam",
                        api("Admin Content", "자격시험 상세 조회")));
        mockMvc.perform(put("/api/admin/qualification-exams/{id}", qualification.getId())
                        .header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "status", "PUBLISHED", "subjects", List.of(Map.of(
                                        "subjectId", subject.getId(), "status", "PUBLISHED", "displayOrder", 1))))))
                .andExpect(status().isOk()).andDo(document("admin-update-qualification-exam",
                        api("Admin Content", "자격시험과 과목 연결 수정")));
        long emptyQualificationId = responseId(mockMvc.perform(post("/api/admin/qualification-exams")
                        .header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", "CPA", "status", "DRAFT", "subjects", List.of()))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        mockMvc.perform(delete("/api/admin/qualification-exams/{id}", emptyQualificationId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent()).andDo(document("admin-delete-qualification-exam",
                        api("Admin Content", "사용하지 않는 자격시험 삭제")));

        mockMvc.perform(get("/api/admin/subjects").header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andDo(document("admin-list-subjects", api("Admin Content", "전역 과목 목록 조회")));
        mockMvc.perform(get("/api/admin/subjects/{id}", subject.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andDo(document("admin-get-subject", api("Admin Content", "전역 과목 상세 조회")));
        mockMvc.perform(put("/api/admin/subjects/{id}", subject.getId())
                        .header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "회계학", "status", "PUBLISHED"))))
                .andExpect(status().isOk()).andDo(document("admin-update-subject", api("Admin Content", "전역 과목 수정")));
        long unusedSubjectId = responseId(mockMvc.perform(post("/api/admin/subjects")
                        .header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "미사용 과목", "status", "DRAFT"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        mockMvc.perform(delete("/api/admin/subjects/{id}", unusedSubjectId).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent()).andDo(document("admin-delete-subject",
                        api("Admin Content", "연결되지 않은 전역 과목 삭제")));

        mockMvc.perform(get("/api/admin/exams").param("qualificationExamId", qualification.getId().toString())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andDo(document("admin-list-exams", api("Admin Content", "자격시험별 회차 목록 조회")));
        mockMvc.perform(get("/api/admin/exams/{id}", exam.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andDo(document("admin-get-exam", api("Admin Content", "시험 회차 상세 조회")));
        mockMvc.perform(put("/api/admin/exams/{id}", exam.getId()).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(Map.of(
                                "qualificationExamId", qualification.getId(), "name", exam.getName(),
                                "year", exam.getYear(), "status", "PUBLISHED"))))
                .andExpect(status().isOk()).andDo(document("admin-update-exam", api("Admin Content", "시험 회차 수정")));
        long unusedExamId = responseId(mockMvc.perform(post("/api/admin/exams")
                        .header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "qualificationExamId", qualification.getId(), "name", "미사용 회차",
                                "year", 2026, "status", "DRAFT"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        mockMvc.perform(delete("/api/admin/exams/{id}", unusedExamId).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent()).andDo(document("admin-delete-exam",
                        api("Admin Content", "문제가 없는 시험 회차 삭제")));

        mockMvc.perform(get("/api/admin/problems").param("qualificationExamId", qualification.getId().toString())
                        .param("examId", exam.getId().toString()).param("subjectId", subject.getId().toString())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andDo(document("admin-list-problems", api("Admin Content", "조건별 문제 목록 조회")));
        mockMvc.perform(get("/api/admin/problems/{id}", newerProblem.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andDo(document("admin-get-problem", api("Admin Content", "문제 상세 조회")));
        mockMvc.perform(put("/api/admin/problems/{id}", newerProblem.getId()).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(problemPayload(
                                exam.getId(), subject.getId(), newerProblem.getNumber()))))
                .andExpect(status().isOk()).andDo(document("admin-update-problem", api("Admin Content", "JSON 블록 문제 수정")));
        mockMvc.perform(delete("/api/admin/problems/{id}", olderProblem.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent()).andDo(document("admin-delete-problem", api("Admin Content", "문제 삭제")));
    }

    private Problem saveProblem(Exam exam, QualificationExamSubject mapping, int number, String text) {
        Problem problem = new Problem(exam, mapping, number, ContentStatus.PUBLISHED,
                List.of(Map.of("type", "text", "text", text)),
                List.of(Map.of("type", "text", "text", "종합 해설")));
        problem.replaceChoices(List.of(
                new Choice(1, "보기 1", false, List.of()),
                new Choice(2, "보기 2", true, List.of(Map.of("type", "text", "text", "정답 해설"))),
                new Choice(3, "보기 3", false, List.of()),
                new Choice(4, "보기 4", false, List.of()),
                new Choice(5, "보기 5", false, List.of())
        ));
        return problemRepository.saveAndFlush(problem);
    }

    private String loginAdmin() throws Exception {
        memberRepository.save(new Member("content-admin", passwordEncoder.encode("safe-password"), Role.ADMIN));
        String body = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "loginId", "content-admin", "password", "safe-password"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("accessToken").asText();
    }

    private long responseId(String responseBody) throws Exception {
        return objectMapper.readTree(responseBody).path("data").path("id").asLong();
    }

    private Map<String, Object> adminChoice(int number, boolean answer) {
        return Map.of("number", number, "content", "보기 " + number,
                "isAnswer", answer, "explanation", List.of());
    }

    private Map<String, Object> problemPayload(long examId, long subjectId, int number) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("examId", examId);
        payload.put("subjectId", subjectId);
        payload.put("number", number);
        payload.put("status", "PUBLISHED");
        payload.put("content", List.of(Map.of("type", "text", "text", "수정 문제")));
        payload.put("explanation", List.of());
        payload.put("choices", List.of(adminChoice(1, false), adminChoice(2, true),
                adminChoice(3, false), adminChoice(4, false), adminChoice(5, false)));
        return payload;
    }

    private List<Map<String, Object>> statementGroupContent() {
        return List.of(Map.of(
                "type", "statementGroup",
                "items", List.of(
                        Map.of("label", "(가)", "content", List.of(Map.of(
                                "type", "text", "spans", List.of(Map.of("text", "보고기간말 이전에"))))),
                        Map.of("label", "ㄴ.", "content", List.of(Map.of(
                                "type", "text", "spans", List.of(Map.of("text", "유동부채로 분류한다"))))))));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private org.springframework.restdocs.snippet.Snippet api(String tag, String summary) {
        return resource(ResourceSnippetParameters.builder().tag(tag).summary(summary).build());
    }
}
