package com.cpa.yusin.quiz.problem.integration;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemLectureRequest;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemSaveV2Request;
import com.cpa.yusin.quiz.problem.domain.block.TextBlock;
import com.cpa.yusin.quiz.problem.domain.Problem;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import com.cpa.yusin.quiz.subject.domain.Subject;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
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
                .build());
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
