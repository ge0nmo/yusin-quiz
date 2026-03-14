package com.cpa.yusin.quiz.study.integration;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
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
import com.cpa.yusin.quiz.study.domain.DailyStudyLog;
import com.cpa.yusin.quiz.study.domain.ExamMode;
import com.cpa.yusin.quiz.study.domain.StudySession;
import com.cpa.yusin.quiz.study.domain.StudySessionStatus;
import com.cpa.yusin.quiz.study.infrastructure.DailyStudyLogJpaRepository;
import com.cpa.yusin.quiz.study.infrastructure.SubmittedAnswerJpaRepository;
import com.cpa.yusin.quiz.study.infrastructure.StudySessionJpaRepository;
import com.cpa.yusin.quiz.study.service.StudyLogService;
import com.cpa.yusin.quiz.study.service.StudySessionService;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(TeardownExtension.class)
@SpringBootTest
class StudyConcurrencyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 14, 10, 0);

    @Autowired
    private StudySessionService studySessionService;

    @Autowired
    private StudyLogService studyLogService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private ChoiceRepository choiceRepository;

    @Autowired
    private StudySessionJpaRepository studySessionJpaRepository;

    @Autowired
    private SubmittedAnswerJpaRepository submittedAnswerJpaRepository;

    @Autowired
    private DailyStudyLogJpaRepository dailyStudyLogJpaRepository;

    @MockBean
    private ClockHolder clockHolder;

    @BeforeEach
    void setUp() {
        given(clockHolder.getCurrentDateTime()).willReturn(NOW);
        given(clockHolder.getCurrentTime()).willReturn(NOW.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    @Test
    @DisplayName("동시 startSession 요청에도 IN_PROGRESS 세션은 하나만 생성된다")
    void startSession_shouldCreateSingleInProgressSessionPerMemberExamMode() throws Exception {
        Member member = createMember("session@test.com");
        Exam exam = createExam("회계학", "1차", 2025);

        List<StudySession> sessions = runConcurrently(2, () ->
                studySessionService.startSession(member.getId(), exam.getId(), ExamMode.EXAM)
        );

        assertThat(sessions).hasSize(2);
        assertThat(sessions.get(0).getId()).isEqualTo(sessions.get(1).getId());
        assertThat(studySessionJpaRepository.findAll()).hasSize(1);
        assertThat(studySessionJpaRepository.findByMemberIdAndExamIdAndStatusAndMode(
                member.getId(),
                exam.getId(),
                StudySessionStatus.IN_PROGRESS,
                ExamMode.EXAM
        )).isPresent();
    }

    @Test
    @DisplayName("동시 saveAnswer 요청에도 session-problem 당 submitted answer 는 한 행만 유지된다")
    void saveAnswer_shouldKeepSingleSubmittedAnswerRowPerSessionProblem() throws Exception {
        Member member = createMember("answer@test.com");
        Exam exam = createExam("세법", "1차", 2025);
        Problem problem = problemRepository.save(Problem.builder()
                .content("문제")
                .explanation("해설")
                .number(1)
                .exam(exam)
                .build());
        Choice firstChoice = choiceRepository.save(Choice.builder()
                .content("1")
                .number(1)
                .isAnswer(true)
                .problem(problem)
                .build());
        Choice secondChoice = choiceRepository.save(Choice.builder()
                .content("2")
                .number(2)
                .isAnswer(false)
                .problem(problem)
                .build());
        StudySession session = studySessionService.startSession(member.getId(), exam.getId(), ExamMode.EXAM);

        runConcurrently(new ConcurrentCallable<Void>() {
            @Override
            public Void call() {
                studySessionService.saveAnswer(member.getId(), session.getId(), problem.getId(), firstChoice.getId(), 1);
                return null;
            }
        }, new ConcurrentCallable<Void>() {
            @Override
            public Void call() {
                studySessionService.saveAnswer(member.getId(), session.getId(), problem.getId(), secondChoice.getId(), 1);
                return null;
            }
        });

        assertThat(submittedAnswerJpaRepository.findAll()).hasSize(1);
        assertThat(submittedAnswerJpaRepository.findByStudySessionIdAndProblemId(session.getId(), problem.getId()))
                .isPresent()
                .get()
                .satisfies(answer -> assertThat(answer.getChoiceId()).isIn(firstChoice.getId(), secondChoice.getId()));
    }

    @Test
    @DisplayName("동시 recordActivity 요청에도 하루 로그는 한 행으로 누적된다")
    void recordActivity_shouldAccumulateIntoSingleDailyLogRow() throws Exception {
        Member member = createMember("log@test.com");

        runConcurrently(new ConcurrentCallable<Void>() {
            @Override
            public Void call() {
                studyLogService.recordActivity(member.getId(), 2);
                return null;
            }
        }, new ConcurrentCallable<Void>() {
            @Override
            public Void call() {
                studyLogService.recordActivity(member.getId(), 3);
                return null;
            }
        });

        List<DailyStudyLog> logs = dailyStudyLogJpaRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getDate()).isEqualTo(LocalDate.of(2026, 3, 14));
        assertThat(logs.getFirst().getSolvedCount()).isEqualTo(5);
    }

    private Member createMember(String email) {
        return memberRepository.save(Member.builder()
                .email(email)
                .password("encoded-password")
                .username(email)
                .platform(Platform.HOME)
                .role(Role.USER)
                .build());
    }

    private Exam createExam(String subjectName, String examName, int year) {
        Subject subject = subjectRepository.save(Subject.builder()
                .name(subjectName)
                .build());

        return examRepository.save(Exam.builder()
                .name(examName)
                .year(year)
                .subjectId(subject.getId())
                .build());
    }

    private <T> List<T> runConcurrently(int taskCount, ConcurrentCallable<T> task) throws Exception {
        List<ConcurrentCallable<T>> tasks = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) {
            tasks.add(task);
        }
        return runConcurrently(tasks.toArray(new ConcurrentCallable[0]));
    }

    @SafeVarargs
    private final <T> List<T> runConcurrently(ConcurrentCallable<T>... tasks) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(tasks.length);
        CountDownLatch ready = new CountDownLatch(tasks.length);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<T>> futures = new ArrayList<>();

        try {
            for (ConcurrentCallable<T> task : tasks) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("동시 시작 대기 시간이 초과되었습니다.");
                    }
                    return task.call();
                }));
            }

            ready.await(5, TimeUnit.SECONDS);
            start.countDown();

            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get(10, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    @FunctionalInterface
    private interface ConcurrentCallable<T> {
        T call() throws Exception;
    }
}
