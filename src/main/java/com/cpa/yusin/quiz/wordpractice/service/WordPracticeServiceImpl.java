package com.cpa.yusin.quiz.wordpractice.service;

import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.common.service.UuidHolder;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.SubjectException;
import com.cpa.yusin.quiz.global.exception.WordPracticeException;
import com.cpa.yusin.quiz.problem.service.dto.WordProblemCountProjection;
import com.cpa.yusin.quiz.problem.service.dto.WordProblemCandidateProjection;
import com.cpa.yusin.quiz.problem.service.ProblemV2ResponseAssembler;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.choice.controller.dto.response.ChoiceResponse;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import com.cpa.yusin.quiz.study.event.StudySolvedEvent;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeProgressStatus;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeSubjectResponse;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeCycleResponse;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeProgressResponse;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeProblemBatchResponse;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeAnswerResponse;
import com.cpa.yusin.quiz.wordpractice.controller.dto.request.WordPracticeAnswerRequest;
import com.cpa.yusin.quiz.wordpractice.controller.dto.response.WordPracticeAnswerBatchResponse;
import com.cpa.yusin.quiz.wordpractice.controller.port.WordPracticeService;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeCycle;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeCycleStatus;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeParticipant;
import com.cpa.yusin.quiz.wordpractice.domain.WordPracticeAnswer;
import com.cpa.yusin.quiz.wordpractice.service.port.WordPracticeAnswerRepository;
import com.cpa.yusin.quiz.wordpractice.service.port.WordPracticeCycleRepository;
import com.cpa.yusin.quiz.wordpractice.service.port.WordPracticeParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * 공개 subject 카탈로그와 참여자의 고정 순서 회차·불변 답안을 관리한다.
 * subject 목록은 subject마다 추가 쿼리를 실행하지 않아 진입 화면의 조회 비용을 고정한다.
 */
@Service
@Transactional(readOnly = true)
public class WordPracticeServiceImpl implements WordPracticeService {

    private final SubjectRepository subjectRepository;
    private final ProblemRepository problemRepository;
    private final WordPracticeParticipantResolver participantResolver;
    private final WordPracticeCycleRepository cycleRepository;
    private final WordPracticeParticipantRepository participantRepository;
    private final WordPracticeOrderStrategy orderStrategy;
    private final ClockHolder clockHolder;
    private final UuidHolder uuidHolder;
    private final ChoiceService choiceService;
    private final ProblemV2ResponseAssembler problemV2ResponseAssembler;
    private final WordPracticeAnswerRepository answerRepository;
    private final ApplicationEventPublisher eventPublisher;

