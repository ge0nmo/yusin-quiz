package com.cpa.yusin.quiz.wordpractice.integration;

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
import com.cpa.yusin.quiz.problem.domain.block.TextBlock;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.domain.SubjectStatus;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeCycle;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipant;
import com.cpa.yusin.quiz.wordpractice.service.GuestTokenHasher;
import com.cpa.yusin.quiz.wordpractice.service.port.WordPracticeCycleRepository;
import com.cpa.yusin.quiz.wordpractice.service.port.WordPracticeParticipantRepository;
import com.cpa.yusin.quiz.wordpractice.infrastructure.WordPracticeCycleJpaRepository;
import com.cpa.yusin.quiz.wordpractice.infrastructure.WordPracticeAnswerJpaRepository;
import com.cpa.yusin.quiz.wordpractice.infrastructure.WordPracticeParticipantJpaRepository;
import com.cpa.yusin.quiz.wordpractice.controller.port.WordPracticeService;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeCycleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.util.ArrayList;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 실제 HTTP 경로에서 비회원·guest·회원 진행률과 guest token 검증을 확인한다. */
@ExtendWith(TeardownExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class WordPracticeApiTest {

    @Autowired private MockMvc mvc;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private ExamRepository examRepository;
    @Autowired private ProblemRepository problemRepository;
    @Autowired private ChoiceRepository choiceRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private WordPracticeParticipantRepository participantRepository;
    @Autowired private WordPracticeCycleRepository cycleRepository;
    @Autowired private GuestTokenHasher guestTokenHasher;
    @Autowired private WordPracticeCycleJpaRepository cycleJpaRepository;
    @Autowired private WordPracticeAnswerJpaRepository answerJpaRepository;
    @Autowired private WordPracticeParticipantJpaRepository participantJpaRepository;
    @Autowired private WordPracticeService wordPracticeService;

    private Subject subject;
    private Member member;
    private Exam wordExam;

    @BeforeEach
    void setUp() {
        subject = subjectRepository.save(Subject.builder().name("감정평가사").status(SubjectStatus.PUBLISHED).build());
        Subject draft = subjectRepository.save(Subject.builder().name("임시 과목").status(SubjectStatus.DRAFT).build());
        wordExam = examRepository.save(Exam.builder().name("2025 1차").year(2025)
                .subjectId(subject.getId()).status(ExamStatus.PUBLISHED).build());
        Exam draftExam = examRepository.save(Exam.builder().name("임시 시험").year(2025)
                .subjectId(draft.getId()).status(ExamStatus.PUBLISHED).build());
        Problem firstProblem = problemRepository.save(Problem.builder().number(1).exam(wordExam).requiresCalculation(false)
                .contentJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanationJson(List.of(TextBlock.builder().type("text").tag("p").build())).build());
        choiceRepository.save(Choice.builder().problem(firstProblem).number(1).content("보기 1")
                .isAnswer(true).explanationJson(List.of()).build());
        problemRepository.save(Problem.builder().number(1).exam(draftExam).requiresCalculation(false).build());
        member = memberRepository.save(Member.builder().email("word-practice@example.com").password("password")
                .username("word-practice").platform(Platform.HOME).role(Role.USER).build());
    }

    @Test
    void anonymousRequestReturnsZeroProgressAndDocumentsContract() throws Exception {
        mvc.perform(get("/api/v2/problem/word-practice/subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].subjectId").value(subject.getId()))
                .andExpect(jsonPath("$.data[0].solvedCount").value(0))
                .andExpect(jsonPath("$.data[0].totalCount").value(1))
                .andExpect(jsonPath("$.data[0].remainingCount").value(1))
                .andExpect(jsonPath("$.data[0].status").value("NOT_STARTED"))
                .andDo(document("getWordPracticeSubjects",
                        requestHeaders(
                                headerWithName("X-Guest-Token")
                                        .description("선택 사항. 서버가 발급한 익명 참여자 UUID token")
                                        .optional()
                        ),
                        responseFields(
                        fieldWithPath("data").description("공개 subject별 말문제 진행률 목록").type(JsonFieldType.ARRAY),
                        fieldWithPath("data[].subjectId").description("subject 식별자").type(JsonFieldType.NUMBER),
                        fieldWithPath("data[].subjectName").description("subject 이름").type(JsonFieldType.STRING),
                        fieldWithPath("data[].solvedCount").description("최신 회차에서 답안을 최초 제출한 문제 수").type(JsonFieldType.NUMBER),
                        fieldWithPath("data[].totalCount").description("회차 스냅샷 또는 현재 공개 말문제 수").type(JsonFieldType.NUMBER),
                        fieldWithPath("data[].remainingCount").description("아직 풀지 않은 문제 수").type(JsonFieldType.NUMBER),
                        fieldWithPath("data[].status").description("NOT_STARTED, IN_PROGRESS, COMPLETED").type(JsonFieldType.STRING)
                        )));
    }

    @Test
    void guestAndMemberProgressAreSeparatedAndMemberWinsOverGuestHeader() throws Exception {
        String guestToken = "c0a8012e-24b5-4f48-98b3-5619aac15af1";
        WordPracticeParticipant guest = participantRepository.saveAndFlush(WordPracticeParticipant.guest(guestTokenHasher.hash(guestToken)));
        WordPracticeParticipant memberParticipant = participantRepository.saveAndFlush(WordPracticeParticipant.member(member.getId()));
        cycleRepository.save(WordPracticeCycle.start(guest, subject.getId(), 1, "guest-seed", List.of(1L, 2L), LocalDateTime.now()));
        WordPracticeCycle memberCycle = WordPracticeCycle.start(memberParticipant, subject.getId(), 1, "member-seed", List.of(1L, 2L), LocalDateTime.now());
        memberCycle.markAnswered(true);
        cycleRepository.save(memberCycle);

        mvc.perform(get("/api/v2/problem/word-practice/subjects").header("X-Guest-Token", guestToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].solvedCount").value(0))
                .andExpect(jsonPath("$.data[0].totalCount").value(2));

        mvc.perform(get("/api/v2/problem/word-practice/subjects").header("X-Guest-Token", guestToken)
                        .with(SecurityMockMvcRequestPostProcessors.user(new MemberDetails(member, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].solvedCount").value(1))
                .andExpect(jsonPath("$.data[0].totalCount").value(2))
                .andExpect(jsonPath("$.data[0].status").value("IN_PROGRESS"));
    }

    @Test
    void invalidGuestTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/v2/problem/word-practice/subjects").header("X-Guest-Token", "not-a-uuid"))
                .andExpect(status().isUnauthorized())
                // 잘못된 UUID token을 앱이 폐기하고 새 token 발급 흐름으로 전환할 수 있게 HTTP 계약을 남긴다.
                .andDo(document("wordPracticeInvalidGuestToken"));
    }

    @Test
    void firstGuestStartIssuesTokenAndReturnsCycleResponseShape() throws Exception {
        mvc.perform(post("/api/v2/problem/word-practice/subjects/{subjectId}/cycle", subject.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cycleId").isNumber())
                .andExpect(jsonPath("$.data.subjectId").value(subject.getId()))
                .andExpect(jsonPath("$.data.roundNumber").value(1))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.issuedGuestToken").isString())
                .andExpect(jsonPath("$.data.progress.solvedCount").value(0))
                .andExpect(jsonPath("$.data.progress.totalCount").value(1))
                .andDo(document("postWordPracticeCycle",
                        requestHeaders(
                                headerWithName("X-Guest-Token")
                                        .description("선택 사항. 기존 익명 회차를 이어 풀 때 전달하는 UUID token")
                                        .optional()
                        ),
                        pathParameters(
                                parameterWithName("subjectId").description("말문제 회차를 시작하거나 이어 풀 subject ID")
                        ),
                        responseFields(
                        fieldWithPath("data.cycleId").description("생성 또는 재사용된 회차 식별자").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.subjectId").description("선택한 subject 식별자").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.roundNumber").description("subject별 회차 번호").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.status").description("회차 상태").type(JsonFieldType.STRING),
                        fieldWithPath("data.issuedGuestToken").description("최초 익명 시작 시에만 발급되는 token").type(JsonFieldType.STRING),
                        fieldWithPath("data.progress").description("회차 진행률").type(JsonFieldType.OBJECT),
                        fieldWithPath("data.progress.solvedCount").description("최초 답안을 제출한 문제 수").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.progress.correctCount").description("정답 수").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.progress.incorrectCount").description("오답 수").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.progress.totalCount").description("회차 시작 시 스냅샷 문제 수").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.progress.remainingCount").description("남은 문제 수").type(JsonFieldType.NUMBER)
                        )));
    }

    @Test
    void failedFirstGuestStartRollsBackNewParticipant() {
        Subject emptySubject = subjectRepository.save(
                Subject.builder().name("문제 없는 공개 과목").status(SubjectStatus.PUBLISHED).build());
        long participantCountBeforeStart = participantJpaRepository.count();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> wordPracticeService.startOrResumeCycle(null, null, emptySubject.getId()))
                .isInstanceOf(com.cpa.yusin.quiz.global.exception.WordPracticeException.class);

        org.assertj.core.api.Assertions.assertThat(participantJpaRepository.count())
                .isEqualTo(participantCountBeforeStart);
    }

    /** JWT 회원은 guest token 없이도 자신의 최신 회차를 생성·재사용하며 token 필드가 없어야 한다. */
    @Test
    void memberStartAndResumeUsesMemberIdentityOnly() throws Exception {
        mvc.perform(post("/api/v2/problem/word-practice/subjects/{subjectId}/cycle", subject.getId())
                        .header("X-Guest-Token", "2f10c41e-0c1b-4f29-8971-1108eff78552")
                        .with(SecurityMockMvcRequestPostProcessors.user(new MemberDetails(member, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roundNumber").value(1))
                .andExpect(jsonPath("$.data.issuedGuestToken").doesNotExist())
                .andDo(document("wordPracticeMemberCycleStart"));

        mvc.perform(post("/api/v2/problem/word-practice/subjects/{subjectId}/cycle", subject.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user(new MemberDetails(member, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roundNumber").value(1))
                .andDo(document("wordPracticeMemberCycleResume"));
    }

    @Test
    void concurrentFirstStartCreatesOnlyOneRoundOneCycle() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (int index = 0; index < 2; index++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    wordPracticeService.startOrResumeCycle(member.getId(), null, subject.getId());
                    return null;
                }));
            }
            org.assertj.core.api.Assertions.assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdown();
            org.assertj.core.api.Assertions.assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }

        org.assertj.core.api.Assertions.assertThat(cycleJpaRepository.count()).isOne();
        org.assertj.core.api.Assertions.assertThat(cycleJpaRepository.findFirstByParticipantIdAndSubjectIdOrderByRoundNumberDesc(
                participantRepository.findByTypeAndOwnerKey(com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipantType.MEMBER,
                        String.valueOf(member.getId())).orElseThrow().getId(), subject.getId()).orElseThrow().getRoundNumber()).isEqualTo(1);
    }

    @Test
    void nextProblemBatchSupportsOnlyFixedSizesAndKeepsCycleOrder() throws Exception {
        addWordProblems(15);
        WordPracticeCycleResponse cycle = wordPracticeService.startOrResumeCycle(null, null, subject.getId());

        mvc.perform(get("/api/v2/problem/word-practice/cycles/{cycleId}/problems", cycle.cycleId())
                        .header("X-Guest-Token", cycle.issuedGuestToken()).param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestedCount").value(5))
                .andExpect(jsonPath("$.data.returnedCount").value(5))
                .andExpect(jsonPath("$.data.hasMore").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.problems[0].choices[0].isAnswer").value(true))
                .andDo(document("getWordPracticeProblemBatch",
                        requestHeaders(
                                headerWithName("X-Guest-Token")
                                        .description("비회원 회차 소유권을 확인하는 UUID token")
                                        .optional()
                        ),
                        pathParameters(
                                parameterWithName("cycleId").description("조회할 말문제 회차 ID")
                        ),
                        queryParameters(
                                parameterWithName("count").description("가져올 문제 수. 5, 10, 15만 허용")
                        ),
                        responseFields(
                        fieldWithPath("data.cycleId").description("조회한 말문제 회차 식별자").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.requestedCount").description("요청한 문제 수(5, 10, 15)").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.returnedCount").description("실제로 반환한 문제 수").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.hasMore").description("현재 회차에 더 풀 문제가 있는지 여부").type(JsonFieldType.BOOLEAN),
                        fieldWithPath("data.status").description("회차 상태").type(JsonFieldType.STRING),
                        fieldWithPath("data.progress").description("회차 진행률").type(JsonFieldType.OBJECT),
                        fieldWithPath("data.progress.solvedCount").description("답안을 최초 제출한 문제 수").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.progress.correctCount").description("정답 수").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.progress.incorrectCount").description("오답 수").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.progress.totalCount").description("현재 출제 가능한 회차 문제 수").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.progress.remainingCount").description("남은 문제 수").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.problems").description("기존 ProblemV2Response 형태의 고정 순서 문제 목록").type(JsonFieldType.ARRAY),
                        fieldWithPath("data.problems[].id").description("문제 식별자").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.problems[].number").description("시험 내 문제 번호").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.problems[].requiresCalculation").description("계산 문제 여부").type(JsonFieldType.BOOLEAN),
                        subsectionWithPath("data.problems[].content").description("Block 형식 문제 본문"),
                        subsectionWithPath("data.problems[].explanation").description("Block 형식 해설"),
                        fieldWithPath("data.problems[].lecture").description("해설 강의 정보").optional().type(JsonFieldType.OBJECT),
                        fieldWithPath("data.problems[].choices").description("문제 보기 목록").type(JsonFieldType.ARRAY),
                        fieldWithPath("data.problems[].choices[].id").description("보기 식별자").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.problems[].choices[].number").description("보기 번호").type(JsonFieldType.NUMBER),
                        fieldWithPath("data.problems[].choices[].content").description("보기 내용").type(JsonFieldType.STRING),
                        fieldWithPath("data.problems[].choices[].isAnswer").description("정답 여부").type(JsonFieldType.BOOLEAN),
                        fieldWithPath("data.problems[].choices[].explanation").description("보기 해설 Block").type(JsonFieldType.ARRAY)
                        )));

        mvc.perform(get("/api/v2/problem/word-practice/cycles/{cycleId}/problems", cycle.cycleId())
                        .header("X-Guest-Token", cycle.issuedGuestToken()).param("count", "10"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.returnedCount").value(10))
                .andDo(document("getWordPracticeProblemBatch10"));
        mvc.perform(get("/api/v2/problem/word-practice/cycles/{cycleId}/problems", cycle.cycleId())
                        .header("X-Guest-Token", cycle.issuedGuestToken()).param("count", "15"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.returnedCount").value(15))
                .andDo(document("getWordPracticeProblemBatch15"));
        mvc.perform(get("/api/v2/problem/word-practice/cycles/{cycleId}/problems", cycle.cycleId())
                        .header("X-Guest-Token", cycle.issuedGuestToken()).param("count", "6"))
                .andExpect(status().isBadRequest())
                // count 허용값 위반 시 프론트가 5·10·15 중 하나로 고쳐 재요청해야 함을 문서화한다.
                .andDo(document("wordPracticeInvalidBatchCount"));
        mvc.perform(get("/api/v2/problem/word-practice/cycles/{cycleId}/problems", cycle.cycleId())
                        .header("X-Guest-Token", cycle.issuedGuestToken()).param("count", "0"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v2/problem/word-practice/cycles/{cycleId}/problems", cycle.cycleId())
                        .header("X-Guest-Token", cycle.issuedGuestToken()).param("count", "30"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nextProblemBatchRejectsOtherOwnerAndReturnsNotFoundForUnknownCycle() throws Exception {
        WordPracticeCycleResponse cycle = wordPracticeService.startOrResumeCycle(null, null, subject.getId());

        mvc.perform(get("/api/v2/problem/word-practice/cycles/{cycleId}/problems", cycle.cycleId()).param("count", "5"))
                .andExpect(status().isForbidden())
                .andDo(document("wordPracticeOwnershipFailure"));
        mvc.perform(get("/api/v2/problem/word-practice/cycles/{cycleId}/problems", 999999L)
                        .header("X-Guest-Token", cycle.issuedGuestToken()).param("count", "5"))
                .andExpect(status().isNotFound());
    }

    /** 최초 답안, 동일 요청 재시도, 다른 보기 변경 차단과 마지막 문제 자동 완료를 HTTP 계약으로 확인한다. */
    @Test
    void firstAnswerIsImmutableIdempotentAndCompletesLastProblem() throws Exception {
        WordPracticeCycleResponse cycleResponse = wordPracticeService.startOrResumeCycle(null, null, subject.getId());
        WordPracticeCycle cycle = cycleRepository.findById(cycleResponse.cycleId()).orElseThrow();
        Long problemId = cycle.getProblemOrder().getFirst();
        Choice correctChoice = choiceRepository.findAllByProblemId(problemId).getFirst();
        Choice differentChoice = choiceRepository.save(Choice.builder().problem(correctChoice.getProblem()).number(2)
                .content("다른 보기").isAnswer(false).explanationJson(List.of()).build());
        String answerJson = "{\"problemId\":" + problemId + ",\"choiceId\":" + correctChoice.getId() + "}";

        mvc.perform(post("/api/v2/problem/word-practice/cycles/{cycleId}/answers", cycleResponse.cycleId())
                        .header("X-Guest-Token", cycleResponse.issuedGuestToken())
                        .contentType(MediaType.APPLICATION_JSON).content(answerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.problemId").value(problemId))
                .andExpect(jsonPath("$.data.choiceId").value(correctChoice.getId()))
                .andExpect(jsonPath("$.data.isCorrect").value(true))
                .andExpect(jsonPath("$.data.sequence").value(1))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.progress.solvedCount").value(1))
                .andExpect(jsonPath("$.data.progress.correctCount").value(1))
                .andDo(document("postWordPracticeAnswer",
                        requestHeaders(
                                headerWithName("X-Guest-Token")
                                        .description("비회원 회차 소유권을 확인하는 UUID token")
                                        .optional()
                        ),
                        pathParameters(
                                parameterWithName("cycleId").description("답안을 제출할 말문제 회차 ID")
                        ),
                        requestFields(
                                fieldWithPath("problemId").type(JsonFieldType.NUMBER).description("현재 고정 순서에서 답할 문제 ID"),
                                fieldWithPath("choiceId").type(JsonFieldType.NUMBER).description("해당 문제에서 선택한 보기 ID")
                        ),
                        responseFields(
                                fieldWithPath("data.problemId").type(JsonFieldType.NUMBER).description("답안을 저장한 문제 ID"),
                                fieldWithPath("data.choiceId").type(JsonFieldType.NUMBER).description("저장된 보기 ID"),
                                fieldWithPath("data.isCorrect").type(JsonFieldType.BOOLEAN).description("서버가 Choice 정답값으로 계산한 결과"),
                                fieldWithPath("data.sequence").type(JsonFieldType.NUMBER).description("1부터 시작하는 회차 내 최초 답안 제출 순서"),
                                fieldWithPath("data.status").type(JsonFieldType.STRING).description("답안 반영 뒤 회차 상태"),
                                fieldWithPath("data.progress").type(JsonFieldType.OBJECT).description("답안 반영 뒤 회차 진행률"),
                                fieldWithPath("data.progress.solvedCount").type(JsonFieldType.NUMBER).description("최초 답안 제출 수"),
                                fieldWithPath("data.progress.correctCount").type(JsonFieldType.NUMBER).description("정답 수"),
                                fieldWithPath("data.progress.incorrectCount").type(JsonFieldType.NUMBER).description("오답 수"),
                                fieldWithPath("data.progress.totalCount").type(JsonFieldType.NUMBER).description("회차 문제 수"),
                                fieldWithPath("data.progress.remainingCount").type(JsonFieldType.NUMBER).description("남은 문제 수")
                        )
                ));

        mvc.perform(post("/api/v2/problem/word-practice/cycles/{cycleId}/answers", cycleResponse.cycleId())
                        .header("X-Guest-Token", cycleResponse.issuedGuestToken())
                        .contentType(MediaType.APPLICATION_JSON).content(answerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.progress.solvedCount").value(1))
                .andDo(document("wordPracticeAnswerIdempotentRetry"));
        org.assertj.core.api.Assertions.assertThat(answerJpaRepository.count()).isOne();

        mvc.perform(post("/api/v2/problem/word-practice/cycles/{cycleId}/answers", cycleResponse.cycleId())
                        .header("X-Guest-Token", cycleResponse.issuedGuestToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"problemId\":" + problemId + ",\"choiceId\":" + differentChoice.getId() + "}"))
                .andExpect(status().isConflict())
                .andDo(document("wordPracticeAnswerChangeConflict"));
    }

    /** 동일 답안이 동시에 도착해도 최초 이력과 회차 진행률은 한 번만 반영돼야 한다. */
    @Test
    void concurrentSameAnswerCreatesOneHistoryAndAdvancesOnce() throws Exception {
        WordPracticeCycleResponse cycleResponse = wordPracticeService.startOrResumeCycle(null, null, subject.getId());
        WordPracticeCycle cycle = cycleRepository.findById(cycleResponse.cycleId()).orElseThrow();
        Long problemId = cycle.getProblemOrder().getFirst();
        Choice choice = choiceRepository.findAllByProblemId(problemId).getFirst();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (int index = 0; index < 2; index++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    wordPracticeService.submitAnswer(null, cycleResponse.issuedGuestToken(),
                            cycleResponse.cycleId(), problemId, choice.getId());
                    return null;
                }));
            }
            org.assertj.core.api.Assertions.assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdown();
            org.assertj.core.api.Assertions.assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }

        WordPracticeCycle persistedCycle = cycleRepository.findById(cycleResponse.cycleId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(answerJpaRepository.count()).isOne();
        org.assertj.core.api.Assertions.assertThat(answerJpaRepository.findAll().getFirst().getSequence()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(persistedCycle.getSolvedCount()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(persistedCycle.getNextIndex()).isEqualTo(1);
    }

    /** 오답도 서버가 choice 정답 여부로 계산해 즉시 피드백 응답에 반영하는지 확인한다. */
    @Test
    void incorrectAnswerReturnsServerCalculatedFeedback() throws Exception {
        WordPracticeCycleResponse cycleResponse = wordPracticeService.startOrResumeCycle(null, null, subject.getId());
        WordPracticeCycle cycle = cycleRepository.findById(cycleResponse.cycleId()).orElseThrow();
        Long problemId = cycle.getProblemOrder().getFirst();
        Problem problem = problemRepository.findById(problemId).orElseThrow();
        Choice incorrectChoice = choiceRepository.save(Choice.builder().problem(problem).number(2)
                .content("오답 보기").isAnswer(false).explanationJson(List.of()).build());

        mvc.perform(post("/api/v2/problem/word-practice/cycles/{cycleId}/answers", cycleResponse.cycleId())
                        .header("X-Guest-Token", cycleResponse.issuedGuestToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"problemId\":" + problemId + ",\"choiceId\":" + incorrectChoice.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isCorrect").value(false))
                .andExpect(jsonPath("$.data.progress.incorrectCount").value(1))
                .andDo(document("wordPracticeIncorrectAnswer"));
    }

    /** 완료된 최신 회차만 명시적으로 다음 round를 만들며, 이전 답안은 그대로 남는지 확인한다. */
    @Test
    void restartCreatesRoundTwoWithNewProgressAndPreservesOldAnswer() throws Exception {
        WordPracticeCycleResponse firstCycle = wordPracticeService.startOrResumeCycle(null, null, subject.getId());
        WordPracticeCycle persistedFirstCycle = cycleRepository.findById(firstCycle.cycleId()).orElseThrow();
        Long problemId = persistedFirstCycle.getProblemOrder().getFirst();
        Choice choice = choiceRepository.findAllByProblemId(problemId).getFirst();
        wordPracticeService.submitAnswer(null, firstCycle.issuedGuestToken(), firstCycle.cycleId(), problemId, choice.getId());
        String firstSeed = cycleRepository.findById(firstCycle.cycleId()).orElseThrow().getShuffleSeed();
        addWordProblems(1); // round 2는 시작 시점의 현재 공개 카탈로그를 새로 스냅샷한다.

        mvc.perform(post("/api/v2/problem/word-practice/cycles/{cycleId}/restart", firstCycle.cycleId())
                        .header("X-Guest-Token", firstCycle.issuedGuestToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roundNumber").value(2))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.progress.solvedCount").value(0))
                .andExpect(jsonPath("$.data.progress.totalCount").value(2))
                .andDo(document("postWordPracticeRestart",
                        requestHeaders(
                                headerWithName("X-Guest-Token")
                                        .description("비회원 회차 소유권을 확인하는 UUID token")
                                        .optional()
                        ),
                        pathParameters(
                                parameterWithName("cycleId").description("재시작할 완료 상태의 최신 회차 ID")
                        ),
                        responseFields(
                        fieldWithPath("data.cycleId").type(JsonFieldType.NUMBER).description("새로 생성된 다음 회차 ID"),
                        fieldWithPath("data.subjectId").type(JsonFieldType.NUMBER).description("회차의 subject ID"),
                        fieldWithPath("data.roundNumber").type(JsonFieldType.NUMBER).description("이전 완료 회차보다 1 큰 회차 번호"),
                        fieldWithPath("data.status").type(JsonFieldType.STRING).description("새 회차의 상태(IN_PROGRESS)"),
                        fieldWithPath("data.progress").type(JsonFieldType.OBJECT).description("새 회차의 초기 진행률"),
                        fieldWithPath("data.progress.solvedCount").type(JsonFieldType.NUMBER).description("최초 답안 제출 수"),
                        fieldWithPath("data.progress.correctCount").type(JsonFieldType.NUMBER).description("정답 수"),
                        fieldWithPath("data.progress.incorrectCount").type(JsonFieldType.NUMBER).description("오답 수"),
                        fieldWithPath("data.progress.totalCount").type(JsonFieldType.NUMBER).description("새 카탈로그 스냅샷 문제 수"),
                        fieldWithPath("data.progress.remainingCount").type(JsonFieldType.NUMBER).description("남은 문제 수")
                        )));

        WordPracticeCycle secondCycle = cycleJpaRepository.findFirstByParticipantIdAndSubjectIdOrderByRoundNumberDesc(
                persistedFirstCycle.getParticipant().getId(), subject.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(secondCycle.getShuffleSeed()).isNotEqualTo(firstSeed);
        org.assertj.core.api.Assertions.assertThat(answerJpaRepository.count()).isOne();

        mvc.perform(get("/api/v2/problem/word-practice/subjects").header("X-Guest-Token", firstCycle.issuedGuestToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].solvedCount").value(0))
                .andExpect(jsonPath("$.data[0].totalCount").value(2))
                .andExpect(jsonPath("$.data[0].status").value("IN_PROGRESS"));

        mvc.perform(post("/api/v2/problem/word-practice/cycles/{cycleId}/restart", secondCycle.getId())
                        .header("X-Guest-Token", firstCycle.issuedGuestToken()))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/v2/problem/word-practice/cycles/{cycleId}/restart", firstCycle.cycleId())
                        .header("X-Guest-Token", firstCycle.issuedGuestToken()))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/v2/problem/word-practice/cycles/{cycleId}/restart", firstCycle.cycleId()))
                .andExpect(status().isForbidden());
    }

    /** participant 비관 잠금으로 두 개의 restart 요청이 round 2를 하나만 생성하는지 검증한다. */
    @Test
    void concurrentRestartCreatesOnlyOneNextRound() throws Exception {
        WordPracticeCycleResponse firstCycle = wordPracticeService.startOrResumeCycle(null, null, subject.getId());
        WordPracticeCycle cycle = cycleRepository.findById(firstCycle.cycleId()).orElseThrow();
        Long problemId = cycle.getProblemOrder().getFirst();
        Choice choice = choiceRepository.findAllByProblemId(problemId).getFirst();
        wordPracticeService.submitAnswer(null, firstCycle.issuedGuestToken(), firstCycle.cycleId(), problemId, choice.getId());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();
        try {
            for (int index = 0; index < 2; index++) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        wordPracticeService.restartCycle(null, firstCycle.issuedGuestToken(), firstCycle.cycleId());
                        return true;
                    } catch (com.cpa.yusin.quiz.global.exception.WordPracticeException ignored) {
                        return false;
                    }
                }));
            }
            org.assertj.core.api.Assertions.assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            long successfulRestartCount = 0;
            for (Future<Boolean> future : futures) {
                if (future.get(10, TimeUnit.SECONDS)) {
                    successfulRestartCount++;
                }
            }
            org.assertj.core.api.Assertions.assertThat(successfulRestartCount).isOne();
        } finally {
            executor.shutdown();
            org.assertj.core.api.Assertions.assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
        org.assertj.core.api.Assertions.assertThat(cycleJpaRepository.count()).isEqualTo(2);
    }

    @Test
    void unavailableProblemIsRemovedFromUnsolvedOrderAndNextProblemFillsItsPlace() throws Exception {
        addWordProblems(5);
        WordPracticeCycleResponse cycleResponse = wordPracticeService.startOrResumeCycle(null, null, subject.getId());
        WordPracticeCycle cycle = cycleRepository.findById(cycleResponse.cycleId()).orElseThrow();
        Long unavailableProblemId = cycle.getProblemOrder().getFirst();
        List<Integer> expectedProblemIds = cycle.getProblemOrder().stream()
                .filter(problemId -> !problemId.equals(unavailableProblemId))
                .limit(5)
                .map(Long::intValue)
                .toList();
        Problem unavailable = problemRepository.findById(unavailableProblemId).orElseThrow();
        unavailable.delete();
        problemRepository.save(unavailable);

        mvc.perform(get("/api/v2/problem/word-practice/cycles/{cycleId}/problems", cycleResponse.cycleId())
                        .header("X-Guest-Token", cycleResponse.issuedGuestToken()).param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.returnedCount").value(5))
                .andExpect(jsonPath("$.data.progress.totalCount").value(5))
                .andExpect(jsonPath("$.data.problems[*].id")
                        .value(org.hamcrest.Matchers.contains(expectedProblemIds.toArray())));
    }

    @Test
    void batchLargerThanRemainderReturnsEveryRemainingProblemOnce() throws Exception {
        addWordProblems(6);
        WordPracticeCycleResponse cycleResponse = wordPracticeService.startOrResumeCycle(null, null, subject.getId());
        WordPracticeCycle cycle = cycleRepository.findById(cycleResponse.cycleId()).orElseThrow();
        List<Integer> expectedProblemIds = cycle.getProblemOrder().stream().map(Long::intValue).toList();

        mvc.perform(get("/api/v2/problem/word-practice/cycles/{cycleId}/problems", cycleResponse.cycleId())
                        .header("X-Guest-Token", cycleResponse.issuedGuestToken()).param("count", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.returnedCount").value(7))
                .andExpect(jsonPath("$.data.hasMore").value(false))
                .andExpect(jsonPath("$.data.problems[*].id")
                        .value(org.hamcrest.Matchers.contains(expectedProblemIds.toArray())));
    }

    /** 회차 생성 이전에 추가된 공개 말문제와 choice를 만들어 batch 조회의 N+1 방지 경로를 준비한다. */
    private void addWordProblems(int count) {
        for (int number = 2; number <= count + 1; number++) {
            Problem problem = problemRepository.save(Problem.builder().number(number).exam(wordExam).requiresCalculation(false)
                    .contentJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                    .explanationJson(List.of(TextBlock.builder().type("text").tag("p").build())).build());
            choiceRepository.save(Choice.builder().problem(problem).number(1).content("보기 " + number)
                    .isAnswer(true).explanationJson(List.of()).build());
        }
    }
}
