package com.cpa.yusin.quiz.report;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.common.domain.ContentStatus;
import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.infrastructure.ExamRepository;
import com.cpa.yusin.quiz.file.controller.port.FileService;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.infrastructure.MemberRepository;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.infrastructure.ProblemRepository;
import com.cpa.yusin.quiz.qualification.domain.QualificationExam;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamCode;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamSubject;
import com.cpa.yusin.quiz.qualification.infrastructure.QualificationExamRepository;
import com.cpa.yusin.quiz.qualification.infrastructure.QualificationExamSubjectRepository;
import com.cpa.yusin.quiz.report.domain.Report;
import com.cpa.yusin.quiz.report.domain.ReportCategory;
import com.cpa.yusin.quiz.report.domain.ReportStatus;
import com.cpa.yusin.quiz.report.infrastructure.ReportRepository;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.infrastructure.SubjectRepository;
import com.cpa.yusin.quiz.version.domain.AppPlatform;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
class ReportApiIntegrationTest {
    private static final String REPORTS = "/api/v2/qualification-exams/{code}/reports";
    private static final String REPORT = "/api/v2/qualification-exams/{code}/reports/{id}";
    private static final String DEVICE_HEADER = "X-Device-Id";
    private static final String DEVICE_A = "a3f9e8d2-1b7c-4c5e-9f0a-2d6b8e4f1c21";
    private static final String DEVICE_B = "b1c2d3e4-f5a6-4b7c-8d9e-0f1a2b3c4d5e";
    private static final String DEVICE_C = "c0ffee00-1234-4abc-8def-001122334455";
    private static final String DATE_TIME = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$";

    @Autowired WebApplicationContext context;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired QualificationExamRepository qualificationExamRepository;
    @Autowired QualificationExamSubjectRepository mappingRepository;
    @Autowired SubjectRepository subjectRepository;
    @Autowired ExamRepository examRepository;
    @Autowired ProblemRepository problemRepository;
    @Autowired ReportRepository reportRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @MockBean FileService fileService;
    @SpyBean ClockHolder clockHolder;

    private MockMvc mockMvc;
    private QualificationExam appraiser;
    private QualificationExam cpa;
    private Subject civilLaw;
    private Exam exam35;
    private Problem problem12;
    private Problem problem13;
    private Problem cpaProblem;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
        cleanUp();

        appraiser = qualificationExamRepository.save(
                new QualificationExam(QualificationExamCode.APPRAISER, ContentStatus.PUBLISHED));
        civilLaw = subjectRepository.save(new Subject("민법", ContentStatus.PUBLISHED));
        QualificationExamSubject mapping = mappingRepository.save(
                new QualificationExamSubject(appraiser, civilLaw, ContentStatus.PUBLISHED, 1));
        exam35 = examRepository.save(new Exam(appraiser, "제35회", 2024, ContentStatus.PUBLISHED));
        problem12 = saveProblem(exam35, mapping, 12);
        problem13 = saveProblem(exam35, mapping, 13);

