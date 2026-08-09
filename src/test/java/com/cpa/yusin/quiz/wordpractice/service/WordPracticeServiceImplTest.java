package com.cpa.yusin.quiz.wordpractice.service;

import com.cpa.yusin.quiz.problem.service.dto.WordProblemCountProjection;
import com.cpa.yusin.quiz.problem.service.dto.WordProblemCandidateProjection;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.ProblemV2ResponseAssembler;
import com.cpa.yusin.quiz.problem.controller.dto.response.ProblemV2Response;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.common.service.UuidHolder;
import com.cpa.yusin.quiz.study.event.StudySolvedEvent;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.domain.SubjectStatus;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeProgressStatus;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeSubjectResponse;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeCycle;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeCycleStatus;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipant;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipantType;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeCycleResponse;
import com.cpa.yusin.quiz.wordpractice.controller.dto.request.WordPracticeAnswerRequest;
import com.cpa.yusin.quiz.wordpractice.service.port.WordPracticeCycleRepository;
import com.cpa.yusin.quiz.wordpractice.service.port.WordPracticeParticipantRepository;
import com.cpa.yusin.quiz.wordpractice.service.port.WordPracticeAnswerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordPracticeServiceImplTest {

    private final SubjectRepository subjectRepository = mock(SubjectRepository.class);
    private final ProblemRepository problemRepository = mock(ProblemRepository.class);
    private final WordPracticeParticipantResolver participantResolver = mock(WordPracticeParticipantResolver.class);
    private final WordPracticeCycleRepository cycleRepository = mock(WordPracticeCycleRepository.class);
    private final WordPracticeParticipantRepository participantRepository = mock(WordPracticeParticipantRepository.class);
    private final WordPracticeOrderStrategy orderStrategy = mock(WordPracticeOrderStrategy.class);
    private final ClockHolder clockHolder = mock(ClockHolder.class);
    private final UuidHolder uuidHolder = mock(UuidHolder.class);
    private final ChoiceService choiceService = mock(ChoiceService.class);
    private final ProblemV2ResponseAssembler problemV2ResponseAssembler = mock(ProblemV2ResponseAssembler.class);
    private final WordPracticeAnswerRepository answerRepository = mock(WordPracticeAnswerRepository.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final WordPracticeServiceImpl service = new WordPracticeServiceImpl(
            subjectRepository, problemRepository, participantResolver, cycleRepository,
            participantRepository, orderStrategy, clockHolder, uuidHolder, choiceService,
            problemV2ResponseAssembler, answerRepository, eventPublisher);

    private final Subject subject = Subject.builder().id(1L).name("감정평가사").status(SubjectStatus.PUBLISHED).build();

    @BeforeEach
    void setUp() {
        when(subjectRepository.findAllPublished()).thenReturn(List.of(subject));
        when(problemRepository.countPublishedWordProblemsBySubject())
                .thenReturn(List.of(new WordProblemCountProjection(1L, 12)));
    }

    @Test
    void identityAbsentReturnsCatalogProgressWithoutCreatingParticipant() {
        when(participantResolver.resolve(null, null)).thenReturn(Optional.empty());

        WordPracticeSubjectResponse response = service.getSubjects(null, null).getFirst();

        assertThat(response).isEqualTo(new WordPracticeSubjectResponse(
                1L, "감정평가사", 0, 12, 12, WordPracticeProgressStatus.NOT_STARTED));
        verify(cycleRepository, org.mockito.Mockito.never()).findLatestByParticipantId(any());
    }

    @Test
    void memberProgressUsesLatestCycleAndIgnoresGuestIdentityInResolverInput() {
        WordPracticeParticipant member = participant(10L, WordPracticeParticipantType.MEMBER);
        when(participantResolver.resolve(100L, "guest-token")).thenReturn(Optional.of(member));
        when(cycleRepository.findLatestByParticipantId(10L)).thenReturn(List.of(cycle(member, 4, 2, WordPracticeCycleStatus.IN_PROGRESS)));

        WordPracticeSubjectResponse response = service.getSubjects(100L, "guest-token").getFirst();

        assertThat(response.solvedCount()).isEqualTo(2);
        assertThat(response.totalCount()).isEqualTo(4);
        assertThat(response.remainingCount()).isEqualTo(2);
        assertThat(response.status()).isEqualTo(WordPracticeProgressStatus.IN_PROGRESS);
        verify(participantResolver).resolve(100L, "guest-token");
    }

    @Test
    void guestProgressAndCompletedStatusAreReturnedSeparately() {
        WordPracticeParticipant guest = participant(20L, WordPracticeParticipantType.GUEST);
        when(participantResolver.resolve(null, "guest-token")).thenReturn(Optional.of(guest));
        when(cycleRepository.findLatestByParticipantId(20L)).thenReturn(List.of(cycle(guest, 2, 2, WordPracticeCycleStatus.COMPLETED)));

        WordPracticeSubjectResponse response = service.getSubjects(null, "guest-token").getFirst();

        assertThat(response.status()).isEqualTo(WordPracticeProgressStatus.COMPLETED);
        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.solvedCount()).isEqualTo(2);
    }

    @Test
    void zeroProblemSubjectRemainsVisible() {
        when(problemRepository.countPublishedWordProblemsBySubject()).thenReturn(List.of());
        when(participantResolver.resolve(null, null)).thenReturn(Optional.empty());

        WordPracticeSubjectResponse response = service.getSubjects(null, null).getFirst();

        assertThat(response).isEqualTo(new WordPracticeSubjectResponse(
                1L, "감정평가사", 0, 0, 0, WordPracticeProgressStatus.NOT_STARTED));
    }

    @Test
    void firstGuestStartIssuesTokenAndSavesFixedOrder() {
        WordPracticeParticipant issuedParticipant = participant(30L, WordPracticeParticipantType.GUEST);
        LocalDateTime now = LocalDateTime.of(2026, 8, 9, 12, 0);
        when(participantResolver.createOrResolve(null, null))
                .thenReturn(WordPracticeParticipantResolution.issued(issuedParticipant, "guest-token"));
        when(participantRepository.findByIdWithLock(30L)).thenReturn(Optional.of(issuedParticipant));
        when(subjectRepository.findPublishedById(1L)).thenReturn(Optional.of(subject));
        when(cycleRepository.findLatestByParticipantIdAndSubjectIdWithLock(30L, 1L)).thenReturn(Optional.empty());
        List<WordProblemCandidateProjection> candidates = List.of(
                new WordProblemCandidateProjection(11L, 101L, 2024),
                new WordProblemCandidateProjection(12L, 102L, 2025));
        when(problemRepository.findPublishedWordProblemCandidatesBySubjectId(1L)).thenReturn(candidates);
        when(uuidHolder.getRandom()).thenReturn("cycle-seed");
        when(orderStrategy.createOrder(candidates, "cycle-seed")).thenReturn(List.of(12L, 11L));
        when(clockHolder.getCurrentDateTime()).thenReturn(now);
        when(cycleRepository.save(any())).thenAnswer(invocation -> {
            WordPracticeCycle unsaved = invocation.getArgument(0);
            return WordPracticeCycle.builder()
                    .id(77L).participant(unsaved.getParticipant()).subjectId(unsaved.getSubjectId())
                    .roundNumber(unsaved.getRoundNumber()).status(unsaved.getStatus()).shuffleSeed(unsaved.getShuffleSeed())
                    .problemOrder(unsaved.getProblemOrder()).plannedProblemCount(unsaved.getPlannedProblemCount())
                    .nextIndex(0).solvedCount(0).correctCount(0).skippedCount(0).startedAt(now).build();
        });

        WordPracticeCycleResponse response = service.startOrResumeCycle(null, null, 1L);

        assertThat(response.cycleId()).isEqualTo(77L);
        assertThat(response.issuedGuestToken()).isEqualTo("guest-token");
        assertThat(response.progress().totalCount()).isEqualTo(2);
        assertThat(response.status()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void existingCompletedCycleIsReturnedWithoutAutomaticRestart() {
        WordPracticeParticipant member = participant(40L, WordPracticeParticipantType.MEMBER);
        WordPracticeCycle completed = cycle(member, 2, 2, WordPracticeCycleStatus.COMPLETED);
        when(participantResolver.createOrResolve(400L, null))
                .thenReturn(WordPracticeParticipantResolution.existing(member));
        when(participantRepository.findByIdWithLock(40L)).thenReturn(Optional.of(member));
        when(subjectRepository.findPublishedById(1L)).thenReturn(Optional.of(subject));
        when(cycleRepository.findLatestByParticipantIdAndSubjectIdWithLock(40L, 1L)).thenReturn(Optional.of(completed));

        WordPracticeCycleResponse response = service.startOrResumeCycle(400L, null, 1L);

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.roundNumber()).isEqualTo(1);
        assertThat(response.issuedGuestToken()).isNull();
        verify(problemRepository, never()).findPublishedWordProblemCandidatesBySubjectId(1L);
    }

    /**
     * 15개 batch는 문제별 choice 조회를 반복하지 않고 한 번의 ID 묶음 조회를 사용한다.
     * 이 서비스 경계 검증은 실제 JPA repository의 IN 조회와 결합되어 choice N+1 회귀를 막는다.
     */
    @Test
    void fifteenProblemBatchLoadsChoicesOnceByProblemIds() {
        WordPracticeParticipant guest = participant(50L, WordPracticeParticipantType.GUEST);
        WordPracticeCycle cycle = cycle(guest, 15, 0, WordPracticeCycleStatus.IN_PROGRESS);
        List<Problem> problems = java.util.stream.LongStream.rangeClosed(1, 15)
                .mapToObj(problemId -> Problem.builder().id(problemId).number((int) problemId).build())
                .toList();
        when(participantResolver.resolveForWrite(null, "guest-token")).thenReturn(Optional.of(guest));
        when(cycleRepository.findByIdWithLock(77L)).thenReturn(Optional.of(cycle));
        when(problemRepository.findPublishedWordProblemsByIds(any())).thenReturn(problems);
        when(choiceService.findAllByProblemIds(any())).thenReturn(Map.of());
        when(problemV2ResponseAssembler.assemble(any(), any())).thenAnswer(invocation -> {
            Problem problem = invocation.getArgument(0);
            return ProblemV2Response.builder().id(problem.getId()).build();
        });

        var response = service.getNextProblems(null, "guest-token", 77L, 15);

        assertThat(response.returnedCount()).isEqualTo(15);
        verify(problemRepository, times(1)).findPublishedWordProblemsByIds(any());
        verify(choiceService, times(1)).findAllByProblemIds(problems.stream().map(Problem::getId).toList());
    }

    /** 다섯 문제 학습 단위를 끝내기 전에는 단건 호환 API도 학습 기록을 만들지 않는다. */
    @Test
    void firstMemberAnswerDoesNotPublishSolvedEventBeforeLearningUnitCompletes() {
        WordPracticeParticipant member = participant(60L, WordPracticeParticipantType.MEMBER);
        WordPracticeCycle activeCycle = cycle(member, 2, 0, WordPracticeCycleStatus.IN_PROGRESS);
        Problem problem = Problem.builder().id(1L).number(1).build();
        Choice choice = Choice.builder().id(11L).number(1).content("정답")
                .isAnswer(true).problem(problem).build();
        LocalDateTime now = LocalDateTime.of(2026, 8, 9, 13, 0);

        when(participantResolver.resolveForWrite(600L, null)).thenReturn(Optional.of(member));
        when(cycleRepository.findByIdWithLock(77L)).thenReturn(Optional.of(activeCycle));
        when(answerRepository.findByCycleIdAndProblemId(77L, 1L)).thenReturn(Optional.empty());
        when(problemRepository.findPublishedWordProblemsByIds(List.of(1L))).thenReturn(List.of(problem));
        when(choiceService.findById(11L)).thenReturn(choice);
        when(clockHolder.getCurrentDateTime()).thenReturn(now);
        when(answerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.submitAnswer(600L, null, 77L, 1L, 11L);

        verify(eventPublisher, never()).publishEvent(any());
    }

    /** 회원의 다섯 답안 배치는 한 이벤트로 실제 풀이 수를 기록한다. */
    @Test
    void memberAnswerBatchPublishesOneCompletedLearningUnitEvent() {
        WordPracticeParticipant member = participant(61L, WordPracticeParticipantType.MEMBER);
        WordPracticeCycle activeCycle = cycle(member, 5, 0, WordPracticeCycleStatus.IN_PROGRESS);
        LocalDateTime now = LocalDateTime.of(2026, 8, 9, 13, 5);
        List<Problem> problems = java.util.stream.LongStream.rangeClosed(1, 5)
                .mapToObj(id -> Problem.builder().id(id).number((int) id).build())
                .toList();
        List<Choice> choices = problems.stream()
                .map(problem -> Choice.builder().id(problem.getId() * 10).number(1).content("정답")
                        .isAnswer(true).problem(problem).build())
                .toList();
        List<WordPracticeAnswerRequest> requests = choices.stream()
                .map(choice -> new WordPracticeAnswerRequest(choice.getProblem().getId(), choice.getId()))
                .toList();

        when(participantResolver.resolveForWrite(610L, null)).thenReturn(Optional.of(member));
        when(cycleRepository.findByIdWithLock(77L)).thenReturn(Optional.of(activeCycle));
        when(answerRepository.findAllByCycleIdAndProblemIds(77L, List.of(1L, 2L, 3L, 4L, 5L)))
                .thenReturn(List.of());
        when(problemRepository.findPublishedWordProblemsByIds(List.of(1L, 2L, 3L, 4L, 5L)))
                .thenReturn(problems);
        for (Choice choice : choices) {
            when(choiceService.findById(choice.getId())).thenReturn(choice);
        }
        when(clockHolder.getCurrentDateTime()).thenReturn(now);
        when(answerRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.submitAnswerBatch(610L, null, 77L, requests);

        assertThat(response.answers()).hasSize(5);
        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.progress().solvedCount()).isEqualTo(5);
        verify(answerRepository).saveAll(any());
        verify(eventPublisher).publishEvent(new StudySolvedEvent(610L, 5));
    }

    /** 마지막 문제 묶음만 다섯 개보다 작을 수 있고, 중간의 부분 묶음은 저장 전에 거부한다. */
    @Test
    void nonFinalPartialBatchIsRejectedBeforePersistence() {
        WordPracticeParticipant member = participant(62L, WordPracticeParticipantType.MEMBER);
        WordPracticeCycle activeCycle = cycle(member, 7, 0, WordPracticeCycleStatus.IN_PROGRESS);
        when(participantResolver.resolveForWrite(620L, null)).thenReturn(Optional.of(member));
        when(cycleRepository.findByIdWithLock(77L)).thenReturn(Optional.of(activeCycle));
        when(answerRepository.findAllByCycleIdAndProblemIds(77L, List.of(1L, 2L))).thenReturn(List.of());

        assertThatThrownBy(() -> service.submitAnswerBatch(620L, null, 77L, List.of(
                new WordPracticeAnswerRequest(1L, 11L),
                new WordPracticeAnswerRequest(2L, 21L))))
                .isInstanceOf(com.cpa.yusin.quiz.global.exception.WordPracticeException.class);

        verify(answerRepository, never()).saveAll(any());
        assertThat(activeCycle.getSolvedCount()).isZero();
    }

    /** 동일한 완성 payload 재시도는 기존 답안을 반환하고 저장·학습 이벤트를 반복하지 않는다. */
    @Test
    void identicalAnswerBatchRetryIsIdempotent() {
        WordPracticeParticipant member = participant(63L, WordPracticeParticipantType.MEMBER);
        WordPracticeCycle completedCycle = cycle(member, 2, 2, WordPracticeCycleStatus.COMPLETED);
        LocalDateTime submittedAt = LocalDateTime.of(2026, 8, 9, 13, 10);
        List<WordPracticeAnswerRequest> requests = List.of(
                new WordPracticeAnswerRequest(1L, 11L),
                new WordPracticeAnswerRequest(2L, 21L));
        List<com.cpa.yusin.quiz.wordpractice.domain.WordPracticeAnswer> existingAnswers = List.of(
                com.cpa.yusin.quiz.wordpractice.domain.WordPracticeAnswer.create(
                        completedCycle, 1L, 11L, 1, true, submittedAt),
                com.cpa.yusin.quiz.wordpractice.domain.WordPracticeAnswer.create(
                        completedCycle, 2L, 21L, 2, false, submittedAt));

        when(participantResolver.resolveForWrite(630L, null)).thenReturn(Optional.of(member));
        when(cycleRepository.findByIdWithLock(77L)).thenReturn(Optional.of(completedCycle));
        when(answerRepository.findAllByCycleIdAndProblemIds(77L, List.of(1L, 2L)))
                .thenReturn(existingAnswers);

        var response = service.submitAnswerBatch(630L, null, 77L, requests);

        assertThat(response.answers()).extracting(answer -> answer.problemId()).containsExactly(1L, 2L);
        assertThat(response.progress().solvedCount()).isEqualTo(2);
        verify(answerRepository, never()).saveAll(any());
        verify(eventPublisher, never()).publishEvent(any());

        assertThatThrownBy(() -> service.submitAnswerBatch(630L, null, 77L, List.of(
                new WordPracticeAnswerRequest(2L, 21L),
                new WordPracticeAnswerRequest(1L, 11L))))
                .isInstanceOf(com.cpa.yusin.quiz.global.exception.WordPracticeException.class);
    }

    /** 구버전 단건 답안 다음 sequence부터 저장된 다섯 답안도 동일 payload면 멱등 응답한다. */
    @Test
    void identicalBatchRetryMayStartImmediatelyAfterLegacySingleAnswer() {
        WordPracticeParticipant member = participant(64L, WordPracticeParticipantType.MEMBER);
        WordPracticeCycle activeCycle = cycle(member, 7, 6, WordPracticeCycleStatus.IN_PROGRESS);
        LocalDateTime submittedAt = LocalDateTime.of(2026, 8, 9, 13, 15);
        List<WordPracticeAnswerRequest> requests = java.util.stream.LongStream.rangeClosed(2, 6)
                .mapToObj(problemId -> new WordPracticeAnswerRequest(problemId, problemId * 10))
                .toList();
        List<com.cpa.yusin.quiz.wordpractice.domain.WordPracticeAnswer> existingAnswers =
                java.util.stream.IntStream.rangeClosed(2, 6)
                        .mapToObj(sequence -> com.cpa.yusin.quiz.wordpractice.domain.WordPracticeAnswer.create(
                                activeCycle,
                                (long) sequence,
                                (long) sequence * 10,
                                sequence,
                                true,
                                submittedAt
                        ))
                        .toList();

        when(participantResolver.resolveForWrite(640L, null)).thenReturn(Optional.of(member));
        when(cycleRepository.findByIdWithLock(77L)).thenReturn(Optional.of(activeCycle));
        when(answerRepository.findAllByCycleIdAndProblemIds(77L, List.of(2L, 3L, 4L, 5L, 6L)))
                .thenReturn(existingAnswers);

        var response = service.submitAnswerBatch(640L, null, 77L, requests);

        assertThat(response.answers()).extracting(answer -> answer.sequence())
                .containsExactly(2, 3, 4, 5, 6);
        assertThat(response.progress().solvedCount()).isEqualTo(6);
        verify(answerRepository, never()).saveAll(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    /** 이미 저장된 동일 답안 재전송은 멱등 응답만 반환하고 학습 통계를 다시 올리지 않는다. */
    @Test
    void identicalMemberAnswerRetryDoesNotPublishSolvedEvent() {
        WordPracticeParticipant member = participant(70L, WordPracticeParticipantType.MEMBER);
        WordPracticeCycle completedCycle = cycle(member, 1, 1, WordPracticeCycleStatus.COMPLETED);
        LocalDateTime submittedAt = LocalDateTime.of(2026, 8, 9, 13, 10);
        var existingAnswer = com.cpa.yusin.quiz.wordpractice.domain.WordPracticeAnswer.create(
                completedCycle, 1L, 11L, 1, true, submittedAt);

        when(participantResolver.resolveForWrite(700L, null)).thenReturn(Optional.of(member));
        when(cycleRepository.findByIdWithLock(77L)).thenReturn(Optional.of(completedCycle));
        when(answerRepository.findByCycleIdAndProblemId(77L, 1L)).thenReturn(Optional.of(existingAnswer));

        service.submitAnswer(700L, null, 77L, 1L, 11L);

        verify(eventPublisher, never()).publishEvent(any());
    }

    /** 비회원의 새 답안은 회차 진행률에는 반영하지만 회원용 학습 통계 이벤트는 만들지 않는다. */
    @Test
    void firstGuestAnswerDoesNotPublishSolvedEvent() {
        WordPracticeParticipant guest = participant(80L, WordPracticeParticipantType.GUEST);
        WordPracticeCycle activeCycle = cycle(guest, 1, 0, WordPracticeCycleStatus.IN_PROGRESS);
        Problem problem = Problem.builder().id(1L).number(1).build();
        Choice choice = Choice.builder().id(11L).number(1).content("정답")
                .isAnswer(true).problem(problem).build();
        LocalDateTime now = LocalDateTime.of(2026, 8, 9, 13, 20);

        when(participantResolver.resolveForWrite(null, "guest-token")).thenReturn(Optional.of(guest));
        when(cycleRepository.findByIdWithLock(77L)).thenReturn(Optional.of(activeCycle));
        when(answerRepository.findByCycleIdAndProblemId(77L, 1L)).thenReturn(Optional.empty());
        when(problemRepository.findPublishedWordProblemsByIds(List.of(1L))).thenReturn(List.of(problem));
        when(choiceService.findById(11L)).thenReturn(choice);
        when(clockHolder.getCurrentDateTime()).thenReturn(now);
        when(answerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.submitAnswer(null, "guest-token", 77L, 1L, 11L);

        verify(eventPublisher, never()).publishEvent(any());
    }

    private WordPracticeParticipant participant(Long id, WordPracticeParticipantType type) {
        return WordPracticeParticipant.builder().id(id).type(type).ownerKey("owner-" + id).build();
    }

    /** 테스트에서 저장된 최신 회차의 스냅샷 total과 상태를 재현한다. */
    private WordPracticeCycle cycle(WordPracticeParticipant participant, int total, int solved, WordPracticeCycleStatus status) {
        return WordPracticeCycle.builder()
                .participant(participant)
                .subjectId(1L)
                .roundNumber(1)
                .status(status)
                .shuffleSeed("seed")
                .problemOrder(java.util.stream.LongStream.rangeClosed(1, total).boxed().toList())
                .plannedProblemCount(total)
                .nextIndex(solved)
                .solvedCount(solved)
                .correctCount(solved)
                .skippedCount(0)
                .startedAt(LocalDateTime.now())
                .build();
    }
}
