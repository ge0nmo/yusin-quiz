package com.cpa.yusin.quiz.member.integration;

import com.cpa.yusin.quiz.answer.domain.Answer;
import com.cpa.yusin.quiz.answer.infrastructure.AnswerJpaRepository;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.cpa.yusin.quiz.bookmark.domain.Bookmark;
import com.cpa.yusin.quiz.bookmark.infrastructure.BookmarkJpaRepository;
import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.domain.ExamStatus;
import com.cpa.yusin.quiz.exam.infrastructure.ExamJpaRepository;
import com.cpa.yusin.quiz.global.details.MemberDetails;
import com.cpa.yusin.quiz.global.jwt.JwtService;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.infrastructure.MemberJpaRepository;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.infrastructure.ProblemJpaRepository;
import com.cpa.yusin.quiz.question.domain.Question;
import com.cpa.yusin.quiz.question.infrastructure.QuestionJpaRepository;
import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import com.cpa.yusin.quiz.study.domain.ExamMode;
import com.cpa.yusin.quiz.study.domain.StudySession;
import com.cpa.yusin.quiz.study.domain.SubmittedAnswer;
import com.cpa.yusin.quiz.study.infrastructure.DailyStudyLogJpaRepository;
import com.cpa.yusin.quiz.study.infrastructure.StudySessionJpaRepository;
import com.cpa.yusin.quiz.study.infrastructure.SubmittedAnswerJpaRepository;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.infrastructure.SubjectJpaRepository;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeAnswer;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeCycle;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipant;
import com.cpa.yusin.quiz.wordpractice.infrastructure.WordPracticeAnswerJpaRepository;
import com.cpa.yusin.quiz.wordpractice.infrastructure.WordPracticeCycleJpaRepository;
import com.cpa.yusin.quiz.wordpractice.infrastructure.WordPracticeParticipantJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, TeardownExtension.class})
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@SpringBootTest
class MemberWithdrawalTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberJpaRepository memberRepository;
    @Autowired
    private SubjectJpaRepository subjectRepository;
    @Autowired
    private ExamJpaRepository examRepository;
    @Autowired
    private ProblemJpaRepository problemRepository;
    @Autowired
    private QuestionJpaRepository questionRepository;
    @Autowired
    private AnswerJpaRepository answerRepository;
    @Autowired
    private BookmarkJpaRepository bookmarkRepository;
    @Autowired
    private StudySessionJpaRepository studySessionRepository;
    @Autowired
    private SubmittedAnswerJpaRepository submittedAnswerRepository;
    @Autowired
    private DailyStudyLogJpaRepository dailyStudyLogRepository;
    @Autowired
    private WordPracticeParticipantJpaRepository wordPracticeParticipantRepository;
    @Autowired
    private WordPracticeCycleJpaRepository wordPracticeCycleRepository;
    @Autowired
    private WordPracticeAnswerJpaRepository wordPracticeAnswerRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member member;
    private MemberDetails memberDetails;
    private Question question;
    private Answer answer;
    private String accessToken;
    private String refreshToken;

    @BeforeEach
    void setUp(WebApplicationContext context, RestDocumentationContextProvider restDocumentation) {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .apply(springSecurity())
                .build();

        member = memberRepository.save(Member.builder()
                .email("withdraw-me@example.com")
                .password("encoded-password")
                .username("탈퇴 전 사용자")
                .platform(Platform.GOOGLE)
                .role(Role.USER)
                .build());
        memberDetails = new MemberDetails(member, null);
        accessToken = jwtService.createAccessToken(member.getEmail(), member.getId());
        refreshToken = jwtService.createRefreshToken(member.getEmail(), member.getId());

        Subject subject = subjectRepository.save(Subject.builder().name("회원 탈퇴 테스트 과목").build());
        Exam exam = examRepository.save(Exam.builder()
                .name("회원 탈퇴 테스트 시험")
                .year(2026)
                .subjectId(subject.getId())
                .status(ExamStatus.PUBLISHED)
                .build());
        Problem problem = problemRepository.save(Problem.builder()
                .content("문제")
                .explanation("해설")
                .number(1)
                .exam(exam)
                .build());

        question = questionRepository.save(Question.builder()
                .member(member)
                .problem(problem)
                .title("남아야 하는 질문")
                .content("공개 질문 내용")
                .answerCount(1)
                .build());
        answer = answerRepository.save(Answer.builder()
                .member(member)
                .question(question)
                .content("남아야 하는 답변")
                .build());
        bookmarkRepository.save(Bookmark.create(member, problem));

        LocalDateTime now = LocalDateTime.now();
        StudySession studySession = studySessionRepository.save(
                StudySession.start(member, exam.getId(), ExamMode.PRACTICE, now, 1));
        submittedAnswerRepository.save(SubmittedAnswer.create(
                studySession, problem.getId(), 1L, true));
        dailyStudyLogRepository.save(DailyStudyLog.createWithCount(member, LocalDate.now(), 3));

        WordPracticeParticipant participant = wordPracticeParticipantRepository.save(
                WordPracticeParticipant.member(member.getId()));
        WordPracticeCycle cycle = wordPracticeCycleRepository.save(WordPracticeCycle.start(
                participant, subject.getId(), 1, "withdrawal-test-seed", List.of(problem.getId()), now));
        wordPracticeAnswerRepository.save(WordPracticeAnswer.create(
                cycle, problem.getId(), 1L, 1, true, now));
    }

    @Test
    void withdrawalDeletesPrivateDataAndAnonymizesPublicContent() throws Exception {
        mvc.perform(delete("/api/v1/members/me").with(user(memberDetails)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andDo(document("withdrawMember",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Member")
                                .summary("회원 탈퇴")
                                .description("개인 학습 데이터는 삭제하고 공개 질문·답변은 익명화한 뒤 회원을 즉시 삭제합니다.")
                                .build())));

        assertThat(memberRepository.findById(member.getId())).isEmpty();
        assertThat(memberRepository.existsByEmail("withdraw-me@example.com")).isFalse();
        assertThat(bookmarkRepository.count()).isZero();
        assertThat(studySessionRepository.count()).isZero();
        assertThat(submittedAnswerRepository.count()).isZero();
        assertThat(dailyStudyLogRepository.count()).isZero();
        assertThat(wordPracticeParticipantRepository.count()).isZero();
        assertThat(wordPracticeCycleRepository.count()).isZero();
        assertThat(wordPracticeAnswerRepository.count()).isZero();

        Long questionAuthorId = jdbcTemplate.queryForObject(
                "select member_id from question where id = ?", Long.class, question.getId());
        Long answerAuthorId = jdbcTemplate.queryForObject(
                "select member_id from answer where id = ?", Long.class, answer.getId());
        String anonymizedUsername = jdbcTemplate.queryForObject(
                "select username from member where id = ?", String.class, questionAuthorId);

        assertThat(questionAuthorId).isEqualTo(answerAuthorId);
        assertThat(questionAuthorId).isNotEqualTo(member.getId());
        assertThat(anonymizedUsername).isEqualTo(Member.WITHDRAWN_AUTHOR_USERNAME);
    }

    @Test
    void oldTokensCannotAuthenticateAfterSameEmailCreatesFreshAccount() throws Exception {
        mvc.perform(delete("/api/v1/members/me").with(user(memberDetails)))
                .andExpect(status().isNoContent());

        Member freshMember = memberRepository.save(Member.builder()
                .email("withdraw-me@example.com")
                .password("new-encoded-password")
                .username("새 사용자")
                .platform(Platform.GOOGLE)
                .role(Role.USER)
                .build());
        assertThat(freshMember.getId()).isNotEqualTo(member.getId());

        mvc.perform(get("/api/v1/study/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
    }
}
