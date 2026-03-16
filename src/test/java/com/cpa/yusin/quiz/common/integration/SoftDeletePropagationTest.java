package com.cpa.yusin.quiz.common.integration;

import com.cpa.yusin.quiz.answer.domain.Answer;
import com.cpa.yusin.quiz.answer.service.port.AnswerRepository;
import com.cpa.yusin.quiz.bookmark.domain.Bookmark;
import com.cpa.yusin.quiz.bookmark.service.port.BookmarkRepository;
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
import com.cpa.yusin.quiz.question.domain.Question;
import com.cpa.yusin.quiz.question.service.port.QuestionRepository;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.domain.SubjectStatus;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(TeardownExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
class SoftDeletePropagationTest {

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
    private AnswerRepository answerRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private MemberRepository memberRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Member member;
    private MemberDetails memberDetails;
    private Member adminMember;
    private MemberDetails adminMemberDetails;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        member = memberRepository.save(Member.builder()
                .email("user@test.com")
                .password("encoded-password")
                .username("tester")
                .platform(Platform.HOME)
                .role(Role.USER)
                .build());
        memberDetails = new MemberDetails(member, null);

        adminMember = memberRepository.save(Member.builder()
                .email("admin@test.com")
                .password("encoded-password")
                .username("admin")
                .platform(Platform.HOME)
                .role(Role.ADMIN)
                .build());
        adminMemberDetails = new MemberDetails(adminMember, null);
    }

    @Test
    @DisplayName("과목 삭제 후 하위 시험/문제/질문/답변은 사용자 API에서 보이지 않아야 한다")
    void subjectDeletionShouldHideDescendantsFromUserApis() throws Exception {
        Subject subject = createSubject("회계학");
        Exam exam = createExam(subject, "1차", 2025);
        Problem problem = createProblem(exam, 1);
        Question question = createQuestion(problem, "질문");
        answerRepository.save(Answer.builder()
                .member(member)
                .content("답변")
                .question(question)
                .build());
        bookmarkRepository.save(Bookmark.create(member, problem));

        deleteSubject(subject);

        mvc.perform(get("/api/v1/subject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        mvc.perform(get("/api/v1/exam")
                        .param("subjectId", subject.getId().toString()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/exam/year")
                        .param("subjectId", subject.getId().toString()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/problem")
                        .param("examId", exam.getId().toString()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v2/problem")
                        .param("examId", exam.getId().toString()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/problem/{id}", problem.getId()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/question/{questionId}", question.getId()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/question/{questionId}/answer", question.getId()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/bookmarks/problems")
                        .with(user(memberDetails))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("문제 삭제 후 질문 생성/목록과 북마크 생성은 차단되어야 한다")
    void deletedProblemShouldRejectQuestionAndBookmarkCreation() throws Exception {
        Subject subject = createSubject("세법");
        Exam exam = createExam(subject, "1차", 2025);
        Problem problem = createProblem(exam, 1);

        deleteProblem(problem);

        mvc.perform(get("/api/v1/problem/{id}", problem.getId()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/problem/{problemId}/question", problem.getId())
                        .with(user(memberDetails)))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/v1/problem/{problemId}/question", problem.getId())
                        .with(user(memberDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "삭제된 문제 질문",
                                  "content": "질문 내용"
                                }
                                """))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/v1/bookmarks/{problemId}", problem.getId())
                        .with(user(memberDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Draft 과목은 유저 API에서 숨기고 관리자 API에서는 그대로 조회할 수 있어야 한다")
    void draftSubjectShouldHideDescendantsFromUserApisButRemainVisibleToAdmins() throws Exception {
        Subject subject = createSubject("임시 과목", SubjectStatus.DRAFT);
        Exam exam = createExam(subject, "모의고사", 2026);
        Problem problem = createProblem(exam, 1);
        Question question = createQuestion(problem, "draft 질문");
        answerRepository.save(Answer.builder()
                .member(member)
                .content("draft 답변")
                .question(question)
                .build());
        bookmarkRepository.save(Bookmark.create(member, problem));

        mvc.perform(get("/api/v1/subject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        mvc.perform(get("/api/v1/exam")
                        .param("subjectId", subject.getId().toString()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/exam/year")
                        .param("subjectId", subject.getId().toString()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/problem")
                        .param("examId", exam.getId().toString()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v2/problem")
                        .param("examId", exam.getId().toString()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/problem/{id}", problem.getId()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/question/{questionId}", question.getId()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/question/{questionId}/answer", question.getId()))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/v1/problem/{problemId}/question", problem.getId())
                        .with(user(memberDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "draft 질문 생성",
                                  "content": "질문 내용"
                                }
                                """))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/v1/bookmarks/{problemId}", problem.getId())
                        .with(user(memberDetails)))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/bookmarks/problems")
                        .with(user(memberDetails))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());

        mvc.perform(get("/api/admin/subject")
                        .with(user(adminMemberDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(subject.getId()))
                .andExpect(jsonPath("$[0].status").value("DRAFT"));

        mvc.perform(get("/api/v2/admin/problem")
                        .with(user(adminMemberDetails))
                        .param("examId", exam.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(problem.getId()));
    }

    @Test
    @DisplayName("Draft 시험은 유저 API에서 숨기고 관리자 API에서는 그대로 조회할 수 있어야 한다")
    void draftExamShouldHideDescendantsFromUserApisButRemainVisibleToAdmins() throws Exception {
        Subject subject = createSubject("공개 과목");
        Exam exam = createExam(subject, "임시 시험", 2026, ExamStatus.DRAFT);
        Problem problem = createProblem(exam, 1);
        Question question = createQuestion(problem, "draft exam 질문");
        answerRepository.save(Answer.builder()
                .member(member)
                .content("draft exam 답변")
                .question(question)
                .build());
        bookmarkRepository.save(Bookmark.create(member, problem));

        mvc.perform(get("/api/v1/subject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(subject.getId()));

        mvc.perform(get("/api/v1/exam")
                        .param("subjectId", subject.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        mvc.perform(get("/api/v1/exam/year")
                        .param("subjectId", subject.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        mvc.perform(get("/api/v1/problem")
                        .param("examId", exam.getId().toString()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v2/problem")
                        .param("examId", exam.getId().toString()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/problem/{id}", problem.getId()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/question/{questionId}", question.getId()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/question/{questionId}/answer", question.getId()))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/v1/problem/{problemId}/question", problem.getId())
                        .with(user(memberDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "draft exam 질문 생성",
                                  "content": "질문 내용"
                                }
                                """))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/v1/bookmarks/{problemId}", problem.getId())
                        .with(user(memberDetails)))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/v1/study/exam/start")
                        .with(user(memberDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new com.cpa.yusin.quiz.study.controller.dto.request.ExamStartRequest(
                                exam.getId(),
                                com.cpa.yusin.quiz.study.domain.ExamMode.EXAM
                        ))))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/bookmarks/problems")
                        .with(user(memberDetails))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());

        mvc.perform(get("/api/admin/subject/{subjectId}/exam", subject.getId())
                        .with(user(adminMemberDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(exam.getId()))
                .andExpect(jsonPath("$.data[0].status").value("DRAFT"));

        mvc.perform(get("/api/v2/admin/problem")
                        .with(user(adminMemberDetails))
                        .param("examId", exam.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(problem.getId()));
    }

    @Test
    @DisplayName("질문 삭제 후 답변 생성과 목록 조회는 차단되어야 한다")
    void deletedQuestionShouldRejectAnswerAccess() throws Exception {
        Subject subject = createSubject("원가관리");
        Exam exam = createExam(subject, "1차", 2025);
        Problem problem = createProblem(exam, 1);
        Question question = createQuestion(problem, "삭제될 질문");

        deleteQuestion(question);

        mvc.perform(get("/api/v1/question/{questionId}/answer", question.getId()))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/v1/question/{questionId}/answer", question.getId())
                        .with(user(memberDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "답변 내용"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("관리자 API도 삭제된 상위 아래 데이터는 direct ID 조회와 목록에서 차단해야 한다")
    void adminApisShouldRejectDeletedDescendants() throws Exception {
        Subject activeSubject = createSubject("재무관리");
        Exam activeExam = createExam(activeSubject, "1차", 2025);
        Problem activeProblem = createProblem(activeExam, 1);
        createQuestion(activeProblem, "활성 질문");

        Subject deletedSubject = createSubject("삭제 과목");
        Exam deletedExam = createExam(deletedSubject, "삭제 시험", 2024);
        Problem deletedProblem = createProblem(deletedExam, 2);
        Question deletedQuestion = createQuestion(deletedProblem, "삭제 계층 질문");

        deleteSubject(deletedSubject);

        mvc.perform(get("/api/v2/admin/problem")
                        .with(user(adminMemberDetails))
                        .param("examId", deletedExam.getId().toString()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/admin/question/{id}", deletedQuestion.getId())
                        .with(user(adminMemberDetails)))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/admin/question/{questionId}/answer", deletedQuestion.getId())
                        .with(user(adminMemberDetails)))
                .andExpect(status().isNotFound());

        mvc.perform(post("/api/admin/question/{questionId}/answer", deletedQuestion.getId())
                        .with(user(adminMemberDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "content": "관리자 답변"
                                }
                                """))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/admin/question")
                        .with(user(adminMemberDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("활성 질문"));

        mvc.perform(get("/api/v2/admin/problem/search")
                        .with(user(adminMemberDetails))
                        .param("page", "0")
                        .param("size", "20")
                        .param("lectureStatus", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(activeProblem.getId()))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(1));
    }

    @Test
    @DisplayName("삭제된 시험은 학습 시작을 막아야 한다")
    void deletedExamShouldRejectStudyStart() throws Exception {
        Subject subject = createSubject("상법");
        Exam exam = createExam(subject, "1차", 2025);

        deleteExam(exam);

        mvc.perform(post("/api/v1/study/exam/start")
                        .with(user(memberDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new com.cpa.yusin.quiz.study.controller.dto.request.ExamStartRequest(exam.getId(), com.cpa.yusin.quiz.study.domain.ExamMode.EXAM))))
                .andExpect(status().isNotFound());
    }

    private Subject createSubject(String name) {
        return subjectRepository.save(Subject.builder()
                .name(name)
                .build());
    }

    private Subject createSubject(String name, SubjectStatus status) {
        return subjectRepository.save(Subject.builder()
                .name(name)
                .status(status)
                .build());
    }

    private Exam createExam(Subject subject, String name, int year) {
        return createExam(subject, name, year, ExamStatus.PUBLISHED);
    }

    private Exam createExam(Subject subject, String name, int year, ExamStatus status) {
        return examRepository.save(Exam.builder()
                .name(name)
                .year(year)
                .subjectId(subject.getId())
                .status(status)
                .build());
    }

    private Problem createProblem(Exam exam, int number) {
        return problemRepository.save(Problem.builder()
                .number(number)
                .content("content")
                .explanation("explanation")
                .exam(exam)
                .build());
    }

    private Question createQuestion(Problem problem, String title) {
        return questionRepository.save(Question.builder()
                .title(title)
                .content("content")
                .member(member)
                .answerCount(0)
                .answeredByAdmin(false)
                .problem(problem)
                .build());
    }

    private void deleteSubject(Subject subject) {
        subject.delete(1L);
        subjectRepository.save(subject);
    }

    private void deleteExam(Exam exam) {
        exam.delete(2L);
        examRepository.save(exam);
    }

    private void deleteProblem(Problem problem) {
        problem.delete();
        problemRepository.save(problem);
    }

    private void deleteQuestion(Question question) {
        question.delete();
        questionRepository.save(question);
    }
}
