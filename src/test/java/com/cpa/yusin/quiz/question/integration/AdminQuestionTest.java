package com.cpa.yusin.quiz.question.integration;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.global.details.MemberDetails;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.given;

@ExtendWith(TeardownExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
class AdminQuestionTest {

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

    @MockBean
    private ClockHolder clockHolder;

    private Problem problem;
    private Member adminMember;
    private MemberDetails adminMemberDetails;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        Subject subject = subjectRepository.save(Subject.builder()
                .name("세법")
                .build());

        Exam exam = examRepository.save(Exam.builder()
                .name("1차")
                .year(2026)
                .subjectId(subject.getId())
                .build());

        problem = problemRepository.save(Problem.builder()
                .number(1)
                .content("문제 본문")
                .explanation("해설")
                .exam(exam)
                .build());

        adminMember = memberRepository.save(Member.builder()
                .email("admin@test.com")
                .password("encoded-password")
                .username("admin")
                .platform(Platform.HOME)
                .role(Role.ADMIN)
                .build());
        adminMemberDetails = new MemberDetails(adminMember, null);

        given(clockHolder.getCurrentDateTime()).willReturn(LocalDateTime.of(2026, 3, 14, 10, 0));
    }

    @Test
    @DisplayName("관리자 질문 목록은 createdAt desc 기본 정렬과 pageInfo를 유지해야 한다")
    void getQuestions_shouldApplyDefaultCreatedAtDescOrder() throws Exception {
        Member author = createMember("latest@test.com", "latest-user");

        createQuestion(author, "가장 오래된 질문", "old-content", false, LocalDateTime.of(2026, 3, 10, 9, 0));
        createQuestion(author, "중간 질문", "mid-content", false, LocalDateTime.of(2026, 3, 11, 9, 0));
        createQuestion(author, "가장 최신 질문", "new-content", true, LocalDateTime.of(2026, 3, 12, 9, 0));

        mvc.perform(get("/api/admin/question")
                        .with(user(adminMemberDetails))
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("가장 최신 질문"))
                .andExpect(jsonPath("$.data[1].title").value("중간 질문"))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(3))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(2))
                .andExpect(jsonPath("$.pageInfo.currentPage").value(1))
                .andExpect(jsonPath("$.pageInfo.pageSize").value(2));
    }

    @Test
    @DisplayName("관리자 질문 목록은 status 로 답변 여부를 필터링해야 한다")
    void getQuestions_shouldFilterByStatus() throws Exception {
        Member author = createMember("status@test.com", "status-user");

        createQuestion(author, "답변 완료 질문", "answered-content", true, LocalDateTime.of(2026, 3, 10, 9, 0));
        createQuestion(author, "미답변 질문", "pending-content", false, LocalDateTime.of(2026, 3, 11, 9, 0));

        mvc.perform(get("/api/admin/question")
                        .with(user(adminMemberDetails))
                        .param("status", "UNANSWERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("미답변 질문"))
                .andExpect(jsonPath("$.data[0].answeredByAdmin").value(false));

        mvc.perform(get("/api/admin/question")
                        .with(user(adminMemberDetails))
                        .param("status", "ANSWERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("답변 완료 질문"))
                .andExpect(jsonPath("$.data[0].answeredByAdmin").value(true));
    }

    @Test
    @DisplayName("관리자 질문 목록 keyword 는 title content username email 대상 검색을 지원해야 한다")
    void getQuestions_shouldSearchAcrossSupportedKeywordFields() throws Exception {
        Member titleAuthor = createMember("title@test.com", "title-user");
        Member emailAuthor = createMember("alpha.search@test.com", "alpha-user");
        Member usernameAuthor = createMember("username@test.com", "keyword-owner");

        createQuestion(titleAuthor, "환급 일정 문의", "본문", false, LocalDateTime.of(2026, 3, 10, 9, 0));
        createQuestion(titleAuthor, "기타 질문", "환급 처리 기간이 궁금합니다", false, LocalDateTime.of(2026, 3, 10, 10, 0));
        createQuestion(emailAuthor, "이메일 검색 대상", "본문", false, LocalDateTime.of(2026, 3, 10, 11, 0));
        createQuestion(usernameAuthor, "사용자명 검색 대상", "본문", false, LocalDateTime.of(2026, 3, 10, 12, 0));

        mvc.perform(get("/api/admin/question")
                        .with(user(adminMemberDetails))
                        .param("keyword", "환급"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        mvc.perform(get("/api/admin/question")
                        .with(user(adminMemberDetails))
                        .param("keyword", "alpha.search@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("이메일 검색 대상"))
                .andExpect(jsonPath("$.data[0].email").value("alpha.search@test.com"));

        mvc.perform(get("/api/admin/question")
                        .with(user(adminMemberDetails))
                        .param("keyword", "keyword-owner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("사용자명 검색 대상"))
                .andExpect(jsonPath("$.data[0].username").value("keyword-owner"));
    }

    @Test
    @DisplayName("관리자 질문 목록은 datePreset=TODAY 를 ClockHolder 날짜 경계와 동일하게 적용해야 한다")
    void getQuestions_shouldFilterByTodayPresetWithDashboardAlignedBoundary() throws Exception {
        Member author = createMember("today@test.com", "today-user");

        createQuestion(author, "어제 질문", "yesterday", false, LocalDateTime.of(2026, 3, 13, 23, 59));
        createQuestion(author, "오늘 첫 질문", "today-1", false, LocalDateTime.of(2026, 3, 14, 0, 1));
        createQuestion(author, "오늘 답변 완료 질문", "today-2", true, LocalDateTime.of(2026, 3, 14, 9, 30));

        mvc.perform(get("/api/admin/question")
                        .with(user(adminMemberDetails))
                        .param("page", "0")
                        .param("size", "20")
                        .param("datePreset", "TODAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("오늘 답변 완료 질문"))
                .andExpect(jsonPath("$.data[1].title").value("오늘 첫 질문"))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(2));

        mvc.perform(get("/api/admin/question")
                        .with(user(adminMemberDetails))
                        .param("page", "0")
                        .param("size", "20")
                        .param("datePreset", "TODAY")
                        .param("status", "UNANSWERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("오늘 첫 질문"))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(1));

        mvc.perform(get("/api/admin/dashboard")
                        .with(user(adminMemberDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.operations.todayQuestionCount").value(2));
    }

    private Member createMember(String email, String username) {
        return memberRepository.save(Member.builder()
                .email(email)
                .password("encoded-password")
                .username(username)
                .platform(Platform.HOME)
                .role(Role.USER)
                .build());
    }

    private Question createQuestion(Member author, String title, String content, boolean answeredByAdmin, LocalDateTime createdAt) {
        Question question = questionRepository.save(Question.builder()
                .title(title)
                .content(content)
                .member(author)
                .answeredByAdmin(answeredByAdmin)
                .answerCount(answeredByAdmin ? 1 : 0)
                .problem(problem)
                .build());

        jdbcTemplate.update(
                "UPDATE question SET created_at = ?, updated_at = ? WHERE id = ?",
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt),
                question.getId()
        );

        return question;
    }
}
