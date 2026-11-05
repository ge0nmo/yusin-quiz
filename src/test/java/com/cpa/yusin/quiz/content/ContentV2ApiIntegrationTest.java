package com.cpa.yusin.quiz.content;

import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.common.domain.ContentStatus;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.infrastructure.ExamRepository;
import com.cpa.yusin.quiz.file.controller.port.FileService;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.infrastructure.ProblemRepository;
import com.cpa.yusin.quiz.qualification.domain.QualificationExam;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamCode;
import com.cpa.yusin.quiz.qualification.domain.QualificationExamSubject;
import com.cpa.yusin.quiz.qualification.infrastructure.QualificationExamRepository;
import com.cpa.yusin.quiz.qualification.infrastructure.QualificationExamSubjectRepository;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.infrastructure.SubjectRepository;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;
import java.util.Map;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
class ContentV2ApiIntegrationTest {
    private static final String CONTENT = "/api/v2/qualification-exams/{code}/content";
    private static final String EXPLANATIONS = "/api/v2/qualification-exams/{code}/explanations";

    @Autowired WebApplicationContext context;
    @Autowired ObjectMapper objectMapper;
    @Autowired QualificationExamRepository qualificationExamRepository;
    @Autowired QualificationExamSubjectRepository mappingRepository;
    @Autowired SubjectRepository subjectRepository;
    @Autowired ExamRepository examRepository;
    @Autowired ProblemRepository problemRepository;
    @MockBean FileService fileService;
    // CI는 시크릿의 테스트 설정을 쓰므로 관리 이미지 경로는 설정된 prefix로 만든다.
    @Value("${cloud.aws.s3.prefix}") String s3Prefix;

    private MockMvc mockMvc;
    private QualificationExamSubject accountingMapping;
    private Subject economics;
    private Problem olderProblem;
    private Problem newerProblem;
    private Problem draftProblem;
    private Problem otherQualificationProblem;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
        problemRepository.deleteAll();
        examRepository.deleteAll();
        mappingRepository.deleteAll();
        subjectRepository.deleteAll();
        qualificationExamRepository.deleteAll();

        QualificationExam appraiser = qualificationExamRepository.save(
                new QualificationExam(QualificationExamCode.APPRAISER, ContentStatus.PUBLISHED));
        Subject accounting = subjectRepository.save(new Subject("회계학", ContentStatus.PUBLISHED));
        economics = subjectRepository.save(new Subject("경제학원론", ContentStatus.PUBLISHED));
        accountingMapping = mappingRepository.save(
                new QualificationExamSubject(appraiser, accounting, ContentStatus.PUBLISHED, 1));
        mappingRepository.save(new QualificationExamSubject(appraiser, economics, ContentStatus.PUBLISHED, 2));
        Exam exam2022 = examRepository.save(new Exam(appraiser, "2022년 1차", 2022, ContentStatus.PUBLISHED));
        Exam exam2025 = examRepository.save(new Exam(appraiser, "2025년 1차", 2025, ContentStatus.PUBLISHED));

        olderProblem = saveProblem(exam2022, accountingMapping, 41, "오래된 문제", ContentStatus.PUBLISHED);
        newerProblem = saveProblem(exam2025, accountingMapping, 56, "새 문제", ContentStatus.PUBLISHED);
        draftProblem = saveProblem(exam2025, accountingMapping, 57, "작성 중 문제", ContentStatus.DRAFT);

