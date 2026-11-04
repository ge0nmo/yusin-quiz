package com.cpa.yusin.quiz.content.service;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.common.domain.ContentStatus;
import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.content.controller.dto.PublicContentV2Dto.*;
import com.cpa.yusin.quiz.file.controller.port.FileService;
import com.cpa.yusin.quiz.global.exception.ContentException;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.infrastructure.ProblemRepository;
import com.cpa.yusin.quiz.problem.service.JsonBlockContentProcessor;
import com.cpa.yusin.quiz.qualification.domain.QualificationExam;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamCode;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamSubject;
import com.cpa.yusin.quiz.qualification.infrastructure.QualificationExamRepository;
import com.cpa.yusin.quiz.qualification.infrastructure.QualificationExamSubjectRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * v2 공개 콘텐츠. 앱이 시작할 때 문제·정답과 해설을 각각 한 번에 받아 기기에서 채점하도록 전체 스냅샷을 내려준다.
 * <p>
 * 해설은 향후 이용권 적용 대상이라 문제·정답과 다른 메서드와 version으로 분리한다.
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class PublicContentV2Service {
    private static final Duration IMAGE_URL_EXPIRY_MARGIN = Duration.ofMinutes(10);

    private final QualificationExamRepository qualificationExamRepository;
    private final QualificationExamSubjectRepository mappingRepository;
    private final ProblemRepository problemRepository;
    private final JsonBlockContentProcessor blockContentProcessor;
    private final ClockHolder clockHolder;
    // Map 키를 정렬해 직렬화해야 같은 데이터가 항상 같은 version이 된다.
    private final ObjectMapper hashMapper;

    public PublicContentV2Service(QualificationExamRepository qualificationExamRepository,
                                  QualificationExamSubjectRepository mappingRepository,
                                  ProblemRepository problemRepository,
                                  JsonBlockContentProcessor blockContentProcessor,
                                  ClockHolder clockHolder,
                                  ObjectMapper objectMapper) {
        this.qualificationExamRepository = qualificationExamRepository;
        this.mappingRepository = mappingRepository;
        this.problemRepository = problemRepository;
        this.blockContentProcessor = blockContentProcessor;
        this.clockHolder = clockHolder;
        this.hashMapper = objectMapper.copy().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    /**
     * 응답 version과 본문. 요청의 If-None-Match가 현재 version과 같으면 본문은 null이다.
     */
    public record Versioned<T>(String version, T body) {
        public boolean notModified() {
            return body == null;
        }
    }

    public Versioned<ContentResponse> getContent(String code, String ifNoneMatch) {
        QualificationExam qualificationExam = findPublishedQualification(code);
        List<QualificationExamSubject> mappings = findPublishedMappings(qualificationExam);
        List<Problem> problems = problemRepository.findAllPublished(qualificationExam.getCode());

        String version = hash(toSubjects(mappings, problems, ImageSources.objectKeys(blockContentProcessor)));
        if (matches(ifNoneMatch, version)) {
            return new Versioned<>(version, null);
        }
        Instant generatedAt = now();
        boolean hasImages = problems.stream().anyMatch(problem ->
                blockContentProcessor.containsManagedImage(problem.getContent())
                        || problem.getChoices().stream().anyMatch(choice ->
                        blockContentProcessor.isManagedImage(choice.getImageUrl())));
        return new Versioned<>(version, new ContentResponse(qualificationExam.getCode().name(), version, generatedAt,
                imageUrlsExpireAt(generatedAt, hasImages),
                toSubjects(mappings, problems, ImageSources.freshUrls(blockContentProcessor))));
    }

    public Versioned<ExplanationsResponse> getExplanations(String code, String ifNoneMatch) {
        QualificationExam qualificationExam = findPublishedQualification(code);
        List<Problem> problems = problemRepository.findAllPublished(qualificationExam.getCode()).stream()
                .sorted(Comparator.comparing(Problem::getId))
                .toList();

        String version = hash(toExplanations(problems, blockContentProcessor::withImageObjectKeys));
        if (matches(ifNoneMatch, version)) {
            return new Versioned<>(version, null);
        }
        Instant generatedAt = now();
        boolean hasImages = problems.stream().anyMatch(problem ->
                blockContentProcessor.containsManagedImage(problem.getExplanation())
                        || problem.getChoices().stream().anyMatch(choice ->
                        blockContentProcessor.containsManagedImage(choice.getExplanation())));
        return new Versioned<>(version, new ExplanationsResponse(qualificationExam.getCode().name(), version,
                generatedAt, imageUrlsExpireAt(generatedAt, hasImages),
                toExplanations(problems, blockContentProcessor::withFreshImageUrls)));
    }

    /**
     * 관리 이미지 URL 처리 방식. 응답에는 새 서명 URL을, version 해시에는 서명과 무관한 object key를 쓴다.
     */
    private record ImageSources(UnaryOperator<List<Map<String, Object>>> blocks, UnaryOperator<String> url) {
        static ImageSources freshUrls(JsonBlockContentProcessor processor) {
            return new ImageSources(processor::withFreshImageUrls, processor::withFreshImageUrl);
        }

        static ImageSources objectKeys(JsonBlockContentProcessor processor) {
            return new ImageSources(processor::withImageObjectKeys, processor::withImageObjectKey);
        }
    }

    private List<SubjectContentResponse> toSubjects(List<QualificationExamSubject> mappings, List<Problem> problems,
                                                    ImageSources images) {
        // 조회 순서(시험 연도 내림차순, 문제 번호 오름차순)를 과목 안에서도 유지한다.
        Map<Long, List<Problem>> problemsBySubject = problems.stream().collect(Collectors.groupingBy(
                problem -> problem.getSubjectMapping().getSubject().getId(), LinkedHashMap::new, Collectors.toList()));
        return mappings.stream()
                .map(mapping -> {
                    List<ProblemContentResponse> subjectProblems = problemsBySubject
                            .getOrDefault(mapping.getSubject().getId(), List.of()).stream()
                            .map(problem -> toProblemContent(problem, images))
                            .toList();
                    return new SubjectContentResponse(mapping.getSubject().getId(), mapping.getSubject().getName(),
                            subjectProblems.size(), subjectProblems);
                })
                .toList();
    }

    private ProblemContentResponse toProblemContent(Problem problem, ImageSources images) {
        List<Choice> choices = sortedChoices(problem);
        List<Long> correctChoiceIds = choices.stream().filter(Choice::isAnswer).map(Choice::getId).toList();
        if (correctChoiceIds.isEmpty()) {
            throw new ContentException(ExceptionMessage.INVALID_PROBLEM_CHOICES);
        }
        ExamResponse exam = new ExamResponse(problem.getExam().getId(), problem.getExam().getName(),
                problem.getExam().getYear());
        return new ProblemContentResponse(problem.getId(), problem.getNumber(), exam,
                images.blocks().apply(problem.getContent()), correctChoiceIds,
                choices.stream().map(choice -> new ChoiceResponse(choice.getId(), choice.getNumber(), choice.getContent(),
                        images.url().apply(choice.getImageUrl()))).toList());
    }

    private List<ProblemExplanationResponse> toExplanations(List<Problem> problems,
                                                            UnaryOperator<List<Map<String, Object>>> blocks) {
        return problems.stream()
                .map(problem -> new ProblemExplanationResponse(problem.getId(), blocks.apply(problem.getExplanation()),
                        sortedChoices(problem).stream()
                                .map(choice -> new ChoiceExplanationResponse(choice.getId(), blocks.apply(choice.getExplanation())))
                                .toList()))
                .toList();
    }

    private List<Choice> sortedChoices(Problem problem) {
        return problem.getChoices().stream().sorted(Comparator.comparingInt(Choice::getNumber)).toList();
    }

    private Instant imageUrlsExpireAt(Instant generatedAt, boolean hasImages) {
        // 서명 만료 직전에 받은 앱이 만료된 링크를 쓰지 않도록 10분 일찍 만료로 알린다.
        return hasImages ? generatedAt.plus(FileService.PRESIGNED_URL_TTL).minus(IMAGE_URL_EXPIRY_MARGIN) : null;
    }

    private Instant now() {
        return Instant.ofEpochMilli(clockHolder.getCurrentTime());
    }

    private String hash(Object payload) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(hashMapper.writeValueAsBytes(payload));
            return "sha256:" + HexFormat.of().formatHex(digest);
        } catch (JsonProcessingException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("콘텐츠 version을 계산할 수 없습니다.", exception);
        }
    }

    /**
     * If-None-Match의 목록, 약한 ETag(W/), 따옴표, * 를 처리한다.
     */
    private boolean matches(String ifNoneMatch, String version) {
        if (ifNoneMatch == null || ifNoneMatch.isBlank()) {
            return false;
        }
        return Arrays.stream(ifNoneMatch.split(","))
                .map(String::trim)
                .map(tag -> tag.startsWith("W/") ? tag.substring(2) : tag)
                .map(tag -> tag.length() >= 2 && tag.startsWith("\"") && tag.endsWith("\"")
                        ? tag.substring(1, tag.length() - 1) : tag)
                .anyMatch(tag -> tag.equals("*") || tag.equals(version));
    }

    private QualificationExam findPublishedQualification(String code) {
        QualificationExamCode qualificationCode = QualificationExamCode.from(code)
                .orElseThrow(() -> new ContentException(ExceptionMessage.QUALIFICATION_EXAM_NOT_FOUND));
        QualificationExam qualificationExam = qualificationExamRepository.findByCode(qualificationCode)
                .orElseThrow(() -> new ContentException(ExceptionMessage.QUALIFICATION_EXAM_NOT_FOUND));
        if (qualificationExam.getStatus() != ContentStatus.PUBLISHED) {
            throw new ContentException(ExceptionMessage.QUALIFICATION_EXAM_NOT_FOUND);
        }
        return qualificationExam;
    }

    private List<QualificationExamSubject> findPublishedMappings(QualificationExam qualificationExam) {
        return mappingRepository.findAllByQualificationExamIdOrderByDisplayOrderAscSubjectNameAsc(qualificationExam.getId())
                .stream()
                .filter(mapping -> mapping.getStatus() == ContentStatus.PUBLISHED)
                .filter(mapping -> mapping.getSubject().getStatus() == ContentStatus.PUBLISHED)
                .toList();
    }
}