        cpa = qualificationExamRepository.save(new QualificationExam(QualificationExamCode.CPA, ContentStatus.PUBLISHED));
        Subject accounting = subjectRepository.save(new Subject("회계학", ContentStatus.PUBLISHED));
        QualificationExamSubject cpaMapping = mappingRepository.save(
                new QualificationExamSubject(cpa, accounting, ContentStatus.PUBLISHED, 1));
        Exam cpaExam = examRepository.save(new Exam(cpa, "2025년 1차", 2025, ContentStatus.PUBLISHED));
        cpaProblem = saveProblem(cpaExam, cpaMapping, 1);
    }

    /** 다른 통합 테스트가 자격시험·문제를 직접 지우므로 신고를 남기지 않는다. */
    @AfterEach
    void tearDown() {
        reportRepository.deleteAll();
    }

    // ---------------------------------------------------------------- app: create

    @Test
    void appCreatesProblemReportWithSnapshotAndNormalizedDeviceId() throws Exception {
        String body = createReport(DEVICE_A.toUpperCase(), "APPRAISER",
                payload("WRONG_ANSWER", problem12.getId(), "  3번도 맞는 설명 아닌가요?  ", List.of(3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.category").value("WRONG_ANSWER"))
                .andExpect(jsonPath("$.data.status").value("RECEIVED"))
                .andExpect(jsonPath("$.data.problem.id").value(problem12.getId()))
                .andExpect(jsonPath("$.data.problem.number").value(12))
                .andExpect(jsonPath("$.data.problem.examName").value("제35회"))
                .andExpect(jsonPath("$.data.problem.examYear").value(2024))
                .andExpect(jsonPath("$.data.problem.subjectName").value("민법"))
                .andExpect(jsonPath("$.data.content").value("3번도 맞는 설명 아닌가요?"))
                .andExpect(jsonPath("$.data.selectedChoiceNumbers", contains(3)))
                .andExpect(jsonPath("$.data.answer").value(nullValue()))
                .andExpect(jsonPath("$.data.answeredAt").value(nullValue()))
                .andExpect(jsonPath("$.data.createdAt", matchesPattern(DATE_TIME)))
                .andExpect(jsonPath("$.data.updatedAt", matchesPattern(DATE_TIME)))
                .andDo(document("public-v2-create-report", appApi("문제 오류 신고·건의·문의 보내기")))
                .andReturn().getResponse().getContentAsString();
        System.out.println("REPORT_CREATE_RESPONSE " + body);

        Report saved = reportRepository.findById(objectMapper.readTree(body).path("data").path("id").asLong())
                .orElseThrow();
        assertThat(saved.getDeviceId()).isEqualTo(DEVICE_A);
        assertThat(saved.getAppVersion()).isEqualTo("2.0.0");
        assertThat(saved.getPlatform()).isEqualTo(AppPlatform.ANDROID);
        assertThat(saved.getQualificationExam().getId()).isEqualTo(appraiser.getId());
    }

    @Test
    void feedbackMayOmitOrAttachProblemAndSortsChoiceNumbers() throws Exception {
        createReport(DEVICE_A, "APPRAISER", payload("SUGGESTION", null, "다크 모드가 있으면 좋겠어요", List.of()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.problem").value(nullValue()))
                .andExpect(jsonPath("$.data.selectedChoiceNumbers", hasSize(0)));
        createReport(DEVICE_A, "APPRAISER", payload("INQUIRY", problem12.getId(), "이 문제는 몇 년도 기출인가요?", List.of(4, 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.problem.id").value(problem12.getId()))
                .andExpect(jsonPath("$.data.selectedChoiceNumbers", contains(2, 4)));
        // 내용 선택 유형은 빈 내용을 받는다.
        createReport(DEVICE_A, "APPRAISER", payload("TYPO", problem12.getId(), "", List.of()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content").value(""));
        // 앞뒤 공백을 뺀 뒤 1000자면 받는다.
        createReport(DEVICE_A, "APPRAISER", payload("EXPLANATION", problem12.getId(), "  " + "가".repeat(1000) + "  ", List.of()))
                .andExpect(status().isCreated());
        // 내용 필수 유형은 공백을 뺀 5자면 된다.
        createReport(DEVICE_A, "APPRAISER", payload("OTHER_ERROR", problem12.getId(), " 다섯글자다 ", List.of(1, 2, 3, 4, 5)))
                .andExpect(status().isCreated());
    }

    @Test
    void createRejectsInvalidRequestsAsValidationErrors() throws Exception {
        List<Object[]> cases = List.of(
                new Object[]{payload("WRONG_ANSWER", problem12.getId(), "네글자다", List.of()), "content"},
                new Object[]{payload("WRONG_ANSWER", problem12.getId(), "   네글자   ", List.of()), "content"},
                new Object[]{payload("OTHER_ERROR", problem12.getId(), "", List.of()), "content"},
                new Object[]{payload("SUGGESTION", null, "짧음", List.of()), "content"},
                new Object[]{payload("INQUIRY", null, "    ", List.of()), "content"},
                new Object[]{payload("TYPO", problem12.getId(), "가".repeat(1001), List.of()), "content"},
                new Object[]{payload("EXPLANATION", null, "해설이 부족해요", List.of()), "problemId"},
                new Object[]{payload("WRONG_ANSWER", null, "정답이 틀린 것 같아요", List.of()), "problemId"},
                new Object[]{payload("SUGGESTION", null, "건의 사항이에요", List.of(1)), "selectedChoiceNumbers"},
                new Object[]{payload("TYPO", problem12.getId(), "", List.of(1, 1)), "selectedChoiceNumbers"},
                new Object[]{payload("TYPO", problem12.getId(), "", List.of(6)), "selectedChoiceNumbers"},
                new Object[]{payload("TYPO", problem12.getId(), "", List.of(0)), "selectedChoiceNumbers"},
                new Object[]{payload("TYPO", problem12.getId(), "", List.of(1, 2, 3, 4, 5, 1)), "selectedChoiceNumbers"},
                new Object[]{with(payload("TYPO", problem12.getId(), "", List.of()), "selectedChoiceNumbers", null), "selectedChoiceNumbers"},
                new Object[]{with(payload("TYPO", problem12.getId(), "", List.of()), "content", null), "content"},
                new Object[]{with(payload("TYPO", problem12.getId(), "", List.of()), "category", null), "category"},
                new Object[]{with(payload("TYPO", problem12.getId(), "", List.of()), "appVersion", ""), "appVersion"},
                new Object[]{with(payload("TYPO", problem12.getId(), "", List.of()), "appVersion", "1".repeat(21)), "appVersion"},
                new Object[]{with(payload("TYPO", problem12.getId(), "", List.of()), "platform", null), "platform"});
        for (Object[] testCase : cases) {
            createReport(DEVICE_A, "APPRAISER", (Map<?, ?>) testCase[0])
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.valueErrors[*].descriptor", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.startsWith((String) testCase[1]))));
        }
        createReport(DEVICE_A, "APPRAISER", with(payload("TYPO", problem12.getId(), "", List.of()), "category", "UNKNOWN"))
                .andExpect(status().isBadRequest());
        createReport(DEVICE_A, "APPRAISER", with(payload("TYPO", problem12.getId(), "", List.of()), "platform", "WINDOWS"))
                .andExpect(status().isBadRequest());
        assertThat(reportRepository.count()).isZero();
    }

    @Test
    void missingOrMalformedDeviceIdIsRejectedBeforeBodyValidation() throws Exception {
        Map<String, Object> valid = payload("TYPO", problem12.getId(), "", List.of());
        for (String deviceId : new String[]{null, "", "not-a-uuid", DEVICE_A.replace("-", ""),
                DEVICE_A + "0", "g3f9e8d2-1b7c-4c5e-9f0a-2d6b8e4f1c21"}) {
            createReport(deviceId, "APPRAISER", valid)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_DEVICE_ID"))
                    .andExpect(jsonPath("$.message").value("기기 정보를 확인할 수 없어요. 앱을 다시 실행해 주세요."));
        }
        createReport(null, "APPRAISER", payload("WRONG_ANSWER", null, "", List.of()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DEVICE_ID"));
        mockMvc.perform(get(REPORTS, "APPRAISER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DEVICE_ID"));
        mockMvc.perform(get(REPORT, "APPRAISER", 1).header(DEVICE_HEADER, "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DEVICE_ID"));
        assertThat(reportRepository.count()).isZero();
    }

    @Test
    void problemMustBelongToPathQualification() throws Exception {
        createReport(DEVICE_A, "APPRAISER", payload("TYPO", cpaProblem.getId(), "", List.of()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROBLEM_NOT_FOUND"));
        createReport(DEVICE_A, "APPRAISER", payload("SUGGESTION", cpaProblem.getId(), "다른 시험 문제예요", List.of()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROBLEM_NOT_FOUND"));
        createReport(DEVICE_A, "APPRAISER", payload("TYPO", 999_999L, "", List.of()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROBLEM_NOT_FOUND"));
        createReport(DEVICE_A, "CPA", payload("TYPO", cpaProblem.getId(), "", List.of()))
                .andExpect(status().isCreated());
        createReport(DEVICE_A, "CUSTOMS_BROKER", payload("SUGGESTION", null, "관세사도 있나요?", List.of()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("QUALIFICATION_EXAM_NOT_FOUND"));
        createReport(DEVICE_A, "UNKNOWN", payload("SUGGESTION", null, "모르는 코드예요", List.of()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("QUALIFICATION_EXAM_NOT_FOUND"));
    }

    @Test
    void sameDeviceProblemAndCategoryWhileOpenIsDuplicate() throws Exception {
        long first = createdId(createReport(DEVICE_A, "APPRAISER",
                payload("WRONG_ANSWER", problem12.getId(), "정답이 3번 같아요", List.of(3))));

        createReport(DEVICE_A.toUpperCase(), "APPRAISER", payload("WRONG_ANSWER", problem12.getId(), "다시 보내요 정말로", List.of()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REPORT_DUPLICATED"))
                .andExpect(jsonPath("$.message").value("이미 신고한 문제예요. 확인 후 답변 드릴게요."));
        // 유형, 문제, 기기가 다르면 중복이 아니다.
        createReport(DEVICE_A, "APPRAISER", payload("TYPO", problem12.getId(), "", List.of())).andExpect(status().isCreated());
        createReport(DEVICE_A, "APPRAISER", payload("WRONG_ANSWER", problem13.getId(), "정답이 틀렸어요", List.of()))
                .andExpect(status().isCreated());
        createReport(DEVICE_B, "APPRAISER", payload("WRONG_ANSWER", problem12.getId(), "정답이 틀렸어요", List.of()))
                .andExpect(status().isCreated());
        // 확인 중도 미처리다.
        process(first, ReportStatus.IN_REVIEW, null);
        createReport(DEVICE_A, "APPRAISER", payload("WRONG_ANSWER", problem12.getId(), "정답이 틀렸어요", List.of()))
                .andExpect(status().isConflict());
        // 종료되면 다시 보낼 수 있다.
        process(first, ReportStatus.REJECTED, "정답은 2번이 맞아요.");
        createReport(DEVICE_A, "APPRAISER", payload("WRONG_ANSWER", problem12.getId(), "그래도 이상해요", List.of()))
                .andExpect(status().isCreated());
        // 문제 없는 건의·문의는 중복 검사를 하지 않는다.
        createReport(DEVICE_A, "APPRAISER", payload("SUGGESTION", null, "같은 건의를 또 해요", List.of())).andExpect(status().isCreated());
        createReport(DEVICE_A, "APPRAISER", payload("SUGGESTION", null, "같은 건의를 또 해요", List.of())).andExpect(status().isCreated());
    }

    @Test
    void deviceCanSendTwentyReportsInRolling24Hours() throws Exception {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ids.add(createdId(createReport(DEVICE_A, "APPRAISER", payload("SUGGESTION", null, "건의 사항 " + i, List.of()))));
        }
        createReport(DEVICE_A, "APPRAISER", payload("SUGGESTION", null, "스물한 번째 건의", List.of()))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.code").value("REPORT_RATE_LIMITED"))
                .andExpect(jsonPath("$.message").value("오늘은 더 보낼 수 없어요. 내일 다시 시도해 주세요."));
        // 다른 기기는 영향이 없다.
        createReport(DEVICE_B, "APPRAISER", payload("SUGGESTION", null, "다른 기기의 건의", List.of())).andExpect(status().isCreated());

        // 가장 오래된 1건이 24시간 창을 벗어나면 1건만 더 보낼 수 있다.
        setCreatedAt(ids.get(0), LocalDateTime.now().minusHours(24).minusMinutes(1));
        setCreatedAt(ids.get(1), LocalDateTime.now().minusHours(23));
        createReport(DEVICE_A, "APPRAISER", payload("SUGGESTION", null, "창이 지난 뒤 건의", List.of())).andExpect(status().isCreated());
        createReport(DEVICE_A, "APPRAISER", payload("SUGGESTION", null, "다시 한도 초과", List.of()))
                .andExpect(status().isTooManyRequests());
    }

    // ---------------------------------------------------------------- app: read

    @Test
    void appListsAndReadsOnlyThisDevicesReportsForTheCode() throws Exception {
        long older = createdId(createReport(DEVICE_A, "APPRAISER", payload("TYPO", problem12.getId(), "오타가 있어요", List.of())));
        long newer = createdId(createReport(DEVICE_A, "APPRAISER", payload("SUGGESTION", null, "건의 사항이에요", List.of())));
        long otherCode = createdId(createReport(DEVICE_A, "CPA", payload("TYPO", cpaProblem.getId(), "", List.of())));
        long otherDevice = createdId(createReport(DEVICE_B, "APPRAISER", payload("TYPO", problem12.getId(), "", List.of())));
        setCreatedAt(older, LocalDateTime.of(2026, 11, 3, 18, 2));
        setCreatedAt(newer, LocalDateTime.of(2026, 11, 3, 21, 14));

        mockMvc.perform(get(REPORTS, "APPRAISER").header(DEVICE_HEADER, DEVICE_A.toUpperCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value(newer))
                .andExpect(jsonPath("$.data[0].createdAt").value("2026-11-03T21:14:00"))
                .andExpect(jsonPath("$.data[1].id").value(older))
                .andExpect(jsonPath("$.pageInfo").doesNotExist())
                .andDo(document("public-v2-list-reports", appApi("이 기기의 신고·문의 내역")));

        mockMvc.perform(get(REPORT, "APPRAISER", older).header(DEVICE_HEADER, DEVICE_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(older))
                .andExpect(jsonPath("$.data.problem.number").value(12))
                .andDo(document("public-v2-get-report", appApi("이 기기의 신고·문의 상세")));

        for (Object[] notMine : new Object[][]{{"APPRAISER", otherCode}, {"APPRAISER", otherDevice},
                {"CPA", older}, {"APPRAISER", 999_999L}}) {
            mockMvc.perform(get(REPORT, notMine[0], notMine[1]).header(DEVICE_HEADER, DEVICE_A))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("REPORT_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("문의 내역을 찾을 수 없어요."));
        }
        mockMvc.perform(get(REPORTS, "CPA").header(DEVICE_HEADER, DEVICE_B))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void appListReturnsAtMostHundredNewestReports() throws Exception {
        List<Report> reports = IntStream.range(0, 101)
                .mapToObj(i -> new Report(appraiser, null, ReportCategory.SUGGESTION, "건의 " + i, List.of(),
                        DEVICE_C, "2.0.0", AppPlatform.IOS))
                .toList();
        List<Report> saved = reportRepository.saveAll(reports);
        setCreatedAt(saved.get(0).getId(), LocalDateTime.of(2020, 1, 1, 0, 0));

        mockMvc.perform(get(REPORTS, "APPRAISER").header(DEVICE_HEADER, DEVICE_C))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(100)))
                .andExpect(jsonPath("$.data[?(@.id == " + saved.get(0).getId() + ")]").isEmpty());
    }

    @Test
    void appSeesAnswerOnlyWhileClosedButAdminAlwaysSeesStoredAnswer() throws Exception {
        long id = createdId(createReport(DEVICE_A, "APPRAISER", payload("WRONG_ANSWER", problem12.getId(), "3번도 맞는 설명 아닌가요?", List.of(3))));
        LocalDateTime answeredAt = LocalDateTime.of(2026, 11, 4, 10, 20);
        doReturn(answeredAt).when(clockHolder).getCurrentDateTime();
        String token = loginAdmin();

        mockMvc.perform(put("/api/admin/reports/{id}", id).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "RESOLVED", "answer", "  확인해 보니 정답은 2번이 맞아요.  "))))
                .andExpect(status().isOk());
        mockMvc.perform(get(REPORT, "APPRAISER", id).header(DEVICE_HEADER, DEVICE_A))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.answer").value("확인해 보니 정답은 2번이 맞아요."))
                .andExpect(jsonPath("$.data.answeredAt").value("2026-11-04T10:20:00"));

        // 미처리로 되돌리면 답변 시각을 지우고 답변 원문은 관리자에게만 남긴다.
        mockMvc.perform(put("/api/admin/reports/{id}", id).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "IN_REVIEW", "answer", "확인해 보니 정답은 2번이 맞아요."))))
                .andExpect(status().isOk());
        mockMvc.perform(get(REPORTS, "APPRAISER").header(DEVICE_HEADER, DEVICE_A))
                .andExpect(jsonPath("$.data[0].status").value("IN_REVIEW"))
                .andExpect(jsonPath("$.data[0].answer").value(nullValue()))
                .andExpect(jsonPath("$.data[0].answeredAt").value(nullValue()));
        mockMvc.perform(get("/api/admin/reports/{id}", id).header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.data.answer").value("확인해 보니 정답은 2번이 맞아요."))
                .andExpect(jsonPath("$.data.answeredAt").value(nullValue()));

        // REJECTED도 앱에는 답변이 보인다.
        doReturn(answeredAt.plusDays(1)).when(clockHolder).getCurrentDateTime();
        mockMvc.perform(put("/api/admin/reports/{id}", id).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "REJECTED", "answer", "정답은 2번이에요."))))
                .andExpect(status().isOk());
        mockMvc.perform(get(REPORT, "APPRAISER", id).header(DEVICE_HEADER, DEVICE_A))
                .andExpect(jsonPath("$.data.answer").value("정답은 2번이에요."))
                .andExpect(jsonPath("$.data.answeredAt").value("2026-11-05T10:20:00"));
    }

    // ---------------------------------------------------------------- admin

    @Test
    void adminReportApisRequireAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/reports")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/reports/summary")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/reports/{id}", 1)).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/admin/reports/{id}", 1).contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("status", "RESOLVED", "answer", "답변")))).andExpect(status().isUnauthorized());
    }

    @Test
    void adminListSortsBySameProblemAcrossPages() throws Exception {
        Map<String, Long> ids = seedAdminReports();
        String token = loginAdmin();

        // 같은 문제 미처리 수: problem12 = 3, problem13 = 2, 문제 없는 건의 = 0. 같으면 최신순.
        mockMvc.perform(get("/api/admin/reports").header("Authorization", bearer(token))
                        .param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].id", contains(ids.get("p12-c").intValue(), ids.get("p12-b").intValue())))
                .andExpect(jsonPath("$.data[0].sameProblemOpenCount").value(3))
                .andExpect(jsonPath("$.data[0].status").value("RECEIVED"))
                .andExpect(jsonPath("$.data[0].category").value("WRONG_ANSWER"))
                .andExpect(jsonPath("$.data[0].qualificationExam.id").value(appraiser.getId()))
                .andExpect(jsonPath("$.data[0].qualificationExam.name").value("감정평가사"))
                .andExpect(jsonPath("$.data[0].problem.id").value(problem12.getId()))
                .andExpect(jsonPath("$.data[0].problem.number").value(12))
                .andExpect(jsonPath("$.data[0].problem.examId").value(exam35.getId()))
                .andExpect(jsonPath("$.data[0].problem.examName").value("제35회"))
                .andExpect(jsonPath("$.data[0].problem.examYear").value(2024))
                .andExpect(jsonPath("$.data[0].problem.subjectId").value(civilLaw.getId()))
                .andExpect(jsonPath("$.data[0].problem.subjectName").value("민법"))
                .andExpect(jsonPath("$.data[0].content").value("정답이 3번 같아요 C"))
                .andExpect(jsonPath("$.data[0].createdAt").value("2026-11-03T13:00:00"))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(6))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(3))
                .andExpect(jsonPath("$.pageInfo.currentPage").value(1))
                .andExpect(jsonPath("$.pageInfo.pageSize").value(2))
                .andDo(document("admin-list-reports", adminApi("신고·문의 목록 (상태·시험·유형 필터, 정렬, 페이지)")));
        mockMvc.perform(get("/api/admin/reports").header("Authorization", bearer(token))
                        .param("page", "2").param("size", "2"))
                .andExpect(jsonPath("$.data[*].id", contains(ids.get("p12-a").intValue(), ids.get("p13-b").intValue())))
                .andExpect(jsonPath("$.data[1].sameProblemOpenCount").value(2))
                .andExpect(jsonPath("$.pageInfo.currentPage").value(2));
        mockMvc.perform(get("/api/admin/reports").header("Authorization", bearer(token))
                        .param("page", "3").param("size", "2"))
                .andExpect(jsonPath("$.data[*].id", contains(ids.get("p13-a").intValue(), ids.get("feedback").intValue())))
                .andExpect(jsonPath("$.data[1].problem").value(nullValue()))
                .andExpect(jsonPath("$.data[1].sameProblemOpenCount").value(0));
        mockMvc.perform(get("/api/admin/reports").header("Authorization", bearer(token)).param("page", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(6))
                .andExpect(jsonPath("$.pageInfo.pageSize").value(20));
    }

    @Test
    void adminListFiltersByStatusQualificationAndCategoryAndSortsByTime() throws Exception {
        Map<String, Long> ids = seedAdminReports();
        String token = loginAdmin();

        // 기본: 미처리, SAME_PROBLEM
        mockMvc.perform(get("/api/admin/reports").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.data", hasSize(6)))
                .andExpect(jsonPath("$.pageInfo.currentPage").value(1));
        mockMvc.perform(get("/api/admin/reports").header("Authorization", bearer(token)).param("statusGroup", "RESOLVED"))
                .andExpect(jsonPath("$.data[*].id", contains(ids.get("p13-resolved").intValue())))
                // 종료된 신고의 같은 문제 수도 그 문제의 미처리 수다.
                .andExpect(jsonPath("$.data[0].sameProblemOpenCount").value(2));
        mockMvc.perform(get("/api/admin/reports").header("Authorization", bearer(token)).param("statusGroup", "REJECTED"))
                .andExpect(jsonPath("$.data[*].id", contains(ids.get("cpa-rejected").intValue())));
        mockMvc.perform(get("/api/admin/reports").header("Authorization", bearer(token)).param("statusGroup", "ALL"))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(8));
        mockMvc.perform(get("/api/admin/reports").header("Authorization", bearer(token))
                        .param("statusGroup", "ALL").param("qualificationExamId", cpa.getId().toString()))
                .andExpect(jsonPath("$.data[*].id", contains(ids.get("cpa-rejected").intValue())));
        mockMvc.perform(get("/api/admin/reports").header("Authorization", bearer(token)).param("categoryGroup", "FEEDBACK"))
                .andExpect(jsonPath("$.data[*].id", contains(ids.get("feedback").intValue())));
        mockMvc.perform(get("/api/admin/reports").header("Authorization", bearer(token)).param("categoryGroup", "PROBLEM_ERROR")
                        .param("qualificationExamId", appraiser.getId().toString()))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(5));
        mockMvc.perform(get("/api/admin/reports").header("Authorization", bearer(token)).param("sort", "LATEST"))
                .andExpect(jsonPath("$.data[*].id", contains(ids.get("feedback").intValue(), ids.get("p13-b").intValue(),
                        ids.get("p13-a").intValue(), ids.get("p12-c").intValue(), ids.get("p12-b").intValue(),
                        ids.get("p12-a").intValue())));
        mockMvc.perform(get("/api/admin/reports").header("Authorization", bearer(token)).param("sort", "OLDEST")
                        .param("size", "2"))
                .andExpect(jsonPath("$.data[*].id", contains(ids.get("p12-a").intValue(), ids.get("p12-b").intValue())))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(3));

        for (String[] invalid : new String[][]{{"page", "0"}, {"size", "0"}, {"size", "101"}}) {
            mockMvc.perform(get("/api/admin/reports").header("Authorization", bearer(token)).param(invalid[0], invalid[1]))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
        mockMvc.perform(get("/api/admin/reports").header("Authorization", bearer(token)).param("size", "100"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/reports").header("Authorization", bearer(token)).param("statusGroup", "CLOSED"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminSummaryCountsByStatusOverallAndPerQualification() throws Exception {
        seedAdminReports();
        String token = loginAdmin();

        mockMvc.perform(get("/api/admin/reports/summary").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.openCount").value(6))
                .andExpect(jsonPath("$.data.resolvedCount").value(1))
                .andExpect(jsonPath("$.data.rejectedCount").value(1))
                .andExpect(jsonPath("$.data.totalCount").value(8))
                .andDo(document("admin-report-summary", adminApi("신고·문의 상태별 개수")));
        mockMvc.perform(get("/api/admin/reports/summary").header("Authorization", bearer(token))
                        .param("qualificationExamId", cpa.getId().toString()))
                .andExpect(jsonPath("$.data.openCount").value(0))
                .andExpect(jsonPath("$.data.resolvedCount").value(0))
                .andExpect(jsonPath("$.data.rejectedCount").value(1))
                .andExpect(jsonPath("$.data.totalCount").value(1));
        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.data.openReportCount").value(6))
                .andExpect(jsonPath("$.data.problemCount").value(3));
    }

    @Test
    void adminDetailShowsDeviceStatsMaskingAndOtherOpenReports() throws Exception {
        Map<String, Long> ids = seedAdminReports();
        String token = loginAdmin();

        mockMvc.perform(get("/api/admin/reports/{id}", ids.get("p12-b")).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(ids.get("p12-b")))
                .andExpect(jsonPath("$.data.status").value("RECEIVED"))
                .andExpect(jsonPath("$.data.qualificationExam.name").value("감정평가사"))
                .andExpect(jsonPath("$.data.problem.examId").value(exam35.getId()))
                .andExpect(jsonPath("$.data.problem.subjectId").value(civilLaw.getId()))
                .andExpect(jsonPath("$.data.sameProblemOpenCount").value(3))
                .andExpect(jsonPath("$.data.selectedChoiceNumbers", contains(3)))
                .andExpect(jsonPath("$.data.appVersion").value("2.0.0"))
                .andExpect(jsonPath("$.data.platform").value("ANDROID"))
                .andExpect(jsonPath("$.data.deviceIdMasked").value("b1c2…d5e"))
                .andExpect(jsonPath("$.data.deviceReportCount").value(2))
                .andExpect(jsonPath("$.data.deviceRejectedCount").value(1))
                .andExpect(jsonPath("$.data.answer").value(nullValue()))
                .andExpect(jsonPath("$.data.answeredAt").value(nullValue()))
                .andExpect(jsonPath("$.data.updatedAt", matchesPattern(DATE_TIME)))
                .andExpect(jsonPath("$.data.sameProblemOpenReports[*].id",
                        contains(ids.get("p12-c").intValue(), ids.get("p12-a").intValue())))
                .andExpect(jsonPath("$.data.sameProblemOpenReports[0].status").value("RECEIVED"))
                .andExpect(jsonPath("$.data.sameProblemOpenReports[0].category").value("WRONG_ANSWER"))
                .andExpect(jsonPath("$.data.sameProblemOpenReports[0].content").value("정답이 3번 같아요 C"))
                .andExpect(jsonPath("$.data.sameProblemOpenReports[0].createdAt").value("2026-11-03T13:00:00"))
                .andDo(document("admin-get-report", adminApi("신고·문의 상세")));

        mockMvc.perform(get("/api/admin/reports/{id}", ids.get("feedback")).header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.data.problem").value(nullValue()))
                .andExpect(jsonPath("$.data.sameProblemOpenCount").value(0))
                .andExpect(jsonPath("$.data.sameProblemOpenReports", hasSize(0)));
        // 종료된 신고: 자기 자신은 세지 않고 같은 문제의 미처리 신고만 보인다.
        mockMvc.perform(get("/api/admin/reports/{id}", ids.get("p13-resolved")).header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.data.sameProblemOpenCount").value(2))
                .andExpect(jsonPath("$.data.sameProblemOpenReports", hasSize(2)))
                .andExpect(jsonPath("$.data.answer").value("이미 고쳤어요."))
                .andExpect(jsonPath("$.data.answeredAt", matchesPattern(DATE_TIME)));
        mockMvc.perform(get("/api/admin/reports/{id}", 999_999).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REPORT_NOT_FOUND"));
    }

    @Test
    void adminUpdateAppliesToSameProblemOpenReportsWhenRequested() throws Exception {
        Map<String, Long> ids = seedAdminReports();
        String token = loginAdmin();
        doReturn(LocalDateTime.of(2026, 11, 4, 10, 20)).when(clockHolder).getCurrentDateTime();

        mockMvc.perform(put("/api/admin/reports/{id}", ids.get("p12-b")).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "REJECTED", "answer", "확인해 보니 정답은 2번이 맞아요.",
                                "applyToSameProblemOpen", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updatedIds", contains(ids.get("p12-b").intValue(),
                        ids.get("p12-c").intValue(), ids.get("p12-a").intValue())))
                .andDo(document("admin-update-report", adminApi("신고·문의 처리 (같은 문제의 미처리 신고 일괄 적용)")));
        for (String key : List.of("p12-a", "p12-b", "p12-c")) {
            Report report = reportRepository.findById(ids.get(key)).orElseThrow();
            assertThat(report.getStatus()).isEqualTo(ReportStatus.REJECTED);
            assertThat(report.getAnswer()).isEqualTo("확인해 보니 정답은 2번이 맞아요.");
            assertThat(report.getAnsweredAt()).isEqualTo(LocalDateTime.of(2026, 11, 4, 10, 20));
        }

        // 적용하지 않으면 대상만 바뀌고, 종료된 같은 문제 신고는 일괄 적용 대상이 아니다.
        mockMvc.perform(put("/api/admin/reports/{id}", ids.get("p13-a")).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "IN_REVIEW", "applyToSameProblemOpen", false))))
                .andExpect(jsonPath("$.data.updatedIds", contains(ids.get("p13-a").intValue())));
        assertThat(reportRepository.findById(ids.get("p13-b")).orElseThrow().getStatus()).isEqualTo(ReportStatus.RECEIVED);
        mockMvc.perform(put("/api/admin/reports/{id}", ids.get("p13-a")).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "RESOLVED", "answer", "고쳤어요.", "applyToSameProblemOpen", true))))
                .andExpect(jsonPath("$.data.updatedIds", contains(ids.get("p13-a").intValue(), ids.get("p13-b").intValue())));
        assertThat(reportRepository.findById(ids.get("p13-resolved")).orElseThrow().getAnswer()).isEqualTo("이미 고쳤어요.");

        // 문제 없는 건이면 일괄 적용 옵션을 무시한다.
        mockMvc.perform(put("/api/admin/reports/{id}", ids.get("feedback")).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "RESOLVED", "answer", "반영할게요.", "applyToSameProblemOpen", true))))
                .andExpect(jsonPath("$.data.updatedIds", contains(ids.get("feedback").intValue())));
    }

    @Test
    void adminUpdateValidatesAnswerForClosingStatuses() throws Exception {
        Map<String, Long> ids = seedAdminReports();
        String token = loginAdmin();
        long id = ids.get("p12-a");

        List<Map<String, Object>> invalid = new ArrayList<>();
        invalid.add(Map.of("status", "RESOLVED"));
        invalid.add(Map.of("status", "REJECTED", "answer", "   "));
        invalid.add(Map.of("status", "RESOLVED", "answer", "가".repeat(1001)));
        invalid.add(Map.of("status", "IN_REVIEW", "answer", "가".repeat(1001)));
        for (Map<String, Object> body : invalid) {
            mockMvc.perform(put("/api/admin/reports/{id}", id).header("Authorization", bearer(token))
                            .contentType(MediaType.APPLICATION_JSON).content(json(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.valueErrors[0].descriptor").value("answer"));
        }
        mockMvc.perform(put("/api/admin/reports/{id}", id).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("answer", "답변"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(put("/api/admin/reports/{id}", 999_999).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "IN_REVIEW"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REPORT_NOT_FOUND"));
        assertThat(reportRepository.findById(id).orElseThrow().getStatus()).isEqualTo(ReportStatus.RECEIVED);

        // 미처리 상태의 빈 답변은 null로 저장하고, 1000자 답변은 받는다.
        mockMvc.perform(put("/api/admin/reports/{id}", id).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "IN_REVIEW", "answer", "  "))))
                .andExpect(status().isOk());
        Report inReview = reportRepository.findById(id).orElseThrow();
        assertThat(inReview.getStatus()).isEqualTo(ReportStatus.IN_REVIEW);
        assertThat(inReview.getAnswer()).isNull();
        assertThat(inReview.getAnsweredAt()).isNull();
        mockMvc.perform(put("/api/admin/reports/{id}", id).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "RESOLVED", "answer", "가".repeat(1000)))))
                .andExpect(status().isOk());
        assertThat(reportRepository.findById(id).orElseThrow().getAnsweredAt()).isNotNull();
    }

    // ---------------------------------------------------------------- problem deletion

    @Test
    void deletingProblemDetachesReportsAndKeepsSnapshot() throws Exception {
        long first = createdId(createReport(DEVICE_A, "APPRAISER", payload("WRONG_ANSWER", problem12.getId(), "정답이 틀렸어요", List.of(3))));
        long second = createdId(createReport(DEVICE_B, "APPRAISER", payload("TYPO", problem12.getId(), "", List.of())));
        String token = loginAdmin();

        mockMvc.perform(delete("/api/admin/problems/{id}", problem12.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        assertThat(problemRepository.findById(problem12.getId())).isEmpty();
        for (long id : List.of(first, second)) {
            Report report = reportRepository.findById(id).orElseThrow();
            assertThat(report.getProblem()).isNull();
            assertThat(report.getProblemNumber()).isEqualTo(12);
            assertThat(report.getExamName()).isEqualTo("제35회");
        }
        mockMvc.perform(get(REPORT, "APPRAISER", first).header(DEVICE_HEADER, DEVICE_A))
                .andExpect(jsonPath("$.data.problem.id").value(nullValue()))
                .andExpect(jsonPath("$.data.problem.number").value(12))
                .andExpect(jsonPath("$.data.problem.examName").value("제35회"))
                .andExpect(jsonPath("$.data.problem.examYear").value(2024))
                .andExpect(jsonPath("$.data.problem.subjectName").value("민법"));
        mockMvc.perform(get("/api/admin/reports/{id}", first).header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.data.problem.id").value(nullValue()))
                .andExpect(jsonPath("$.data.problem.examId").value(nullValue()))
                .andExpect(jsonPath("$.data.problem.subjectId").value(nullValue()))
                .andExpect(jsonPath("$.data.problem.number").value(12))
                .andExpect(jsonPath("$.data.sameProblemOpenCount").value(0))
                .andExpect(jsonPath("$.data.sameProblemOpenReports", hasSize(0)));
        mockMvc.perform(get("/api/admin/reports").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.data[0].sameProblemOpenCount").value(0))
                .andExpect(jsonPath("$.data[1].sameProblemOpenCount").value(0));
        // 끊긴 신고는 중복 검사 대상이 아니므로 같은 번호의 새 문제에 다시 신고할 수 있다.
        mockMvc.perform(put("/api/admin/reports/{id}", first).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "RESOLVED", "answer", "문제를 지웠어요.", "applyToSameProblemOpen", true))))
                .andExpect(jsonPath("$.data.updatedIds", contains((int) first)));

        // 남은 문제를 지우면 신고가 있어도 회차를 지울 수 있다.
        createReport(DEVICE_A, "APPRAISER", payload("TYPO", problem13.getId(), "", List.of())).andExpect(status().isCreated());
        mockMvc.perform(delete("/api/admin/problems/{id}", problem13.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/admin/exams/{id}", exam35.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        assertThat(reportRepository.count()).isEqualTo(3);

        // 신고·문의가 남은 자격시험은 지우지 않는다.
        mockMvc.perform(put("/api/admin/qualification-exams/{id}", appraiser.getId()).header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("status", "PUBLISHED", "subjects", List.of()))))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/admin/qualification-exams/{id}", appraiser.getId()).header("Authorization", bearer(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONTENT_IN_USE"));
    }

    @Test
    void reportForeignKeySetsNullEvenWhenProblemIsDeletedOutsideTheService() throws Exception {
        long id = createdId(createReport(DEVICE_A, "APPRAISER", payload("TYPO", problem13.getId(), "", List.of())));

        problemRepository.deleteById(problem13.getId());

        Report report = reportRepository.findById(id).orElseThrow();
        assertThat(report.getProblem()).isNull();
        assertThat(report.getProblemNumber()).isEqualTo(13);
    }

    @Test
    void reportTableHasContractIndexes() {
        List<String> indexes = jdbcTemplate.queryForList(
                "select index_name from information_schema.indexes where table_name = 'report'", String.class);
        assertThat(indexes).contains("idx_report_device_created", "idx_report_problem_status", "idx_report_status_created");
    }

    // ---------------------------------------------------------------- helpers

    /**
     * 관리자 목록용 데이터. 감정평가사 미처리 6건(problem12 3건, problem13 2건, 문제 없는 건의 1건)과
     * problem13 처리 완료 1건, 회계사 반려 1건. 받은 시각은 아래 순서대로 오래된 것부터다.
     */
    private Map<String, Long> seedAdminReports() throws Exception {
        Map<String, Long> ids = new LinkedHashMap<>();
        ids.put("cpa-rejected", createdId(createReport(DEVICE_B, "CPA", payload("TYPO", cpaProblem.getId(), "", List.of()))));
        ids.put("p13-resolved", createdId(createReport(DEVICE_C, "APPRAISER", payload("TYPO", problem13.getId(), "", List.of()))));
        ids.put("p12-a", createdId(createReport(DEVICE_A, "APPRAISER", payload("WRONG_ANSWER", problem12.getId(), "정답이 3번 같아요 A", List.of(3)))));
        ids.put("p12-b", createdId(createReport(DEVICE_B, "APPRAISER", payload("WRONG_ANSWER", problem12.getId(), "정답이 3번 같아요 B", List.of(3)))));
        ids.put("p12-c", createdId(createReport(DEVICE_C, "APPRAISER", payload("WRONG_ANSWER", problem12.getId(), "정답이 3번 같아요 C", List.of(3)))));
        ids.put("p13-a", createdId(createReport(DEVICE_A, "APPRAISER", payload("EXPLANATION", problem13.getId(), "해설이 부족해요", List.of()))));
        ids.put("p13-b", createdId(createReport(DEVICE_C, "APPRAISER", payload("BROKEN_MEDIA", problem13.getId(), "표가 깨져요", List.of()))));
        ids.put("feedback", createdId(createReport(DEVICE_A, "APPRAISER", payload("SUGGESTION", null, "다크 모드가 있으면 좋겠어요", List.of()))));
        LocalDateTime base = LocalDateTime.of(2026, 11, 3, 9, 0);
        int hour = 0;
        for (Long id : ids.values()) {
            setCreatedAt(id, base.plusHours(hour++));
        }
        Report rejected = reportRepository.findById(ids.get("cpa-rejected")).orElseThrow();
        rejected.process(ReportStatus.REJECTED, "정답이 맞아요.", LocalDateTime.now());
        reportRepository.save(rejected);
        Report resolved = reportRepository.findById(ids.get("p13-resolved")).orElseThrow();
        resolved.process(ReportStatus.RESOLVED, "이미 고쳤어요.", LocalDateTime.now());
        reportRepository.save(resolved);
        return ids;
    }

    private void process(long id, ReportStatus status, String answer) {
        Report report = reportRepository.findById(id).orElseThrow();
        report.process(status, answer, LocalDateTime.now());
        reportRepository.save(report);
    }

    private void setCreatedAt(long id, LocalDateTime createdAt) {
        jdbcTemplate.update("update report set created_at = ? where id = ?", createdAt, id);
    }

    private ResultActions createReport(String deviceId, String code, Map<?, ?> body) throws Exception {
        var request = post(REPORTS, code).contentType(MediaType.APPLICATION_JSON).content(json(body));
        if (deviceId != null) {
            request.header(DEVICE_HEADER, deviceId);
        }
        return mockMvc.perform(request);
    }

    private long createdId(ResultActions actions) throws Exception {
        String body = actions.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asLong();
    }

    private Map<String, Object> payload(String category, Long problemId, String content, List<Integer> choices) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("category", category);
        payload.put("problemId", problemId);
        payload.put("content", content);
        payload.put("selectedChoiceNumbers", choices);
        payload.put("appVersion", "2.0.0");
        payload.put("platform", "ANDROID");
        return payload;
    }

    private Map<String, Object> with(Map<String, Object> payload, String key, Object value) {
        Map<String, Object> copy = new HashMap<>(payload);
        copy.put(key, value);
        return copy;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private Problem saveProblem(Exam exam, QualificationExamSubject mapping, int number) {
        Problem problem = new Problem(exam, mapping, number, ContentStatus.PUBLISHED,
                List.of(Map.of("type", "text", "text", "문제 " + number)), List.of());
        problem.replaceChoices(List.of(
                new Choice(1, "보기 1", false, List.of()),
                new Choice(2, "보기 2", true, List.of()),
                new Choice(3, "보기 3", false, List.of()),
                new Choice(4, "보기 4", false, List.of()),
                new Choice(5, "보기 5", false, List.of())));
        return problemRepository.saveAndFlush(problem);
    }

    private String loginAdmin() throws Exception {
        if (memberRepository.findByLoginId("report-admin").isEmpty()) {
            memberRepository.save(new Member("report-admin", passwordEncoder.encode("safe-password"), Role.ADMIN));
        }
        String body = mockMvc.perform(post("/api/admin/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("loginId", "report-admin", "password", "safe-password"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode data = objectMapper.readTree(body).path("data");
        return data.path("accessToken").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private void cleanUp() {
        reportRepository.deleteAll();
        problemRepository.deleteAll();
        examRepository.deleteAll();
        mappingRepository.deleteAll();
        subjectRepository.deleteAll();
        qualificationExamRepository.deleteAll();
        memberRepository.deleteAll();
    }

    private org.springframework.restdocs.snippet.Snippet appApi(String summary) {
        return resource(ResourceSnippetParameters.builder().tag("Public Report v2").summary(summary)
                .requestHeaders(headerWithName(DEVICE_HEADER)
                        .description("앱이 처음 실행할 때 만든 익명 기기 UUID (대소문자 무관, 36자)"))
                .build());
    }

    private org.springframework.restdocs.snippet.Snippet adminApi(String summary) {
        return resource(ResourceSnippetParameters.builder().tag("Admin Report").summary(summary).build());
    }
}
