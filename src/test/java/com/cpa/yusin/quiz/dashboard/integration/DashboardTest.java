package com.cpa.yusin.quiz.dashboard.integration;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import com.cpa.yusin.quiz.question.domain.Question;
import com.cpa.yusin.quiz.question.service.port.QuestionRepository;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import jakarta.persistence.EntityManager;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
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
class DashboardTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @MockBean
    private ClockHolder clockHolder;

    private Member questionAuthor;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .apply(springSecurity())
                .build();

        questionAuthor = memberRepository.save(Member.builder()
                .email("user@test.com")
                .password("encoded-password")
                .username("tester")
                .platform(Platform.HOME)
                .role(Role.USER)
                .build());

        given(clockHolder.getCurrentDateTime()).willReturn(LocalDateTime.of(2026, 3, 14, 10, 0));
    }

    @Test
    @DisplayName("관리자 대시보드 API는 soft delete 전파와 ClockHolder 날짜 경계를 반영해 집계를 반환한다")
    void getDashboard_success() throws Exception {
        Subject accounting = createSubject("회계학");
        Subject tax = createSubject("세법");
        Subject removedSubject = createSubject("삭제 과목");
        removeSubject(removedSubject);

        Exam accountingFirstExam = createExam(accounting, "1차", 2025);
        Exam accountingSecondExam = createExam(accounting, "2차", 2025);
        Exam taxExam = createExam(tax, "1차", 2024);
        Exam removedExam = createExam(accounting, "삭제 시험", 2023);
        removeExam(removedExam);
        Exam examUnderRemovedSubject = createExam(removedSubject, "유령 시험", 2022);

        Problem accountingProblemOne = createProblem(accountingFirstExam, 1, "https://www.youtube.com/watch?v=lecture-1");
        Problem accountingProblemTwo = createProblem(accountingFirstExam, 2, "   ");
        Problem accountingProblemThree = createProblem(accountingFirstExam, 3, null);
        Problem accountingProblemFour = createProblem(accountingSecondExam, 1, "https://www.youtube.com/watch?v=lecture-2");
        Problem taxProblemOne = createProblem(taxExam, 1, "");

        Problem removedProblem = createProblem(accountingFirstExam, 99, "https://www.youtube.com/watch?v=removed-problem");
        removeProblem(removedProblem);
        Problem problemUnderRemovedExam = createProblem(removedExam, 1, "");
        Problem problemUnderRemovedSubject = createProblem(examUnderRemovedSubject, 1, "");

        LocalDate currentDate = LocalDate.of(2026, 3, 14);
        LocalDateTime yesterdayBeforeMidnight = currentDate.minusDays(1).atTime(23, 59);
        LocalDateTime todayJustAfterMidnight = currentDate.atTime(0, 5);
        LocalDateTime todayMorning = currentDate.atTime(8, 0);
        LocalDateTime todayLatestTie = currentDate.atTime(9, 30);
        LocalDateTime twoDaysAgo = currentDate.minusDays(2).atTime(12, 0);

        Question olderPendingQuestion = createQuestion(accountingProblemTwo, "검토 요청 2", false, 1, yesterdayBeforeMidnight);
        Question newestPendingQuestionBeforeTie = createQuestion(accountingProblemThree, "검토 요청 4", false, 0, todayLatestTie);
        Question newestPendingQuestion = createQuestion(accountingProblemFour, "검토 요청 5", false, 2, todayLatestTie);
        Question todayPendingQuestion = createQuestion(accountingProblemOne, "검토 요청 3", false, 0, todayMorning);
        Question midnightPendingQuestion = createQuestion(taxProblemOne, "검토 요청 1", false, 0, todayJustAfterMidnight);
        createQuestion(taxProblemOne, "제외될 오래된 질문", false, 0, twoDaysAgo);
        createQuestion(accountingProblemOne, "답변 완료 질문", true, 1, currentDate.atTime(10, 0));

        Question removedQuestion = createQuestion(accountingProblemOne, "삭제된 질문", false, 0, currentDate.atTime(11, 0));
        removeQuestion(removedQuestion);

        createQuestion(removedProblem, "삭제된 문제의 질문", false, 0, currentDate.atTime(11, 10));
        createQuestion(problemUnderRemovedExam, "삭제된 시험의 질문", false, 0, currentDate.atTime(11, 20));
        createQuestion(problemUnderRemovedSubject, "삭제된 과목의 질문", false, 0, currentDate.atTime(11, 30));

        ResultActions resultActions = mvc.perform(get("/api/admin/dashboard")
                .with(user("admin@test.com").roles("ADMIN"))
                .queryParam("subjectId", accounting.getId().toString())
                .queryParam("examId", accountingFirstExam.getId().toString())
                .contentType(MediaType.APPLICATION_JSON));

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totals.subjectCount").value(2))
                .andExpect(jsonPath("$.data.totals.examCount").value(3))
                .andExpect(jsonPath("$.data.totals.problemCount").value(5))
                .andExpect(jsonPath("$.data.totals.questionCount").value(7))
                .andExpect(jsonPath("$.data.operations.todayQuestionCount").value(5))
                .andExpect(jsonPath("$.data.operations.unansweredQuestionCount").value(6))
                .andExpect(jsonPath("$.data.operations.problemsWithoutLectureCount").value(3))
                .andExpect(jsonPath("$.data.pendingQuestions", hasSize(5)))
                .andExpect(jsonPath("$.data.pendingQuestions[0].id").value(newestPendingQuestion.getId()))
                .andExpect(jsonPath("$.data.pendingQuestions[0].title").value("검토 요청 5"))
                .andExpect(jsonPath("$.data.pendingQuestions[1].id").value(newestPendingQuestionBeforeTie.getId()))
                .andExpect(jsonPath("$.data.pendingQuestions[1].title").value("검토 요청 4"))
                .andExpect(jsonPath("$.data.pendingQuestions[2].id").value(todayPendingQuestion.getId()))
                .andExpect(jsonPath("$.data.pendingQuestions[2].title").value("검토 요청 3"))
                .andExpect(jsonPath("$.data.pendingQuestions[3].id").value(midnightPendingQuestion.getId()))
                .andExpect(jsonPath("$.data.pendingQuestions[3].title").value("검토 요청 1"))
                .andExpect(jsonPath("$.data.pendingQuestions[4].id").value(olderPendingQuestion.getId()))
                .andExpect(jsonPath("$.data.pendingQuestions[4].title").value("검토 요청 2"))
                .andExpect(jsonPath("$.data.pendingQuestions[4].answerCount").value(1))
                .andExpect(jsonPath("$.data.context.subject.id").value(accounting.getId()))
                .andExpect(jsonPath("$.data.context.subject.name").value("회계학"))
                .andExpect(jsonPath("$.data.context.subject.examCount").value(2))
                .andExpect(jsonPath("$.data.context.subject.problemCount").value(4))
                .andExpect(jsonPath("$.data.context.exam.id").value(accountingFirstExam.getId()))
                .andExpect(jsonPath("$.data.context.exam.name").value("1차"))
                .andExpect(jsonPath("$.data.context.exam.year").value(2025))
                .andExpect(jsonPath("$.data.context.exam.problemCount").value(3))
                .andExpect(jsonPath("$.data.context.exam.questionCount").value(4))
                .andExpect(jsonPath("$.data.context.exam.unansweredQuestionCount").value(3))
                .andExpect(jsonPath("$.data.context.exam.lectureCoverageRate").value(33.3))
                .andDo(document("getAdminDashboard",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Dashboard")
                                .summary("관리자 대시보드 집계 조회")
                                .description("관리자 대시보드가 사용하는 전체 집계, 운영 지표, 최신 미답변 질문, 선택된 과목/시험 컨텍스트를 반환합니다.")
                                .build()),
                        queryParameters(
                                parameterWithName("subjectId").description("선택된 과목 ID. stale selection이면 subject context는 null").optional(),
                                parameterWithName("examId").description("선택된 시험 ID. stale selection이거나 subjectId와 불일치하면 exam context는 null").optional()
                        ),
                        responseFields(
                                fieldWithPath("data.totals").type(JsonFieldType.OBJECT).description("전체 활성 리소스 집계"),
                                fieldWithPath("data.totals.subjectCount").type(JsonFieldType.NUMBER).description("삭제되지 않은 과목 수"),
                                fieldWithPath("data.totals.examCount").type(JsonFieldType.NUMBER).description("삭제되지 않은 과목에 속한 활성 시험 수"),
                                fieldWithPath("data.totals.problemCount").type(JsonFieldType.NUMBER).description("삭제되지 않은 과목/시험에 속한 활성 문제 수"),
                                fieldWithPath("data.totals.questionCount").type(JsonFieldType.NUMBER).description("삭제되지 않은 과목/시험/문제에 속한 활성 질문 수"),
                                fieldWithPath("data.operations").type(JsonFieldType.OBJECT).description("운영 지표 집계"),
                                fieldWithPath("data.operations.todayQuestionCount").type(JsonFieldType.NUMBER).description("ClockHolder 현재 날짜 경계 기준 오늘 생성된 활성 질문 수"),
                                fieldWithPath("data.operations.unansweredQuestionCount").type(JsonFieldType.NUMBER).description("관리자 미답변 상태인 활성 질문 수"),
                                fieldWithPath("data.operations.problemsWithoutLectureCount").type(JsonFieldType.NUMBER).description("해설 강의 URL이 없거나 blank 인 활성 문제 수"),
                                fieldWithPath("data.pendingQuestions").type(JsonFieldType.ARRAY).description("최신 미답변 질문 최대 5건"),
                                fieldWithPath("data.pendingQuestions[].id").type(JsonFieldType.NUMBER).description("질문 ID"),
                                fieldWithPath("data.pendingQuestions[].title").type(JsonFieldType.STRING).description("질문 제목"),
                                fieldWithPath("data.pendingQuestions[].username").type(JsonFieldType.STRING).description("질문 작성자 닉네임"),
                                fieldWithPath("data.pendingQuestions[].createdAt").type(JsonFieldType.STRING).description("질문 생성 시각"),
                                fieldWithPath("data.pendingQuestions[].answerCount").type(JsonFieldType.NUMBER).description("질문 답변 수"),
                                fieldWithPath("data.pendingQuestions[].problemId").type(JsonFieldType.NUMBER).description("질문이 속한 문제 ID"),
                                fieldWithPath("data.context").type(JsonFieldType.OBJECT).description("선택된 과목/시험 컨텍스트"),
                                fieldWithPath("data.context.subject").type(JsonFieldType.OBJECT).description("선택된 활성 과목 컨텍스트"),
                                fieldWithPath("data.context.subject.id").type(JsonFieldType.NUMBER).description("과목 ID"),
                                fieldWithPath("data.context.subject.name").type(JsonFieldType.STRING).description("과목명"),
                                fieldWithPath("data.context.subject.examCount").type(JsonFieldType.NUMBER).description("과목에 속한 활성 시험 수"),
                                fieldWithPath("data.context.subject.problemCount").type(JsonFieldType.NUMBER).description("과목에 속한 활성 문제 수"),
                                fieldWithPath("data.context.exam").type(JsonFieldType.OBJECT).description("선택된 활성 시험 컨텍스트"),
                                fieldWithPath("data.context.exam.id").type(JsonFieldType.NUMBER).description("시험 ID"),
                                fieldWithPath("data.context.exam.name").type(JsonFieldType.STRING).description("시험명"),
                                fieldWithPath("data.context.exam.year").type(JsonFieldType.NUMBER).description("시험 연도"),
                                fieldWithPath("data.context.exam.problemCount").type(JsonFieldType.NUMBER).description("시험에 속한 활성 문제 수"),
                                fieldWithPath("data.context.exam.questionCount").type(JsonFieldType.NUMBER).description("시험에 속한 활성 질문 수"),
                                fieldWithPath("data.context.exam.unansweredQuestionCount").type(JsonFieldType.NUMBER).description("시험에 속한 관리자 미답변 질문 수"),
                                fieldWithPath("data.context.exam.lectureCoverageRate").type(JsonFieldType.NUMBER).description("활성 문제 대비 해설 강의 연결 비율(%)")
                        )));
    }

    @Test
    @DisplayName("stale subjectId 와 examId 는 200 응답과 null context 로 처리한다")
    void getDashboard_staleSelection() throws Exception {
        mvc.perform(get("/api/admin/dashboard")
                        .with(user("admin@test.com").roles("ADMIN"))
                        .queryParam("subjectId", "999999")
                        .queryParam("examId", "888888"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.context.subject").value(nullValue()))
                .andExpect(jsonPath("$.data.context.exam").value(nullValue()));
    }

    @Test
    @DisplayName("ROLE_USER 는 관리자 대시보드 API 에 접근할 수 없다")
    void getDashboard_forbiddenForUserRole() throws Exception {
        mvc.perform(get("/api/admin/dashboard")
                        .with(user("user@test.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ROLE_ADMIN 은 관리자 대시보드 API 에 접근할 수 있다")
    void getDashboard_allowedForAdminRole() throws Exception {
        mvc.perform(get("/api/admin/dashboard")
                        .with(user("admin@test.com").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    private Subject createSubject(String name) {
        return subjectRepository.save(Subject.builder()
                .name(name)
                .build());
    }

    private Exam createExam(Subject subject, String name, int year) {
        return examRepository.save(Exam.builder()
                .name(name)
                .year(year)
                .subjectId(subject.getId())
                .build());
    }

    private Problem createProblem(Exam exam, int number, String lectureYoutubeUrl) {
        return problemRepository.save(Problem.builder()
                .content("문제 " + number)
                .explanation("해설 " + number)
                .number(number)
                .lectureYoutubeUrl(lectureYoutubeUrl)
                .exam(exam)
                .build());
    }

    private Question createQuestion(Problem problem, String title, boolean answeredByAdmin, int answerCount, LocalDateTime createdAt) {
        Question question = questionRepository.save(Question.builder()
                .title(title)
                .content(title + " 내용")
                .member(questionAuthor)
                .answeredByAdmin(answeredByAdmin)
                .answerCount(answerCount)
                .problem(problem)
                .build());

        jdbcTemplate.update(
                "UPDATE question SET created_at = ?, updated_at = ? WHERE id = ?",
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt),
                question.getId()
        );
        entityManager.clear();

        return question;
    }

    private void removeSubject(Subject subject) {
        subject.delete();
        subjectRepository.save(subject);
    }

    private void removeExam(Exam exam) {
        exam.delete();
        examRepository.save(exam);
    }

    private void removeProblem(Problem problem) {
        problem.delete();
        problemRepository.save(problem);
    }

    private void removeQuestion(Question question) {
        question.delete();
        questionRepository.save(question);
    }
}