    /** 애플리케이션 생성 UUID는 파일명 UUID와 분리된 systemUuidHolder를 사용한다. */
    public WordPracticeServiceImpl(
            SubjectRepository subjectRepository,
            ProblemRepository problemRepository,
            WordPracticeParticipantResolver participantResolver,
            WordPracticeCycleRepository cycleRepository,
            WordPracticeParticipantRepository participantRepository,
            WordPracticeOrderStrategy orderStrategy,
            ClockHolder clockHolder,
            @Qualifier("systemUuidHolder") UuidHolder uuidHolder,
            ChoiceService choiceService,
            ProblemV2ResponseAssembler problemV2ResponseAssembler,
            WordPracticeAnswerRepository answerRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.subjectRepository = subjectRepository;
        this.problemRepository = problemRepository;
        this.participantResolver = participantResolver;
        this.cycleRepository = cycleRepository;
        this.participantRepository = participantRepository;
        this.orderStrategy = orderStrategy;
        this.clockHolder = clockHolder;
        this.uuidHolder = uuidHolder;
        this.choiceService = choiceService;
        this.problemV2ResponseAssembler = problemV2ResponseAssembler;
        this.answerRepository = answerRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 인증 회원을 우선해 참여자를 해석하고, 없으면 guest token을 사용한다.
     * 신원이 전혀 없을 때는 participant를 생성하지 않고 모든 subject에 0 진행률만 반환한다.
     */
    @Override
    public List<WordPracticeSubjectResponse> getSubjects(Long memberId, String guestToken) {
        List<Subject> subjects = subjectRepository.findAllPublished();
        Map<Long, Long> catalogCounts = problemRepository.countPublishedWordProblemsBySubject().stream()
                .collect(Collectors.toMap(WordProblemCountProjection::subjectId, WordProblemCountProjection::problemCount));

        Optional<WordPracticeParticipant> participant = participantResolver.resolve(memberId, guestToken);
        Map<Long, WordPracticeCycle> latestCycles = participant
                .map(value -> cycleRepository.findLatestByParticipantId(value.getId()))
                .orElseGet(List::of)
                .stream()
                .collect(Collectors.toMap(WordPracticeCycle::getSubjectId, Function.identity()));

        return subjects.stream()
                .map(subject -> toResponse(subject, catalogCounts.getOrDefault(subject.getId(), 0L), latestCycles.get(subject.getId())))
                .toList();
    }

    /** 최신 회차가 있으면 스냅샷 진행률을, 없으면 현재 공개 카탈로그 수를 응답 값으로 사용한다. */
    private WordPracticeSubjectResponse toResponse(Subject subject, long catalogCount, WordPracticeCycle cycle) {
        if (cycle == null) {
            int totalCount = Math.toIntExact(catalogCount);
            return new WordPracticeSubjectResponse(subject.getId(), subject.getName(), 0, totalCount, totalCount,
                    WordPracticeProgressStatus.NOT_STARTED);
        }
        return new WordPracticeSubjectResponse(
                subject.getId(), subject.getName(), cycle.getSolvedCount(), cycle.getTotalCount(), cycle.getRemainingCount(),
                cycle.getStatus() == WordPracticeCycleStatus.COMPLETED
                        ? WordPracticeProgressStatus.COMPLETED : WordPracticeProgressStatus.IN_PROGRESS
        );
    }

    /**
     * 회차 시작 요청을 멱등하게 처리한다. 참여자 행을 잠근 후 최신 회차를 먼저 확인하므로
     * 동시에 들어온 최초 요청도 한 개의 round 1만 생성한다.
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public WordPracticeCycleResponse startOrResumeCycle(Long memberId, String guestToken, Long subjectId) {
        WordPracticeParticipantResolution resolution = participantResolver.createOrResolve(memberId, guestToken);
        WordPracticeParticipant participant = participantRepository.findByIdWithLock(resolution.participant().getId())
                .orElseThrow(() -> new IllegalStateException("Word practice participant was not persisted"));

        // 삭제되었거나 비공개 subject에는 회차를 새로 만들거나 기존 기록을 노출하지 않는다.
        Subject subject = subjectRepository.findPublishedById(subjectId)
                .orElseThrow(() -> new SubjectException(ExceptionMessage.SUBJECT_NOT_FOUND));

        WordPracticeCycle cycle = cycleRepository.findLatestByParticipantIdAndSubjectIdWithLock(participant.getId(), subject.getId())
                .orElseGet(() -> createFirstCycle(participant, subject.getId()));
        return toCycleResponse(cycle, resolution.issuedGuestToken().orElse(null));
    }

    /**
     * 최초 회차에만 현재 공개 말문제 후보를 스냅샷하고 새 UUID seed로 고정 순서를 생성한다.
     * 문제 본문은 읽지 않으며, 이후 Task의 문제 묶음 조회에서만 상세 콘텐츠를 가져온다.
     */
    private WordPracticeCycle createFirstCycle(WordPracticeParticipant participant, Long subjectId) {
        List<WordProblemCandidateProjection> candidates = problemRepository.findPublishedWordProblemCandidatesBySubjectId(subjectId);
        if (candidates.isEmpty()) {
            throw new WordPracticeException(ExceptionMessage.WORD_PRACTICE_NO_PROBLEMS);
        }
        String shuffleSeed = uuidHolder.getRandom();
        WordPracticeCycle cycle = WordPracticeCycle.start(
                participant,
                subjectId,
                1,
                shuffleSeed,
                orderStrategy.createOrder(candidates, shuffleSeed),
                clockHolder.getCurrentDateTime()
        );
        return cycleRepository.save(cycle);
    }

    /** HTTP 응답이 도메인 엔티티를 직접 노출하지 않도록 진행률과 발급 token을 변환한다. */
    private WordPracticeCycleResponse toCycleResponse(WordPracticeCycle cycle, String issuedGuestToken) {
        WordPracticeProgressResponse progress = new WordPracticeProgressResponse(
                cycle.getSolvedCount(), cycle.getCorrectCount(), cycle.getIncorrectCount(),
                cycle.getTotalCount(), cycle.getRemainingCount()
        );
        return new WordPracticeCycleResponse(
                cycle.getId(), cycle.getSubjectId(), cycle.getRoundNumber(), cycle.getStatus().name(), issuedGuestToken, progress
        );
    }

    /**
     * 회차 잠금 아래에서 현재 nextIndex의 문제를 배치로 읽는다. 중간에 삭제·비공개된 문제는
     * 아직 답하지 않은 order에서만 제거하고, 답안 제출 전 재시도는 nextIndex를 바꾸지 않는다.
     */
    @Override
    @Transactional
    public WordPracticeProblemBatchResponse getNextProblems(Long memberId, String guestToken, Long cycleId, int count) {
        validateBatchCount(count);
        WordPracticeParticipant participant = participantResolver.resolveForWrite(memberId, guestToken)
                .orElseThrow(() -> new WordPracticeException(ExceptionMessage.NO_AUTHORIZATION));
        WordPracticeCycle cycle = cycleRepository.findByIdWithLock(cycleId)
                .orElseThrow(() -> new WordPracticeException(ExceptionMessage.WORD_PRACTICE_CYCLE_NOT_FOUND));
        validateOwnership(participant, cycle);

        if (cycle.isCompleted()) {
            return toProblemBatchResponse(cycle, count, List.of());
        }

        List<Problem> orderedProblems = findAvailableProblemsInOrder(cycle, count);
        Map<Long, List<ChoiceResponse>> choicesByProblemId = orderedProblems.isEmpty()
                ? Map.of() : choiceService.findAllByProblemIds(orderedProblems.stream().map(Problem::getId).toList());
        return toProblemBatchResponse(cycle, count, orderedProblems.stream()
                .map(problem -> problemV2ResponseAssembler.assemble(problem,
                        choicesByProblemId.getOrDefault(problem.getId(), List.of())))
                .toList());
    }

    /** count는 화면 UX와 조회량을 고정하기 위해 5·10·15 중 하나만 허용한다. */
    private void validateBatchCount(int count) {
        if (count != 5 && count != 10 && count != 15) {
            throw new WordPracticeException(ExceptionMessage.INVALID_DATA);
        }
    }

    /** 다른 회원 또는 다른 익명 token이 회차 ID를 추측해도 풀이 순서를 볼 수 없게 막는다. */
    private void validateOwnership(WordPracticeParticipant participant, WordPracticeCycle cycle) {
        if (!cycle.getParticipant().getId().equals(participant.getId())) {
            throw new WordPracticeException(ExceptionMessage.WORD_PRACTICE_NOT_OWNER);
        }
    }

    /**
     * 현재 미풀이 order에서 필요한 수만큼만 배치 조회한다. 누락된 ID는 공개 계층에서 제외된
     * 문제이므로 order에서 제거한 뒤 부족한 자리만 다음 chunk로 보충한다.
     */
    private List<Problem> findAvailableProblemsInOrder(WordPracticeCycle cycle, int count) {
        List<Problem> result = new ArrayList<>();
        while (result.size() < count && cycle.getNextIndex() < cycle.getProblemOrder().size()) {
            List<Long> currentOrder = cycle.getProblemOrder();
            int chunkStartIndex = cycle.getNextIndex() + result.size();
            if (chunkStartIndex >= currentOrder.size()) {
                break;
            }
            List<Long> requestedIds = currentOrder.subList(
                    chunkStartIndex,
                    Math.min(currentOrder.size(), chunkStartIndex + count - result.size())
            );
            List<Problem> foundProblems = problemRepository.findPublishedWordProblemsByIds(requestedIds);
            Map<Long, Problem> foundById = new HashMap<>();
            for (Problem problem : foundProblems) {
                foundById.put(problem.getId(), problem);
            }

            // IN 조회의 반환 순서와 무관하게 회차에 저장된 고정 order로 응답을 재조립한다.
            List<Long> unavailableIds = new ArrayList<>();
            for (Long problemId : requestedIds) {
                Problem problem = foundById.get(problemId);
                if (problem == null) {
                    unavailableIds.add(problemId);
                } else {
                    result.add(problem);
                }
            }
            if (unavailableIds.isEmpty()) {
                break;
            }
            cycle.removeUnavailableProblemIds(unavailableIds);
        }
        cycle.completeIfFinished(clockHolder.getCurrentDateTime());
        return result;
    }

    /** 회차 상태와 서버가 보유한 진행률을 함께 반환해 클라이언트가 별도 진행률 조회를 하지 않게 한다. */
    private WordPracticeProblemBatchResponse toProblemBatchResponse(
            WordPracticeCycle cycle,
            int requestedCount,
            List<com.cpa.yusin.quiz.problem.controller.dto.response.ProblemV2Response> problems
    ) {
        WordPracticeProgressResponse progress = new WordPracticeProgressResponse(
                cycle.getSolvedCount(), cycle.getCorrectCount(), cycle.getIncorrectCount(),
                cycle.getTotalCount(), cycle.getRemainingCount());
        return new WordPracticeProblemBatchResponse(
                cycle.getId(), requestedCount, problems.size(), cycle.getRemainingCount() > problems.size(),
                cycle.getStatus().name(), progress, problems);
    }

    /**
     * 회차 행 잠금 안에서 최초 답안을 저장한다. 이 잠금은 같은 문제의 동시 재시도가
     * 통계를 두 번 올리지 않게 하며, 기존 StudySession/SubmittedAnswer 흐름과는 분리되어 사용된다.
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, noRollbackFor = WordPracticeException.class)
    public WordPracticeAnswerResponse submitAnswer(
            Long memberId, String guestToken, Long cycleId, Long problemId, Long choiceId
    ) {
        WordPracticeParticipant participant = participantResolver.resolveForWrite(memberId, guestToken)
                .orElseThrow(() -> new WordPracticeException(ExceptionMessage.NO_AUTHORIZATION));
        WordPracticeCycle cycle = cycleRepository.findByIdWithLock(cycleId)
                .orElseThrow(() -> new WordPracticeException(ExceptionMessage.WORD_PRACTICE_CYCLE_NOT_FOUND));
        validateOwnership(participant, cycle);

        Optional<WordPracticeAnswer> existingAnswer = answerRepository.findByCycleIdAndProblemId(cycleId, problemId);
        if (existingAnswer.isPresent()) {
            if (existingAnswer.get().getChoiceId().equals(choiceId)) {
                return toAnswerResponse(existingAnswer.get(), cycle);
            }
            throw new WordPracticeException(ExceptionMessage.WORD_PRACTICE_ANSWER_ALREADY_SUBMITTED);
        }
        if (cycle.isCompleted()) {
            throw new WordPracticeException(ExceptionMessage.WORD_PRACTICE_CYCLE_COMPLETED);
        }
        if (!cycle.currentProblemId().filter(problemId::equals).isPresent()) {
            throw new WordPracticeException(ExceptionMessage.WORD_PRACTICE_OUT_OF_ORDER);
        }

        // 공개 계층 또는 말문제 조건에서 빠진 문제는 답안을 남기지 않고 회차에서만 안전하게 제외한다.
        if (problemRepository.findPublishedWordProblemsByIds(List.of(problemId)).isEmpty()) {
            cycle.removeUnavailableProblemIds(List.of(problemId));
            cycle.completeIfFinished(clockHolder.getCurrentDateTime());
            throw new WordPracticeException(ExceptionMessage.WORD_PRACTICE_PROBLEM_UNAVAILABLE);
        }

        Choice choice = choiceService.findById(choiceId);
        if (!choice.getProblem().getId().equals(problemId)) {
            throw new WordPracticeException(ExceptionMessage.INVALID_DATA);
        }
        WordPracticeAnswer answer = WordPracticeAnswer.create(
                cycle, problemId, choiceId, cycle.getNextIndex() + 1, choice.getIsAnswer(), clockHolder.getCurrentDateTime()
        );
        WordPracticeAnswer savedAnswer = answerRepository.save(answer);
        cycle.markAnswered(choice.getIsAnswer());
        cycle.completeIfFinished(clockHolder.getCurrentDateTime());
        publishCompletedLearningUnitForSingleAnswer(memberId, cycle);
        return toAnswerResponse(savedAnswer, cycle);
    }

    /**
     * 이전 단건 API를 사용하는 앱도 다섯 문제 또는 마지막 잔여 묶음을 끝냈을 때만 학습 기록을 만든다.
     */
    private void publishCompletedLearningUnitForSingleAnswer(Long memberId, WordPracticeCycle cycle) {
        if (memberId == null) {
            return;
        }
        int solvedCount = cycle.getSolvedCount();
        boolean completedFullUnit = solvedCount % 5 == 0;
        if (!completedFullUnit && !cycle.isCompleted()) {
            return;
        }
        int completedUnitSize = completedFullUnit ? 5 : solvedCount % 5;
        eventPublisher.publishEvent(new StudySolvedEvent(memberId, completedUnitSize));
    }

    /** 답안 저장과 동일 답안 재시도가 같은 진행률 응답 형태를 공유하도록 변환한다. */
    private WordPracticeAnswerResponse toAnswerResponse(WordPracticeAnswer answer, WordPracticeCycle cycle) {
        return WordPracticeAnswerResponse.from(answer, toCycleResponse(cycle, null));
    }

    /**
     * 현재 서버 cursor의 한 문제 묶음을 먼저 전부 검증한 뒤 저장한다. cycle 행 잠금과 답안 유니크 제약으로
     * 동시 요청을 직렬화하며, 이미 저장된 동일 payload는 통계와 학습 기록을 다시 올리지 않고 반환한다.
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public WordPracticeAnswerBatchResponse submitAnswerBatch(
            Long memberId,
            String guestToken,
            Long cycleId,
            List<WordPracticeAnswerRequest> requests
    ) {
        validateAnswerBatchRequest(requests);
        WordPracticeParticipant participant = participantResolver.resolveForWrite(memberId, guestToken)
                .orElseThrow(() -> new WordPracticeException(ExceptionMessage.NO_AUTHORIZATION));
        WordPracticeCycle cycle = cycleRepository.findByIdWithLock(cycleId)
                .orElseThrow(() -> new WordPracticeException(ExceptionMessage.WORD_PRACTICE_CYCLE_NOT_FOUND));
        validateOwnership(participant, cycle);

        List<Long> problemIds = requests.stream().map(WordPracticeAnswerRequest::problemId).toList();
        List<WordPracticeAnswer> existingAnswers = answerRepository.findAllByCycleIdAndProblemIds(cycleId, problemIds);
        if (!existingAnswers.isEmpty()) {
            return toIdempotentBatchResponse(requests, existingAnswers, cycle);
        }
        if (cycle.isCompleted()) {
            throw new WordPracticeException(ExceptionMessage.WORD_PRACTICE_CYCLE_COMPLETED);
        }

        int expectedCount = Math.min(5, cycle.getRemainingCount());
        if (requests.size() != expectedCount) {
            throw new WordPracticeException(ExceptionMessage.INVALID_DATA);
        }
        List<Long> expectedProblemIds = cycle.getProblemOrder().subList(
                cycle.getNextIndex(), cycle.getNextIndex() + expectedCount);
        if (!expectedProblemIds.equals(problemIds)) {
            throw new WordPracticeException(ExceptionMessage.WORD_PRACTICE_OUT_OF_ORDER);
        }

        List<Problem> availableProblems = problemRepository.findPublishedWordProblemsByIds(problemIds);
        if (availableProblems.size() != problemIds.size()
                || !availableProblems.stream().map(Problem::getId).collect(Collectors.toSet()).containsAll(problemIds)) {
            throw new WordPracticeException(ExceptionMessage.WORD_PRACTICE_PROBLEM_UNAVAILABLE);
        }

        List<Choice> choices = requests.stream().map(request -> {
            Choice choice = choiceService.findById(request.choiceId());
            if (!choice.getProblem().getId().equals(request.problemId())) {
                throw new WordPracticeException(ExceptionMessage.INVALID_DATA);
            }
            return choice;
        }).toList();

        LocalDateTime submittedAt = clockHolder.getCurrentDateTime();
        int firstSequence = cycle.getNextIndex() + 1;
        List<WordPracticeAnswer> newAnswers = new ArrayList<>();
        for (int index = 0; index < requests.size(); index++) {
            WordPracticeAnswerRequest request = requests.get(index);
            Choice choice = choices.get(index);
            newAnswers.add(WordPracticeAnswer.create(
                    cycle,
                    request.problemId(),
                    request.choiceId(),
                    firstSequence + index,
                    choice.getIsAnswer(),
                    submittedAt
            ));
        }

        List<WordPracticeAnswer> savedAnswers = answerRepository.saveAll(newAnswers);
        for (Choice choice : choices) {
            cycle.markAnswered(choice.getIsAnswer());
        }
        cycle.completeIfFinished(clockHolder.getCurrentDateTime());
        publishCompletedBatchForMember(memberId, savedAnswers.size());
        return toAnswerBatchResponse(savedAnswers, cycle);
    }

    /** controller 검증을 우회한 서비스 호출에도 같은 최대 다섯 개 계약을 적용한다. */
    private void validateAnswerBatchRequest(List<WordPracticeAnswerRequest> requests) {
        if (requests == null || requests.isEmpty() || requests.size() > 5
                || requests.stream().anyMatch(request -> request == null
                || request.problemId() == null || request.problemId() <= 0
                || request.choiceId() == null || request.choiceId() <= 0)
                || new HashSet<>(requests.stream().map(WordPracticeAnswerRequest::problemId).toList()).size()
                != requests.size()) {
            throw new WordPracticeException(ExceptionMessage.INVALID_DATA);
        }
    }

    /** 일부만 기존인 상태는 원자적 배치 계약과 맞지 않으므로 충돌로 처리한다. */
    private WordPracticeAnswerBatchResponse toIdempotentBatchResponse(
            List<WordPracticeAnswerRequest> requests,
            List<WordPracticeAnswer> existingAnswers,
            WordPracticeCycle cycle
    ) {
        Map<Long, WordPracticeAnswer> existingByProblemId = existingAnswers.stream()
                .collect(Collectors.toMap(WordPracticeAnswer::getProblemId, Function.identity()));
        List<WordPracticeAnswer> orderedAnswers = new ArrayList<>();
        Integer firstSequence = null;
        for (int index = 0; index < requests.size(); index++) {
            WordPracticeAnswerRequest request = requests.get(index);
            WordPracticeAnswer existing = existingByProblemId.get(request.problemId());
            if (existing == null || !existing.getChoiceId().equals(request.choiceId())) {
                throw new WordPracticeException(ExceptionMessage.WORD_PRACTICE_ANSWER_ALREADY_SUBMITTED);
            }
            if (firstSequence == null) {
                firstSequence = existing.getSequence();
                int expectedBatchSize = Math.min(5, cycle.getTotalCount() - firstSequence + 1);
                if (requests.size() != expectedBatchSize) {
                    throw new WordPracticeException(ExceptionMessage.WORD_PRACTICE_ANSWER_ALREADY_SUBMITTED);
                }
            }
            if (existing.getSequence() != firstSequence + index) {
                throw new WordPracticeException(ExceptionMessage.WORD_PRACTICE_ANSWER_ALREADY_SUBMITTED);
            }
            orderedAnswers.add(existing);
        }
        return toAnswerBatchResponse(orderedAnswers, cycle);
    }

    /** 한 번의 학습 단위 완료 이벤트에 실제 저장된 문제 수를 담는다. */
    private void publishCompletedBatchForMember(Long memberId, int solvedCount) {
        if (memberId != null) {
            eventPublisher.publishEvent(new StudySolvedEvent(memberId, solvedCount));
        }
    }

    private WordPracticeAnswerBatchResponse toAnswerBatchResponse(
            List<WordPracticeAnswer> answers,
            WordPracticeCycle cycle
    ) {
        return WordPracticeAnswerBatchResponse.from(answers, toCycleResponse(cycle, null));
    }

    /**
     * 완료된 최신 회차만 다음 round로 전환한다. 참여자 행을 먼저 잠가 동시 restart 요청이
     * 동일한 roundNumber를 두 번 만들지 못하게 하고, 이전 cycle·answer 이력은 수정하지 않는다.
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public WordPracticeCycleResponse restartCycle(Long memberId, String guestToken, Long cycleId) {
        WordPracticeParticipant resolvedParticipant = participantResolver.resolveForWrite(memberId, guestToken)
                .orElseThrow(() -> new WordPracticeException(ExceptionMessage.NO_AUTHORIZATION));
        WordPracticeParticipant participant = participantRepository.findByIdWithLock(resolvedParticipant.getId())
                .orElseThrow(() -> new IllegalStateException("Word practice participant was not persisted"));
        WordPracticeCycle sourceCycle = cycleRepository.findByIdWithLock(cycleId)
                .orElseThrow(() -> new WordPracticeException(ExceptionMessage.WORD_PRACTICE_CYCLE_NOT_FOUND));
        validateOwnership(participant, sourceCycle);
        if (!sourceCycle.isCompleted()) {
            throw new WordPracticeException(ExceptionMessage.WORD_PRACTICE_NOT_COMPLETED);
        }

        WordPracticeCycle latestCycle = cycleRepository.findLatestByParticipantIdAndSubjectIdWithLock(
                        participant.getId(), sourceCycle.getSubjectId())
                .orElseThrow(() -> new IllegalStateException("Word practice cycle was not persisted"));
        if (!latestCycle.getId().equals(sourceCycle.getId())) {
            throw new WordPracticeException(ExceptionMessage.WORD_PRACTICE_NOT_LATEST_CYCLE);
        }

        List<WordProblemCandidateProjection> candidates = problemRepository
                .findPublishedWordProblemCandidatesBySubjectId(sourceCycle.getSubjectId());
        if (candidates.isEmpty()) {
            throw new WordPracticeException(ExceptionMessage.WORD_PRACTICE_NO_PROBLEMS);
        }
        String shuffleSeed = uuidHolder.getRandom();
        WordPracticeCycle restartedCycle = WordPracticeCycle.start(
                participant, sourceCycle.getSubjectId(), sourceCycle.getRoundNumber() + 1,
                shuffleSeed, orderStrategy.createOrder(candidates, shuffleSeed), clockHolder.getCurrentDateTime()
        );
        return toCycleResponse(cycleRepository.save(restartedCycle), null);
    }
}
