package com.cpa.yusin.quiz.problem.integration;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.domain.ExamStatus;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemLectureRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemSaveV2Request;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.domain.block.Span;
import com.cpa.yusin.quiz.problem.domain.block.TextBlock;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
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
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, TeardownExtension.class})
@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureRestDocs
public class ProblemTest
{
    @Autowired
    private MockMvc mvc;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private ChoiceRepository choiceRepository;

    @MockBean
    private ClockHolder clockHolder;

    private final ObjectMapper mapper = new ObjectMapper();

    Member admin;
    Subject english;
    Exam exam;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();

        admin = memberRepository.save(Member.builder()
                .email("John@gmail.com")
                .password("12341234")
                .username("John")
                .platform(Platform.HOME)
                .role(Role.ADMIN)
                .build());


        english = subjectRepository.save(Subject.builder()
                .id(1L)
                .name("영어")
                .build());

        exam = examRepository.save(Exam.builder()
                .id(1L)
                .year(2024)
                .name("1차")
                .subjectId(english.getId())
                .status(ExamStatus.PUBLISHED)
                .build());

        given(clockHolder.getCurrentDateTime()).willReturn(LocalDateTime.of(2026, 3, 14, 10, 0));
    }


    @Transactional
    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getById() throws Exception
    {
        // given
        String content = "The walking tour was a big ___ to some people";
        Problem problem = problemRepository.save(Problem.builder()
                .id(1L)
                .content(content)
                .explanation("문제 해설")
                .number(1)
                .lectureYoutubeUrl("https://www.youtube.com/watch?v=abc123XYZ09")
                .lectureStartSecond(430)
                .exam(exam)
                .build());

        List<Choice> choices = List.of(
                Choice.builder().id(1L).content("disappointing").number(1).isAnswer(true).problem(problem).build(),
                Choice.builder().id(2L).content("disappointment").number(2).isAnswer(false).problem(problem).build(),
                Choice.builder().id(3L).content("disappoint").number(3).isAnswer(false).problem(problem).build(),
                Choice.builder().id(4L).content("disappointedly").number(4).isAnswer(false).problem(problem).build()
        );
        choiceRepository.saveAll(choices);

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/problem/{problemId}", problem.getId()));

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("getProblemById",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(problemResource(
                                "V1 문제 상세 조회",
                                "레거시 HTML 기반 문제 상세 조회 응답입니다. lecture 객체를 포함해 프론트가 해설강의 버튼을 바로 구성할 수 있어야 합니다."
                        )),
                        pathParameters(
                                parameterWithName("problemId").description("조회할 문제 고유 식별자")
                        ),

                        responseFields(
                                fieldWithPath("data.id").description("문제 고유 식별자").type(JsonFieldType.NUMBER).optional(),
                                fieldWithPath("data.content").description("문제 내용").type(JsonFieldType.STRING),
                                fieldWithPath("data.number").description("문제 번호").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.explanation").description("문제 해설").type(JsonFieldType.STRING),
                                fieldWithPath("data.lecture").description("문제에 연결된 해설강의 정보").type(JsonFieldType.OBJECT),
                                fieldWithPath("data.lecture.youtubeUrl").description("canonical 유튜브 링크").type(JsonFieldType.STRING),
                                fieldWithPath("data.lecture.startTimeSecond").description("해설 재생 시작 시각(초)").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.lecture.playbackUrl").description("프론트가 바로 재생에 사용할 링크").type(JsonFieldType.STRING),
                                fieldWithPath("data.choices").description("선택지 목록").type(JsonFieldType.ARRAY),
                                fieldWithPath("data.choices[].id").description("선택지 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.choices[].number").description("선택지 번호").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.choices[].content").description("선택지 내용").type(JsonFieldType.STRING),
                                fieldWithPath("data.choices[].isAnswer").description("정답 여부").type(JsonFieldType.BOOLEAN)
                        )
                ));


    }

    @Transactional
    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getAllProblemsByExamId() throws Exception
    {
        // given
        String content = "The walking tour was a big ___ to some people";
        Problem problem = problemRepository.save(Problem.builder()
                .id(1L)
                .content(content)
                .number(1)
                .explanation("설명")
                .lectureYoutubeUrl("https://www.youtube.com/watch?v=abc123XYZ09")
                .lectureStartSecond(430)
                .exam(exam)
                .build());

        List<Choice> choices = List.of(
                Choice.builder().id(1L).content("disappointing").number(1).isAnswer(true)
                       .problem(problem).build(),
                Choice.builder().id(2L).content("disappointment").number(2).isAnswer(false)
                       .problem(problem).build(),
                Choice.builder().id(3L).content("disappoint").number(3).isAnswer(false)
                       .problem(problem).build(),
                Choice.builder().id(4L).content("disappointedly").number(4).isAnswer(false)
                        .problem(problem).build()
        );

        choiceRepository.saveAll(choices);

        content = "He recently ___ a tour of the company's main facility";
        Problem problem2 = problemRepository.save(Problem.builder()
                .id(2L)
                .content(content)
                .number(2)
                .explanation("문제 해설2")
                .lectureYoutubeUrl("https://www.youtube.com/watch?v=xyz987LMN12")
                .lectureStartSecond(35)
                .exam(exam)
                .build());

        List<Choice> choices2 = List.of(
                Choice.builder().id(5L).content("conducted").number(1).isAnswer(true)
                        .problem(problem2).build(),
                Choice.builder().id(6L).content("conduct").number(2).isAnswer(false)
                        .problem(problem2).build(),
                Choice.builder().id(7L).content("to conduct").number(3).isAnswer(false)
                        .problem(problem2).build(),
                Choice.builder().id(8L).content("conductor").number(4).isAnswer(false)
                        .problem(problem2).build()
        );

        choiceRepository.saveAll(choices2);


        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/problem")
                        .queryParam("examId", exam.getId().toString())
                );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("getAllProblemsByExamId",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(problemResource(
                                "V1 시험별 문제 목록 조회",
                                "레거시 HTML 기반 문제 목록 조회 응답입니다. 각 문제의 lecture 정보를 포함합니다."
                        )),
                        queryParameters(
                                parameterWithName("examId").description("문제가 속한 시험 고유 식별자")
                        ),

                        responseFields(
                                fieldWithPath("data[].id").description("문제 고유 식별자").type(JsonFieldType.NUMBER).optional(),
                                fieldWithPath("data[].content").description("문제 내용").type(JsonFieldType.STRING),
                                fieldWithPath("data[].number").description("문제 번호").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].explanation").description("문제 해설").type(JsonFieldType.STRING),
                                fieldWithPath("data[].lecture").description("문제에 연결된 해설강의 정보").type(JsonFieldType.OBJECT),
                                fieldWithPath("data[].lecture.youtubeUrl").description("canonical 유튜브 링크").type(JsonFieldType.STRING),
                                fieldWithPath("data[].lecture.startTimeSecond").description("해설 재생 시작 시각(초)").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].lecture.playbackUrl").description("프론트가 바로 재생에 사용할 링크").type(JsonFieldType.STRING),
                                fieldWithPath("data[].choices").description("선택지 목록").type(JsonFieldType.ARRAY),
                                fieldWithPath("data[].choices[].id").description("선택지 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].choices[].number").description("선택지 번호").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].choices[].content").description("선택지 내용").type(JsonFieldType.STRING),
                                fieldWithPath("data[].choices[].isAnswer").description("정답 여부").type(JsonFieldType.BOOLEAN)
                        )
                ));


    }

    @Transactional
    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getAllProblemsByExamIdV2() throws Exception
    {
        Problem problem = problemRepository.save(Problem.builder()
                .id(11L)
                .number(1)
                .contentJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanationJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .lectureYoutubeUrl("https://www.youtube.com/watch?v=abc123XYZ09")
                .lectureStartSecond(430)
                .exam(exam)
                .build());

        choiceRepository.saveAll(List.of(
                Choice.builder().id(21L).content("A").number(1).isAnswer(true).problem(problem).build(),
                Choice.builder().id(22L).content("B").number(2).isAnswer(false).problem(problem).build()
        ));

        ResultActions resultActions = mvc.perform(get("/api/v2/problem")
                .queryParam("examId", exam.getId().toString()));

        resultActions
                .andExpect(status().isOk())
                .andDo(document("getAllProblemsByExamIdV2",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(problemResource(
                                "V2 사용자 시험별 문제 목록 조회",
                                "Block 기반 사용자 문제 목록 조회 응답입니다. lecture.playbackUrl 을 그대로 사용해 유튜브 딥링크를 열 수 있습니다."
                        )),
                        queryParameters(
                                parameterWithName("examId").description("시험 고유 식별자")
                        ),
                        responseFields(
                                fieldWithPath("data[].id").description("문제 고유 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].number").description("문제 번호").type(JsonFieldType.NUMBER),
                                subsectionWithPath("data[].content").description("Block 기반 문제 본문"),
                                subsectionWithPath("data[].explanation").description("Block 기반 문제 해설"),
                                fieldWithPath("data[].lecture").description("문제에 연결된 해설강의 정보").type(JsonFieldType.OBJECT),
                                fieldWithPath("data[].lecture.youtubeUrl").description("canonical 유튜브 링크").type(JsonFieldType.STRING),
                                fieldWithPath("data[].lecture.startTimeSecond").description("해설 재생 시작 시각(초)").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].lecture.playbackUrl").description("프론트가 바로 재생에 사용할 링크").type(JsonFieldType.STRING),
                                fieldWithPath("data[].choices").description("선택지 목록").type(JsonFieldType.ARRAY),
                                fieldWithPath("data[].choices[].id").description("선택지 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].choices[].number").description("선택지 번호").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].choices[].content").description("선택지 내용").type(JsonFieldType.STRING),
                                fieldWithPath("data[].choices[].isAnswer").description("정답 여부").type(JsonFieldType.BOOLEAN)
                        )
                ));
    }

    @Transactional
    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getAdminProblemByIdV2() throws Exception
    {
        Problem problem = problemRepository.save(Problem.builder()
                .id(31L)
                .number(1)
                .contentJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanationJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .lectureYoutubeUrl("https://www.youtube.com/watch?v=abc123XYZ09")
                .lectureStartSecond(430)
                .exam(exam)
                .build());

        choiceRepository.saveAll(List.of(
                Choice.builder().id(41L).content("A").number(1).isAnswer(true).problem(problem).build(),
                Choice.builder().id(42L).content("B").number(2).isAnswer(false).problem(problem).build()
        ));

        ResultActions resultActions = mvc.perform(get("/api/v2/admin/problem/{problemId}", problem.getId()));

        resultActions
                .andExpect(status().isOk())
                .andDo(document("getAdminProblemByIdV2",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(problemResource(
                                "V2 관리자 문제 상세 조회",
                                "관리자 문제 편집 화면이 초기 로딩 시 사용하는 상세 조회 응답입니다."
                        )),
                        pathParameters(
                                parameterWithName("problemId").description("조회할 문제 고유 식별자")
                        ),
                        responseFields(
                                fieldWithPath("data.id").description("문제 고유 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.number").description("문제 번호").type(JsonFieldType.NUMBER),
                                subsectionWithPath("data.content").description("Block 기반 문제 본문"),
                                subsectionWithPath("data.explanation").description("Block 기반 문제 해설"),
                                fieldWithPath("data.lecture").description("문제에 연결된 해설강의 정보").type(JsonFieldType.OBJECT),
                                fieldWithPath("data.lecture.youtubeUrl").description("canonical 유튜브 링크").type(JsonFieldType.STRING),
                                fieldWithPath("data.lecture.startTimeSecond").description("해설 재생 시작 시각(초)").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.lecture.playbackUrl").description("프론트가 바로 재생에 사용할 링크").type(JsonFieldType.STRING),
                                fieldWithPath("data.choices").description("선택지 목록").type(JsonFieldType.ARRAY),
                                fieldWithPath("data.choices[].id").description("선택지 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.choices[].number").description("선택지 번호").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.choices[].content").description("선택지 내용").type(JsonFieldType.STRING),
                                fieldWithPath("data.choices[].isAnswer").description("정답 여부").type(JsonFieldType.BOOLEAN)
                        )
                ));
    }

    @Transactional
    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void getAdminProblemsByExamIdV2() throws Exception
    {
        Problem problem = problemRepository.save(Problem.builder()
                .id(51L)
                .number(1)
                .contentJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanationJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .lectureYoutubeUrl("https://www.youtube.com/watch?v=abc123XYZ09")
                .lectureStartSecond(430)
                .exam(exam)
                .build());

        choiceRepository.saveAll(List.of(
                Choice.builder().id(61L).content("A").number(1).isAnswer(true).problem(problem).build(),
                Choice.builder().id(62L).content("B").number(2).isAnswer(false).problem(problem).build()
        ));

        ResultActions resultActions = mvc.perform(get("/api/v2/admin/problem")
                .queryParam("examId", exam.getId().toString()));

        resultActions
                .andExpect(status().isOk())
                .andDo(document("getAdminProblemsByExamIdV2",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(problemResource(
                                "V2 관리자 시험별 문제 목록 조회",
                                "관리자 문제 목록 화면이 사용하는 Block 기반 문제 목록 조회 응답입니다."
                        )),
                        queryParameters(
                                parameterWithName("examId").description("시험 고유 식별자")
                        ),
                        responseFields(
                                fieldWithPath("data[].id").description("문제 고유 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].number").description("문제 번호").type(JsonFieldType.NUMBER),
                                subsectionWithPath("data[].content").description("Block 기반 문제 본문"),
                                subsectionWithPath("data[].explanation").description("Block 기반 문제 해설"),
                                fieldWithPath("data[].lecture").description("문제에 연결된 해설강의 정보").type(JsonFieldType.OBJECT),
                                fieldWithPath("data[].lecture.youtubeUrl").description("canonical 유튜브 링크").type(JsonFieldType.STRING),
                                fieldWithPath("data[].lecture.startTimeSecond").description("해설 재생 시작 시각(초)").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].lecture.playbackUrl").description("프론트가 바로 재생에 사용할 링크").type(JsonFieldType.STRING),
                                fieldWithPath("data[].choices").description("선택지 목록").type(JsonFieldType.ARRAY),
                                fieldWithPath("data[].choices[].id").description("선택지 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].choices[].number").description("선택지 번호").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].choices[].content").description("선택지 내용").type(JsonFieldType.STRING),
                                fieldWithPath("data[].choices[].isAnswer").description("정답 여부").type(JsonFieldType.BOOLEAN)
                        )
                ));
    }

    @Transactional
    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void searchAdminProblemsV2() throws Exception
    {
        Subject tax = subjectRepository.save(Subject.builder()
                .name("세법")
                .build());
        Exam taxExam = examRepository.save(Exam.builder()
                .year(2026)
                .name("2차")
                .subjectId(tax.getId())
                .status(ExamStatus.PUBLISHED)
                .build());

        Problem withoutLecture = problemRepository.save(Problem.builder()
                .number(7)
                .contentJson(List.of(TextBlock.builder()
                        .type("text")
                        .tag("p")
                        .spans(List.of(Span.builder().text("강의가 아직 연결되지 않은 세법 문제입니다").build()))
                        .build()))
                .explanationJson(List.of())
                .lectureYoutubeUrl("   ")
                .exam(taxExam)
                .build());

        choiceRepository.saveAll(List.of(
                Choice.builder().content("1번").number(1).isAnswer(true).problem(withoutLecture).build(),
                Choice.builder().content("2번").number(2).isAnswer(false).problem(withoutLecture).build(),
                Choice.builder().content("3번").number(3).isAnswer(false).problem(withoutLecture).build()
        ));

        Problem withoutLectureLegacy = problemRepository.save(Problem.builder()
                .number(2)
                .content("<p>영어 미연결 문제</p>")
                .explanation("해설")
                .exam(exam)
                .build());

        Problem withLecture = problemRepository.save(Problem.builder()
                .number(8)
                .content("강의 연결 문제")
                .explanation("해설")
                .lectureYoutubeUrl("https://www.youtube.com/watch?v=lecture-1")
                .lectureStartSecond(120)
                .exam(taxExam)
                .build());

        choiceRepository.saveAll(List.of(
                Choice.builder().content("A").number(1).isAnswer(true).problem(withLecture).build(),
                Choice.builder().content("B").number(2).isAnswer(false).problem(withLecture).build()
        ));

        Subject deletedSubject = subjectRepository.save(Subject.builder()
                .name("삭제 과목")
                .build());
        Exam deletedExam = examRepository.save(Exam.builder()
                .year(2027)
                .name("삭제 시험")
                .subjectId(deletedSubject.getId())
                .status(ExamStatus.PUBLISHED)
                .build());
        problemRepository.save(Problem.builder()
                .number(99)
                .content("삭제 계층 문제")
                .explanation("해설")
                .exam(deletedExam)
                .build());
        deletedSubject.delete(1L);
        subjectRepository.save(deletedSubject);

        ResultActions resultActions = mvc.perform(get("/api/v2/admin/problem/search")
                .queryParam("page", "0")
                .queryParam("size", "20")
                .queryParam("lectureStatus", "WITHOUT_LECTURE"));

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(withoutLecture.getId()))
                .andExpect(jsonPath("$.data[0].subjectId").value(tax.getId()))
                .andExpect(jsonPath("$.data[0].subjectName").value("세법"))
                .andExpect(jsonPath("$.data[0].examId").value(taxExam.getId()))
                .andExpect(jsonPath("$.data[0].examName").value("2차"))
                .andExpect(jsonPath("$.data[0].examYear").value(2026))
                .andExpect(jsonPath("$.data[0].lecture").value(nullValue()))
                .andExpect(jsonPath("$.data[0].choiceCount").value(3))
                .andExpect(jsonPath("$.data[0].answerChoiceCount").value(1))
                .andExpect(jsonPath("$.data[0].contentPreviewText").value("강의가 아직 연결되지 않은 세법 문제입니다"))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(2))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(1))
                .andExpect(jsonPath("$.pageInfo.currentPage").value(1))
                .andExpect(jsonPath("$.pageInfo.pageSize").value(20))
                .andDo(document("searchAdminProblemsV2",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(problemResource(
                                "V2 관리자 문제 검색",
                                "대시보드 카드 클릭 진입을 위해 active hierarchy 기준으로 관리자 문제 목록을 검색합니다."
                        )),
                        queryParameters(
                                parameterWithName("page").description("0-based 페이지 번호").optional(),
                                parameterWithName("size").description("페이지 크기").optional(),
                                parameterWithName("lectureStatus").description("해설 강의 연결 상태. ALL, WITH_LECTURE, WITHOUT_LECTURE"),
                                parameterWithName("subjectId").description("과목 ID 필터").optional(),
                                parameterWithName("year").description("시험 연도 필터").optional(),
                                parameterWithName("examId").description("시험 ID 필터").optional()
                        ),
                        responseFields(
                                fieldWithPath("data[].id").description("문제 고유 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].number").description("문제 번호").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].subjectId").description("과목 ID").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].subjectName").description("과목명").type(JsonFieldType.STRING),
                                fieldWithPath("data[].examId").description("시험 ID").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].examName").description("시험명").type(JsonFieldType.STRING),
                                fieldWithPath("data[].examYear").description("시험 연도").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].lecture").description("해설 강의 정보. WITHOUT_LECTURE 조회에서는 null").type(JsonFieldType.NULL).optional(),
                                fieldWithPath("data[].choiceCount").description("선택지 수").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].answerChoiceCount").description("정답 선택지 수").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].contentPreviewText").description("짧은 문제 미리보기 텍스트").type(JsonFieldType.STRING),
                                fieldWithPath("pageInfo.totalElements").description("전체 건수").type(JsonFieldType.NUMBER),
                                fieldWithPath("pageInfo.totalPages").description("전체 페이지 수").type(JsonFieldType.NUMBER),
                                fieldWithPath("pageInfo.currentPage").description("현재 페이지(1-based)").type(JsonFieldType.NUMBER),
                                fieldWithPath("pageInfo.pageSize").description("페이지 크기").type(JsonFieldType.NUMBER)
                        )
                ));

        mvc.perform(get("/api/v2/admin/problem/search")
                        .queryParam("page", "0")
                        .queryParam("size", "20")
                        .queryParam("lectureStatus", "WITHOUT_LECTURE")
                        .queryParam("subjectId", tax.getId().toString())
                        .queryParam("year", "2026")
                        .queryParam("examId", taxExam.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(withoutLecture.getId()))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(1));

        mvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.operations.problemsWithoutLectureCount").value(2));
    }

    @Transactional
    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("과거 soft delete 데이터가 같은 번호를 점유해도 새 문제를 등록할 수 있다")
    void saveOrUpdateProblemV2_whenLegacyDeletedProblemHasSameExamNumber_thenRecreateSucceeds() throws Exception
    {
        Problem legacyDeletedProblem = problemRepository.save(Problem.builder()
                .number(4)
                .contentJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanationJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .exam(exam)
                .build());
        legacyDeletedProblem.delete();
        problemRepository.save(legacyDeletedProblem);

        ProblemSaveV2Request request = ProblemSaveV2Request.builder()
                .number(4)
                .content(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanation(List.of(TextBlock.builder().type("text").tag("p").build()))
                .choices(List.of())
                .build();

        mvc.perform(post("/api/v2/admin/problem")
                        .queryParam("examId", exam.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        List<Problem> examProblems = problemRepository.findAll().stream()
                .filter(problem -> problem.getExam().getId().equals(exam.getId()))
                .toList();

        assertThat(examProblems)
                .anyMatch(problem -> problem.isRemoved() && problem.getNumber() < 0);
        assertThat(examProblems)
                .anyMatch(problem -> !problem.isRemoved() && problem.getNumber() == 4);
    }

    @Transactional
    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("삭제된 문제와 같은 시험 번호로 새 문제를 다시 등록할 수 있다")
    void saveOrUpdateProblemV2_whenDeletedProblemHasSameExamNumber_thenRecreateSucceeds() throws Exception
    {
        Problem deletedProblem = problemRepository.save(Problem.builder()
                .number(3)
                .contentJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanationJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .exam(exam)
                .build());

        mvc.perform(delete("/api/admin/problem/{problemId}", deletedProblem.getId()))
                .andExpect(status().isNoContent());

        ProblemSaveV2Request request = ProblemSaveV2Request.builder()
                .number(3)
                .content(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanation(List.of(TextBlock.builder().type("text").tag("p").build()))
                .choices(List.of())
                .build();

        mvc.perform(post("/api/v2/admin/problem")
                        .queryParam("examId", exam.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        List<Problem> examProblems = problemRepository.findAll().stream()
                .filter(problem -> problem.getExam().getId().equals(exam.getId()))
                .toList();

        assertThat(examProblems).hasSize(2);
        assertThat(examProblems)
                .anyMatch(problem -> problem.isRemoved() && problem.getNumber() < 0);
        assertThat(examProblems)
                .anyMatch(problem -> !problem.isRemoved() && problem.getNumber() == 3);
    }

    @Transactional
    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void saveOrUpdateProblemV2_withLecture() throws Exception
    {
        ProblemSaveV2Request request = ProblemSaveV2Request.builder()
                .number(3)
                .content(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanation(List.of(TextBlock.builder().type("text").tag("p").build()))
                .lecture(ProblemLectureRequest.builder()
                        .youtubeUrl("https://youtu.be/abc123XYZ09?t=430")
                        .startTimeSecond(430)
                        .build())
                .choices(List.of())
                .build();

        ResultActions resultActions = mvc.perform(post("/api/v2/admin/problem")
                .queryParam("examId", exam.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));

        resultActions
                .andExpect(status().isOk())
                .andDo(document("saveOrUpdateProblemV2",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(problemResource(
                                "V2 관리자 문제 생성/수정",
                                "관리자 문제 편집 화면이 사용하는 저장 API입니다. lecture=null 이면 기존 강의 링크를 제거합니다."
                        )),
                        queryParameters(
                                parameterWithName("examId").description("문제를 저장할 시험 고유 식별자")
                        ),
                        requestFields(
                                fieldWithPath("id").description("문제 ID. 생성 시 생략, 수정 시 포함").type(JsonFieldType.NUMBER).optional(),
                                fieldWithPath("number").description("문제 번호").type(JsonFieldType.NUMBER),
                                subsectionWithPath("content").description("Block 기반 문제 본문"),
                                subsectionWithPath("explanation").description("Block 기반 문제 해설"),
                                fieldWithPath("lecture").description("문제 해설강의 정보. null 이면 기존 링크 제거").type(JsonFieldType.OBJECT),
                                fieldWithPath("lecture.youtubeUrl").description("관리자 화면에서 입력한 유튜브 링크").type(JsonFieldType.STRING),
                                fieldWithPath("lecture.startTimeSecond").description("해설 시작 시각(초). null 허용").type(JsonFieldType.NUMBER),
                                fieldWithPath("choices").description("보기 목록. 빈 배열 허용").type(JsonFieldType.ARRAY)
                        ),
                        responseFields(
                                fieldWithPath("data").description("저장 완료 메시지").type(JsonFieldType.STRING)
                        )
                ));
    }

    private ResourceSnippetParameters problemResource(String summary, String description) {
        return ResourceSnippetParameters.builder()
                .tag("Problem")
                .summary(summary)
                .description(description)
                .build();
    }
}