        QualificationExam cpa = qualificationExamRepository.save(
                new QualificationExam(QualificationExamCode.CPA, ContentStatus.PUBLISHED));
        Subject costAccounting = subjectRepository.save(new Subject("원가회계", ContentStatus.PUBLISHED));
        QualificationExamSubject cpaMapping = mappingRepository.save(
                new QualificationExamSubject(cpa, costAccounting, ContentStatus.PUBLISHED, 1));
        Exam cpaExam = examRepository.save(new Exam(cpa, "2025년 1차", 2025, ContentStatus.PUBLISHED));
        otherQualificationProblem = saveProblem(cpaExam, cpaMapping, 1, "다른 자격시험 문제", ContentStatus.PUBLISHED);
    }

    @Test
    void contentIncludesAnswersWithoutExplanationsInBackendOrder() throws Exception {
        Choice correctChoice = newerProblem.getChoices().stream().filter(Choice::isAnswer).findFirst().orElseThrow();

        MvcResult result = mockMvc.perform(get(CONTENT, "APPRAISER"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ETAG))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache"))
                .andExpect(jsonPath("$.data.qualificationExamCode").value("APPRAISER"))
                .andExpect(jsonPath("$.data.imageUrlsExpireAt").value(nullValue()))
                .andExpect(jsonPath("$.data.subjects", hasSize(2)))
                .andExpect(jsonPath("$.data.subjects[0].name").value("회계학"))
                .andExpect(jsonPath("$.data.subjects[0].problemCount").value(2))
                .andExpect(jsonPath("$.data.subjects[0].problems", hasSize(2)))
                .andExpect(jsonPath("$.data.subjects[0].problems[0].id").value(newerProblem.getId()))
                .andExpect(jsonPath("$.data.subjects[0].problems[1].id").value(olderProblem.getId()))
                .andExpect(jsonPath("$.data.subjects[0].problems[0].exam.year").value(2025))
                .andExpect(jsonPath("$.data.subjects[0].problems[0].correctChoiceIds[0]").value(correctChoice.getId()))
                .andExpect(jsonPath("$.data.subjects[0].problems[0].choices", hasSize(5)))
                .andExpect(jsonPath("$.data.subjects[0].problems[0].choices[0].number").value(1))
                .andExpect(jsonPath("$.data.subjects[0].problems[0].choices[0].answer").doesNotExist())
                .andExpect(jsonPath("$.data.subjects[0].problems[0].choices[0].isAnswer").doesNotExist())
                .andExpect(jsonPath("$.data.subjects[0].problems[0].choices[0].explanation").doesNotExist())
                .andExpect(jsonPath("$.data.subjects[0].problems[0].explanation").doesNotExist())
                .andExpect(jsonPath("$.data.subjects[1].name").value("경제학원론"))
                .andExpect(jsonPath("$.data.subjects[1].problemCount").value(0))
                .andExpect(jsonPath("$.data.subjects[1].problems", hasSize(0)))
                .andDo(document("public-v2-content", api("Public Content v2", "문제·보기·정답 전체 스냅샷 조회")))
                .andReturn();

        JsonNode data = data(result);
        assertThat(result.getResponse().getHeader(HttpHeaders.ETAG)).isEqualTo("\"" + data.path("version").asText() + "\"");
        assertThat(data.path("version").asText()).startsWith("sha256:");
        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("작성 중 문제")
                .doesNotContain("다른 자격시험 문제");
    }

    @Test
    void explanationsListEveryPublishedProblemByIdWithoutAnswers() throws Exception {
        olderProblem.update(olderProblem.getExam(), olderProblem.getSubjectMapping(), olderProblem.getNumber(),
                olderProblem.getStatus(), olderProblem.getContent(), List.of());
        problemRepository.saveAndFlush(olderProblem);

        MvcResult result = mockMvc.perform(get(EXPLANATIONS, "APPRAISER"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ETAG))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache"))
                .andExpect(jsonPath("$.data.qualificationExamCode").value("APPRAISER"))
                .andExpect(jsonPath("$.data.imageUrlsExpireAt").value(nullValue()))
                .andExpect(jsonPath("$.data.problems", hasSize(2)))
                .andExpect(jsonPath("$.data.problems[0].problemId").value(olderProblem.getId()))
                .andExpect(jsonPath("$.data.problems[0].explanation", hasSize(0)))
                .andExpect(jsonPath("$.data.problems[1].problemId").value(newerProblem.getId()))
                .andExpect(jsonPath("$.data.problems[1].explanation[0].text").value("종합 해설"))
                .andExpect(jsonPath("$.data.problems[1].choices", hasSize(5)))
                .andExpect(jsonPath("$.data.problems[1].choices[0].explanation", hasSize(0)))
                .andExpect(jsonPath("$.data.problems[1].choices[1].explanation[0].text").value("정답 해설"))
                .andExpect(jsonPath("$.data.problems[1].correctChoiceIds").doesNotExist())
                .andDo(document("public-v2-explanations", api("Public Content v2", "문제·보기별 해설 전체 조회")))
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("\"problemId\":" + draftProblem.getId() + ",")
                .doesNotContain("\"problemId\":" + otherQualificationProblem.getId() + ",");
    }

    @Test
    void sameDataKeepsVersionAndMatchingIfNoneMatchReturnsNotModified() throws Exception {
        for (String path : List.of(CONTENT, EXPLANATIONS)) {
            String etag = etag(path);
            assertThat(etag(path)).isEqualTo(etag);

            mockMvc.perform(get(path, "APPRAISER").header(HttpHeaders.IF_NONE_MATCH, etag))
                    .andExpect(status().isNotModified())
                    .andExpect(header().string(HttpHeaders.ETAG, etag))
                    .andExpect(content().string(""));
            mockMvc.perform(get(path, "APPRAISER").header(HttpHeaders.IF_NONE_MATCH, "\"sha256:old\", W/" + etag))
                    .andExpect(status().isNotModified());
            mockMvc.perform(get(path, "APPRAISER").header(HttpHeaders.IF_NONE_MATCH, "\"sha256:old\""))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void contentVersionChangesWithProblemsChoicesAnswersAndPublication() throws Exception {
        String version = etag(CONTENT);

        newerProblem.update(newerProblem.getExam(), newerProblem.getSubjectMapping(), newerProblem.getNumber(),
                newerProblem.getStatus(), List.of(Map.of("type", "text", "text", "고친 문제")), newerProblem.getExplanation());
        problemRepository.saveAndFlush(newerProblem);
        version = assertChanged(CONTENT, version);

        updateChoice(newerProblem.getId(), 3, "고친 보기", false);
        version = assertChanged(CONTENT, version);

        updateChoice(newerProblem.getId(), 4, "보기 4", true);
        version = assertChanged(CONTENT, version);

        problemRepository.deleteById(olderProblem.getId());
        version = assertChanged(CONTENT, version);

        accountingMapping.update(ContentStatus.DRAFT, accountingMapping.getDisplayOrder());
        mappingRepository.saveAndFlush(accountingMapping);
        assertChanged(CONTENT, version);
    }

    @Test
    void explanationEditsChangeOnlyExplanationVersion() throws Exception {
        String contentVersion = etag(CONTENT);
        String explanationVersion = etag(EXPLANATIONS);

        newerProblem.update(newerProblem.getExam(), newerProblem.getSubjectMapping(), newerProblem.getNumber(),
                newerProblem.getStatus(), newerProblem.getContent(), List.of(Map.of("type", "text", "text", "고친 해설")));
        problemRepository.saveAndFlush(newerProblem);
        explanationVersion = assertChanged(EXPLANATIONS, explanationVersion);
        assertThat(etag(CONTENT)).isEqualTo(contentVersion);

        Problem problem = problemRepository.findDetailById(newerProblem.getId()).orElseThrow();
        Choice choice = problem.getChoices().stream().filter(candidate -> candidate.getNumber() == 1).findFirst().orElseThrow();
        choice.update(choice.getContent(), choice.isAnswer(), List.of(Map.of("type", "text", "text", "보기 1 해설")));
        problemRepository.saveAndFlush(problem);
        assertChanged(EXPLANATIONS, explanationVersion);
        assertThat(etag(CONTENT)).isEqualTo(contentVersion);
    }

    @Test
    void freshlySignedImageUrlsDoNotChangeVersion() throws Exception {
        String objectKey = s3Prefix + "/problem.png";
        String managedImage = "https://bucket.s3.amazonaws.com/" + objectKey;
        newerProblem.update(newerProblem.getExam(), newerProblem.getSubjectMapping(), newerProblem.getNumber(),
                newerProblem.getStatus(), List.of(Map.of("type", "image", "src", managedImage)),
                List.of(Map.of("type", "image", "src", managedImage)));
        problemRepository.saveAndFlush(newerProblem);
        when(fileService.generatePresignedUrl(objectKey))
                .thenReturn("https://signed/1", "https://signed/2", "https://signed/3", "https://signed/4");

        JsonNode first = data(mockMvc.perform(get(CONTENT, "APPRAISER")).andExpect(status().isOk()).andReturn());
        JsonNode second = data(mockMvc.perform(get(CONTENT, "APPRAISER")).andExpect(status().isOk()).andReturn());
        assertThat(first.path("version")).isEqualTo(second.path("version"));
        assertThat(first.at("/subjects/0/problems/0/content/0/src").asText()).isEqualTo("https://signed/1");
        assertThat(second.at("/subjects/0/problems/0/content/0/src").asText()).isEqualTo("https://signed/2");
        assertThat(first.path("imageUrlsExpireAt").isTextual()).isTrue();

        JsonNode explanations = data(mockMvc.perform(get(EXPLANATIONS, "APPRAISER")).andExpect(status().isOk()).andReturn());
        assertThat(explanations.at("/problems/1/explanation/0/src").asText()).isEqualTo("https://signed/3");
        assertThat(explanations.path("imageUrlsExpireAt").isTextual()).isTrue();
        assertThat(etag(EXPLANATIONS)).isEqualTo("\"" + explanations.path("version").asText() + "\"");
    }

    @Test
    void choiceImagesAreFreshlySignedAndVersionedByObjectKey() throws Exception {
        JsonNode withoutImages = data(mockMvc.perform(get(CONTENT, "APPRAISER")).andExpect(status().isOk()).andReturn());
        JsonNode plainChoice = withoutImages.at("/subjects/0/problems/0/choices/0");
        assertThat(plainChoice.has("imageUrl")).isTrue();
        assertThat(plainChoice.path("imageUrl").isNull()).isTrue();
        String versionWithoutImages = withoutImages.path("version").asText();

        // 관리 이미지가 아닌 http(s) URL은 그대로 내려주고 서명 만료 시각도 없다.
        String externalUrl = "https://cdn.example.com/graph.png";
        updateChoiceImage(newerProblem.getId(), 2, externalUrl);
        JsonNode external = data(mockMvc.perform(get(CONTENT, "APPRAISER")).andExpect(status().isOk()).andReturn());
        assertThat(external.at("/subjects/0/problems/0/choices/1/imageUrl").asText()).isEqualTo(externalUrl);
        assertThat(external.path("imageUrlsExpireAt").isNull()).isTrue();
        assertThat(external.path("version").asText()).isNotEqualTo(versionWithoutImages);
        updateChoiceImage(newerProblem.getId(), 2, null);

        String objectKey = s3Prefix + "/choice-graph.png";
        String managedImage = "https://bucket.s3.amazonaws.com/" + objectKey;
        updateChoice(newerProblem.getId(), 1, "", false);
        updateChoiceImage(newerProblem.getId(), 1, managedImage + "?X-Amz-Signature=first");
        when(fileService.generatePresignedUrl(objectKey))
                .thenReturn("https://signed/1", "https://signed/2", "https://signed/3", "https://signed/4");

        JsonNode first = data(mockMvc.perform(get(CONTENT, "APPRAISER")).andExpect(status().isOk()).andReturn());
        JsonNode second = data(mockMvc.perform(get(CONTENT, "APPRAISER")).andExpect(status().isOk()).andReturn());
        assertThat(first.at("/subjects/0/problems/0/choices/0/content").asText()).isEmpty();
        assertThat(first.at("/subjects/0/problems/0/choices/0/imageUrl").asText()).isEqualTo("https://signed/1");
        assertThat(second.at("/subjects/0/problems/0/choices/0/imageUrl").asText()).isEqualTo("https://signed/2");
        assertThat(first.at("/subjects/0/problems/0/choices/1/imageUrl").isNull()).isTrue();
        // 본문 이미지 블록이 없어도 보기 이미지만으로 서명 만료 시각을 알린다.
        assertThat(first.at("/subjects/0/problems/0/content/0/type").asText()).isEqualTo("text");
        assertThat(first.path("imageUrlsExpireAt").isTextual()).isTrue();
        String imageVersion = first.path("version").asText();
        assertThat(second.path("version").asText()).isEqualTo(imageVersion);
        assertThat(imageVersion).isNotEqualTo(versionWithoutImages);

        // 저장된 URL의 서명만 달라지면 version은 그대로다.
        updateChoiceImage(newerProblem.getId(), 1, managedImage + "?X-Amz-Signature=second");
        assertThat(etag(CONTENT)).isEqualTo("\"" + imageVersion + "\"");

        // 다른 이미지로 바꾸면 version이 바뀐다.
        updateChoiceImage(newerProblem.getId(), 1, "https://bucket.s3.amazonaws.com/" + s3Prefix + "/other.png");
        String replacedVersion = assertChanged(CONTENT, "\"" + imageVersion + "\"");

        // 이미지를 지우면 version이 바뀌고, 본문까지 되돌리면 처음 version과 같다.
        updateChoiceImage(newerProblem.getId(), 1, null);
        assertChanged(CONTENT, replacedVersion);
        updateChoice(newerProblem.getId(), 1, "보기 1", false);
        assertThat(etag(CONTENT)).isEqualTo("\"" + versionWithoutImages + "\"");
    }

    @Test
    void choiceImagesDoNotChangeExplanations() throws Exception {
        String objectKey = s3Prefix + "/choice-graph.png";
        String explanationsVersion = etag(EXPLANATIONS);

        updateChoiceImage(newerProblem.getId(), 1, "https://bucket.s3.amazonaws.com/" + objectKey);
        when(fileService.generatePresignedUrl(objectKey)).thenReturn("https://signed/choice");

        MvcResult explanations = mockMvc.perform(get(EXPLANATIONS, "APPRAISER")).andExpect(status().isOk()).andReturn();
        assertThat(explanations.getResponse().getContentAsString()).doesNotContain("\"imageUrl\"");
        assertThat(data(explanations).path("imageUrlsExpireAt").isNull()).isTrue();
        assertThat(explanations.getResponse().getHeader(HttpHeaders.ETAG)).isEqualTo(explanationsVersion);
    }

    @Test
    void unknownOrUnpublishedQualificationIsNotFound() throws Exception {
        mockMvc.perform(get(CONTENT, "CUSTOMS_BROKER"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("QUALIFICATION_EXAM_NOT_FOUND"));
        mockMvc.perform(get(EXPLANATIONS, "UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("QUALIFICATION_EXAM_NOT_FOUND"));
    }

    private Problem saveProblem(Exam exam, QualificationExamSubject mapping, int number, String text,
                                ContentStatus status) {
        Problem problem = new Problem(exam, mapping, number, status,
                List.of(Map.of("type", "text", "text", text)),
                List.of(Map.of("type", "text", "text", "종합 해설")));
        problem.replaceChoices(List.of(
                new Choice(1, "보기 1", false, List.of()),
                new Choice(2, "보기 2", true, List.of(Map.of("type", "text", "text", "정답 해설"))),
                new Choice(3, "보기 3", false, List.of()),
                new Choice(4, "보기 4", false, List.of()),
                new Choice(5, "보기 5", false, List.of())
        ));
        return problemRepository.saveAndFlush(problem);
    }

    private void updateChoice(Long problemId, int number, String content, boolean answer) {
        Problem problem = problemRepository.findDetailById(problemId).orElseThrow();
        Choice choice = problem.getChoices().stream().filter(candidate -> candidate.getNumber() == number)
                .findFirst().orElseThrow();
        choice.update(content, answer, choice.getExplanation());
        problemRepository.saveAndFlush(problem);
    }

    private void updateChoiceImage(Long problemId, int number, String imageUrl) {
        Problem problem = problemRepository.findDetailById(problemId).orElseThrow();
        Choice choice = problem.getChoices().stream().filter(candidate -> candidate.getNumber() == number)
                .findFirst().orElseThrow();
        choice.update(choice.getContent(), imageUrl, choice.isAnswer(), choice.getExplanation());
        problemRepository.saveAndFlush(problem);
    }

    private String etag(String path) throws Exception {
        return mockMvc.perform(get(path, "APPRAISER"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getHeader(HttpHeaders.ETAG);
    }

    private String assertChanged(String path, String previous) throws Exception {
        String current = etag(path);
        assertThat(current).isNotEqualTo(previous);
        return current;
    }

    private JsonNode data(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private org.springframework.restdocs.snippet.Snippet api(String tag, String summary) {
        return resource(ResourceSnippetParameters.builder().tag(tag).summary(summary).build());
    }
}
