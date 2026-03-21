package com.cpa.yusin.quiz.study.integration;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.domain.ExamStatus;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import com.cpa.yusin.quiz.study.domain.ExamMode;
import com.cpa.yusin.quiz.study.infrastructure.DailyStudyLogJpaRepository;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, TeardownExtension.class})
@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureRestDocs
class StudyApiTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 15, 10, 0);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private ChoiceRepository choiceRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DailyStudyLogJpaRepository dailyStudyLogJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClockHolder clockHolder;

    private Member member;
    private MemberDetails memberDetails;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext,
               RestDocumentationContextProvider restDocumentation) {
        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .apply(documentationConfiguration(restDocumentation))
                .build();

        given(clockHolder.getCurrentDateTime()).willReturn(NOW);
        given(clockHolder.getCurrentTime()).willReturn(NOW.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());

        member = memberRepository.save(Member.builder()
                .email("study-user@test.com")
                .password("encoded-password")
                .username("study-user")
                .platform(Platform.HOME)
                .role(Role.USER)
                .build());
        memberDetails = new MemberDetails(member, null);
    }

    @Test
    @DisplayName("study start 이어풀기 응답은 submittedAnswers item 을 프론트 계약 shape 로 반환한다")
    void startExam_shouldReturnSubmittedAnswersWithFrontendContract() throws Exception {
        ExamFixture fixture = createExamFixture("원가회계", "3차", 2025, 2);
        Long sessionId = startSession(fixture.exam().getId(), ExamMode.EXAM);

        submitAnswer(sessionId, fixture.problemIds().get(0), fixture.wrongChoiceIds().get(0), 1);

        mvc.perform(post("/api/v1/study/exam/start")
                        .with(user(memberDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new StartRequest(fixture.exam().getId(), ExamMode.EXAM))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(sessionId))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.lastIndex").value(1))
                .andExpect(jsonPath("$.data.submittedAnswers").isArray())
                .andExpect(jsonPath("$.data.submittedAnswers.length()").value(1))
                .andExpect(jsonPath("$.data.submittedAnswers[0].problemId").value(fixture.problemIds().get(0)))
                .andExpect(jsonPath("$.data.submittedAnswers[0].choiceId").value(fixture.wrongChoiceIds().get(0)))
                .andExpect(jsonPath("$.data.submittedAnswers[0].isCorrect").value(false))
                .andExpect(jsonPath("$.data.submittedAnswers[0].correct").doesNotExist())
                .andDo(document("startStudySession",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(studyResource(
                                "학습 세션 시작 또는 이어풀기",
                                "같은 회원, 시험, 모드의 진행 중 세션이 있으면 재사용하고 resume 답안을 submittedAnswers 로 반환합니다."
                        )),
                        requestFields(
                                fieldWithPath("examId").type(JsonFieldType.NUMBER).description("시작하거나 이어풀 시험 ID"),
                                fieldWithPath("mode").type(JsonFieldType.STRING).description("학습 모드. EXAM 또는 PRACTICE")
                        ),
                        responseFields(
                                fieldWithPath("data.sessionId").type(JsonFieldType.NUMBER).description("재사용하거나 새로 생성한 학습 세션 ID"),
                                fieldWithPath("data.status").type(JsonFieldType.STRING).description("현재 세션 상태"),
                                fieldWithPath("data.lastIndex").type(JsonFieldType.NUMBER).description("마지막으로 저장된 문제 인덱스"),
                                fieldWithPath("data.submittedAnswers").type(JsonFieldType.ARRAY).description("이미 제출한 답안 목록. 첫 시작이면 빈 배열"),
                                fieldWithPath("data.submittedAnswers[].problemId").type(JsonFieldType.NUMBER).description("제출한 문제 ID"),
                                fieldWithPath("data.submittedAnswers[].choiceId").type(JsonFieldType.NUMBER).description("선택한 보기 ID"),
                                fieldWithPath("data.submittedAnswers[].isCorrect").type(JsonFieldType.BOOLEAN).description("서버가 저장한 authoritative 정오 여부")
                        )
                ));
    }

    @Test
    @DisplayName("study finish 응답은 counts 기반 요약을 반환하고 finalScore 를 유지한다")
    void finishExam_shouldReturnCountsAndKeepDeprecatedFinalScore() throws Exception {
        ExamFixture fixture = createExamFixture("회계학", "1차", 2025, 4);
        Long sessionId = startSession(fixture.exam().getId(), ExamMode.EXAM);

        submitAnswer(sessionId, fixture.problemIds().get(0), fixture.correctChoiceIds().get(0), 0);
        submitAnswer(sessionId, fixture.problemIds().get(1), fixture.correctChoiceIds().get(1), 1);
        submitAnswer(sessionId, fixture.problemIds().get(2), fixture.wrongChoiceIds().get(2), 2);

        mvc.perform(post("/api/v1/study/finish")
                        .with(user(memberDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId": %d
                                }
                                """.formatted(sessionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finalScore").value(2))
                .andExpect(jsonPath("$.data.correctCount").value(2))
                .andExpect(jsonPath("$.data.totalCount").value(4))
                .andExpect(jsonPath("$.data.answeredCount").value(3))
                .andExpect(jsonPath("$.data.unansweredCount").value(1))
                .andDo(document("finishStudySession",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(studyResource(
                                "학습 세션 종료",
                                "실전/연습 세션 종료 시 counts 기반 요약을 반환합니다. finalScore 는 하위호환용 deprecated 숫자 필드입니다."
                        )),
                        requestFields(
                                fieldWithPath("sessionId").type(JsonFieldType.NUMBER).description("종료할 학습 세션 ID")
                        ),
                        responseFields(
                                fieldWithPath("data.finalScore").type(JsonFieldType.NUMBER)
                                        .description("하위호환용 deprecated 필드. 현재 correctCount 와 같은 값을 유지한다"),
                                fieldWithPath("data.correctCount").type(JsonFieldType.NUMBER)
                                        .description("서버가 계산한 맞은 문제 수"),
                                fieldWithPath("data.totalCount").type(JsonFieldType.NUMBER)
                                        .description("세션 시작 시점에 스냅샷된 총 문제 수"),
                                fieldWithPath("data.answeredCount").type(JsonFieldType.NUMBER)
                                        .description("제출한 문제 수"),
                                fieldWithPath("data.unansweredCount").type(JsonFieldType.NUMBER)
                                        .description("미제출 문제 수")
                        )
                ));
    }

    @Test
    @DisplayName("중복 finish 요청은 같은 요약을 재응답하고 real 로그를 중복 적재하지 않는다")
    void finishExam_shouldBeIdempotentAndNotDoubleCountLogs() throws Exception {
        ExamFixture fixture = createExamFixture("세법", "1차", 2025, 3);
        Long sessionId = startSession(fixture.exam().getId(), ExamMode.EXAM);

        submitAnswer(sessionId, fixture.problemIds().get(0), fixture.correctChoiceIds().get(0), 0);
        submitAnswer(sessionId, fixture.problemIds().get(1), fixture.wrongChoiceIds().get(1), 1);

        ResultActions firstFinish = finishSession(sessionId);
        firstFinish
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finalScore").value(1))
                .andExpect(jsonPath("$.data.correctCount").value(1))
                .andExpect(jsonPath("$.data.totalCount").value(3))
                .andExpect(jsonPath("$.data.answeredCount").value(2))
                .andExpect(jsonPath("$.data.unansweredCount").value(1));

        ResultActions secondFinish = finishSession(sessionId);
        secondFinish
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finalScore").value(1))
                .andExpect(jsonPath("$.data.correctCount").value(1))
                .andExpect(jsonPath("$.data.totalCount").value(3))
                .andExpect(jsonPath("$.data.answeredCount").value(2))
                .andExpect(jsonPath("$.data.unansweredCount").value(1));

        awaitDailyLogCount(member.getId(), NOW.toLocalDate(), 2);

        mvc.perform(get("/api/v1/study-logs/yearly")
                        .with(user(memberDetails))
                        .param("year", String.valueOf(NOW.getYear())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].date").value("2026-03-15"))
                .andExpect(jsonPath("$.data[0].count").value(2))
                .andDo(document("getYearlyStudyLogs",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(studyResource(
                                "연간 학습 로그 조회",
                                "로그인 사용자의 학습 로그를 연간 기준으로 조회합니다. practice 는 세션 내 첫 제출만, real 은 finish 시 answeredCount 만큼 누적됩니다."
                        )),
                        queryParameters(
                                parameterWithName("year").description("조회 연도")
                        ),
                        responseFields(
                                fieldWithPath("data[].date").type(JsonFieldType.STRING).description("학습 일자"),
                                fieldWithPath("data[].count").type(JsonFieldType.NUMBER).description("해당 일자의 누적 학습 수")
                        )
                ));

        mvc.perform(get("/api/v1/study-logs/streak")
                        .with(user(memberDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.streak").value(1))
                .andDo(document("getStudyStreak",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(studyResource(
                                "학습 streak 조회",
                                "연속 학습 일수를 조회합니다. practice 와 real 로그를 모두 포함하지만 동일 세션의 중복 finish 는 다시 누적하지 않습니다."
                        )),
                        responseFields(
                                fieldWithPath("data.streak").type(JsonFieldType.NUMBER).description("현재 연속 학습 일수")
                        )
                ));
    }

    @Test
    @DisplayName("practice 는 세션 내 동일 문제 답 변경을 재집계하지 않고 yearly/streak 에 반영된다")
    void practiceSession_shouldAffectYearlyLogAndStreakWithoutDuplicateRecountPerProblem() throws Exception {
        ExamFixture fixture = createExamFixture("재무회계", "2차", 2025, 2);
        Long sessionId = startSession(fixture.exam().getId(), ExamMode.PRACTICE);

        submitAnswer(sessionId, fixture.problemIds().get(0), fixture.wrongChoiceIds().get(0), 0);
        submitAnswer(sessionId, fixture.problemIds().get(0), fixture.correctChoiceIds().get(0), 0);
        submitAnswer(sessionId, fixture.problemIds().get(1), fixture.correctChoiceIds().get(1), 1);

        awaitDailyLogCount(member.getId(), NOW.toLocalDate(), 2);

        finishSession(sessionId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finalScore").value(2))
                .andExpect(jsonPath("$.data.correctCount").value(2))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.answeredCount").value(2))
                .andExpect(jsonPath("$.data.unansweredCount").value(0));

        mvc.perform(get("/api/v1/study-logs/yearly")
                        .with(user(memberDetails))
                        .param("year", String.valueOf(NOW.getYear())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].count").value(2));

        mvc.perform(get("/api/v1/study-logs/streak")
                        .with(user(memberDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.streak").value(1));
    }

    private Long startSession(Long examId, ExamMode mode) throws Exception {
        ResultActions result = mvc.perform(post("/api/v1/study/exam/start")
                .with(user(memberDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new StartRequest(examId, mode))));

        result.andExpect(status().isOk());

        JsonNode response = objectMapper.readTree(result.andReturn().getResponse().getContentAsString());
        return response.path("data").path("sessionId").asLong();
    }

    private void submitAnswer(Long sessionId, Long problemId, Long choiceId, int index) throws Exception {
        mvc.perform(post("/api/v1/study/answer")
                        .with(user(memberDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId": %d,
                                  "problemId": %d,
                                  "choiceId": %d,
                                  "index": %d
                                }
                                """.formatted(sessionId, problemId, choiceId, index)))
                .andExpect(status().isOk());
    }

    private ResultActions finishSession(Long sessionId) throws Exception {
        return mvc.perform(post("/api/v1/study/finish")
                .with(user(memberDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "sessionId": %d
                        }
                        """.formatted(sessionId)));
    }

    private void awaitDailyLogCount(Long memberId, LocalDate date, int expectedCount) throws Exception {
        for (int attempt = 0; attempt < 20; attempt++) {
            DailyStudyLog log = dailyStudyLogJpaRepository.findByMemberIdAndDate(memberId, date).orElse(null);
            if (log != null && log.getSolvedCount() == expectedCount) {
                return;
            }
            Thread.sleep(100);
        }

        DailyStudyLog log = dailyStudyLogJpaRepository.findByMemberIdAndDate(memberId, date).orElse(null);
        throw new AssertionError("Expected daily log count %d but was %s".formatted(
                expectedCount,
                log == null ? "missing" : log.getSolvedCount()
        ));
    }

    private ExamFixture createExamFixture(String subjectName, String examName, int year, int problemCount) {
        Subject subject = subjectRepository.save(Subject.builder()
                .name(subjectName)
                .build());
        Exam exam = examRepository.save(Exam.builder()
                .name(examName)
                .year(year)
                .subjectId(subject.getId())
                .status(ExamStatus.PUBLISHED)
                .build());

        List<Long> problemIds = new ArrayList<>();
        List<Long> correctChoiceIds = new ArrayList<>();
        List<Long> wrongChoiceIds = new ArrayList<>();

        for (int index = 0; index < problemCount; index++) {
            Problem problem = problemRepository.save(Problem.builder()
                    .number(index + 1)
                    .content("문제 " + (index + 1))
                    .explanation("해설 " + (index + 1))
                    .exam(exam)
                    .build());

            Choice correctChoice = choiceRepository.save(Choice.builder()
                    .content("정답")
                    .number(1)
                    .isAnswer(true)
                    .problem(problem)
                    .build());
            Choice wrongChoice = choiceRepository.save(Choice.builder()
                    .content("오답")
                    .number(2)
                    .isAnswer(false)
                    .problem(problem)
                    .build());

            problemIds.add(problem.getId());
            correctChoiceIds.add(correctChoice.getId());
            wrongChoiceIds.add(wrongChoice.getId());
        }

        return new ExamFixture(exam, problemIds, correctChoiceIds, wrongChoiceIds);
    }

    private ResourceSnippetParameters studyResource(String summary, String description) {
        return ResourceSnippetParameters.builder()
                .tag("Study")
                .summary(summary)
                .description(description)
                .build();
    }

    private record StartRequest(Long examId, ExamMode mode) {
    }

    private record ExamFixture(
            Exam exam,
            List<Long> problemIds,
            List<Long> correctChoiceIds,
            List<Long> wrongChoiceIds
    ) {
    }
}
