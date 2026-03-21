package com.cpa.yusin.quiz.bookmark.integration;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.cpa.yusin.quiz.bookmark.domain.Bookmark;
import com.cpa.yusin.quiz.bookmark.service.port.BookmarkRepository;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({ RestDocumentationExtension.class, TeardownExtension.class })
@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureRestDocs
class BookmarkTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private ChoiceRepository choiceRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    private Member member;
    private MemberDetails memberDetails;
    private Exam exam;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .apply(springSecurity())
                .build();

        Subject subject = subjectRepository.save(Subject.builder()
                .id(1L)
                .name("영어")
                .build());

        exam = examRepository.save(Exam.builder()
                .id(1L)
                .name("1차")
                .year(2024)
                .subjectId(subject.getId())
                .status(ExamStatus.PUBLISHED)
                .build());

        member = memberRepository.save(Member.builder()
                .email("bookmark-user@test.com")
                .password("encoded-pass")
                .username("bookmark-user")
                .platform(Platform.HOME)
                .role(Role.USER)
                .build());
        memberDetails = new MemberDetails(member, null);
    }

    @Test
    void getBookmarkedProblems() throws Exception {
        Problem problem = createProblem(1L, 1);
        problem.assignLecture("https://www.youtube.com/watch?v=abc123XYZ09", 430);
        problemRepository.save(problem);

        choiceRepository.saveAll(List.of(
                Choice.builder().id(1L).content("보기 1").number(1).isAnswer(true).problem(problem).build(),
                Choice.builder().id(2L).content("보기 2").number(2).isAnswer(false).problem(problem).build()
        ));

        bookmarkRepository.save(Bookmark.create(member, problem));

        ResultActions resultActions = mvc.perform(get("/api/v1/bookmarks/problems")
                .with(user(memberDetails))
                .queryParam("page", "0")
                .queryParam("size", "20"));

        resultActions
                .andExpect(status().isOk())
                .andDo(document("getBookmarkedProblems",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Bookmark")
                                .summary("북마크된 문제 Slice 조회")
                                .description("사용자 북마크 화면이 사용하는 문제 목록 응답입니다. ProblemV2Response 기반이며 lecture 정보를 포함합니다.")
                                .build()),
                        queryParameters(
                                parameterWithName("subjectId").description("과목 ID. 생략하면 전체 북마크 조회").optional(),
                                parameterWithName("page").description("0부터 시작하는 페이지 번호"),
                                parameterWithName("size").description("한 페이지 크기")
                        ),
                        responseFields(
                                fieldWithPath("data.content").description("북마크된 문제 목록").type(JsonFieldType.ARRAY),
                                fieldWithPath("data.content[].id").description("문제 고유 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.content[].number").description("문제 번호").type(JsonFieldType.NUMBER),
                                subsectionWithPath("data.content[].content").description("Block 기반 문제 본문"),
                                subsectionWithPath("data.content[].explanation").description("Block 기반 문제 해설"),
                                fieldWithPath("data.content[].lecture").description("문제에 연결된 해설강의 정보").type(JsonFieldType.OBJECT),
                                fieldWithPath("data.content[].lecture.youtubeUrl").description("canonical 유튜브 링크").type(JsonFieldType.STRING),
                                fieldWithPath("data.content[].lecture.startTimeSecond").description("해설 재생 시작 시각(초)").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.content[].lecture.playbackUrl").description("프론트가 바로 재생에 사용할 링크").type(JsonFieldType.STRING),
                                fieldWithPath("data.content[].choices").description("선택지 목록").type(JsonFieldType.ARRAY),
                                fieldWithPath("data.content[].choices[].id").description("선택지 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.content[].choices[].number").description("선택지 번호").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.content[].choices[].content").description("선택지 내용").type(JsonFieldType.STRING),
                                fieldWithPath("data.content[].choices[].isAnswer").description("정답 여부").type(JsonFieldType.BOOLEAN),
                                fieldWithPath("data.currentPage").description("현재 페이지 번호").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.size").description("페이지 크기").type(JsonFieldType.NUMBER),
                                fieldWithPath("data.hasNext").description("다음 페이지 존재 여부").type(JsonFieldType.BOOLEAN)
                        )
                ));
    }

    @Test
    void getBookmarkStatus() throws Exception {
        Problem problem1 = createProblem(1L, 1);
        Problem problem2 = createProblem(2L, 2);
        bookmarkRepository.save(Bookmark.create(member, problem1));
        bookmarkRepository.save(Bookmark.create(member, problem2));

        ResultActions resultActions = mvc.perform(post("/api/v1/bookmarks/status")
                .with(user(memberDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "problemIds": [2, 999, 2, 1]
                        }
                        """));

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookmarkedIds.length()").value(2))
                .andExpect(jsonPath("$.data.bookmarkedIds[0]").value(2))
                .andExpect(jsonPath("$.data.bookmarkedIds[1]").value(1))
                .andDo(document("postBookmarkStatus",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        resource(ResourceSnippetParameters.builder()
                                .tag("Bookmark")
                                .summary("북마크 상태 조회")
                                .description("현재 로그인 사용자가 전달한 problemIds 중 북마크한 문제 ID만 순서 보존 규칙에 따라 반환합니다.")
                                .build()),
                        requestFields(
                                fieldWithPath("problemIds").description("상태 확인 대상 문제 ID 목록. 빈 배열 허용, 최대 500개이며 각 원소는 양의 정수")
                        ),
                        responseFields(
                                fieldWithPath("data.bookmarkedIds").description("현재 로그인 사용자가 북마크한 문제 ID 목록").type(JsonFieldType.ARRAY)
                        )
                ));
    }

    @Test
    void getBookmarkStatus_emptyRequest_returnsEmptyResponse() throws Exception {
        mvc.perform(post("/api/v1/bookmarks/status")
                        .with(user(memberDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "problemIds": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookmarkedIds").isEmpty());
    }

    @Test
    void getBookmarkStatus_duplicateInput_preservesOrder() throws Exception {
        Problem problem1 = createProblem(1L, 1);
        Problem problem2 = createProblem(2L, 2);
        bookmarkRepository.save(Bookmark.create(member, problem1));
        bookmarkRepository.save(Bookmark.create(member, problem2));

        mvc.perform(post("/api/v1/bookmarks/status")
                        .with(user(memberDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "problemIds": [2, 1, 2, 1]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookmarkedIds.length()").value(2))
                .andExpect(jsonPath("$.data.bookmarkedIds[0]").value(2))
                .andExpect(jsonPath("$.data.bookmarkedIds[1]").value(1));
    }

    @Test
    void getBookmarkStatus_missingProblemIds_returnsBadRequest() throws Exception {
        mvc.perform(post("/api/v1/bookmarks/status")
                        .with(user(memberDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.valueErrors[0].descriptor").value("problemIds"))
                .andExpect(jsonPath("$.valueErrors[0].reason").value("problemIds는 필수입니다"));
    }

    @Test
    void getBookmarkStatus_nullElement_returnsBadRequest() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/v1/bookmarks/status")
                .with(user(memberDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "problemIds": [1, null]
                        }
                        """));

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.valueErrors[0].descriptor").value("problemIds[1]"))
                .andExpect(jsonPath("$.valueErrors[0].reason").value("problemIds에는 null을 포함할 수 없습니다"))
                .andDo(document("postBookmarkStatus-invalid",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("status").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                fieldWithPath("message").description("오류 요약 메시지").type(JsonFieldType.STRING),
                                fieldWithPath("valueErrors").description("필드 단위 검증 오류 목록").type(JsonFieldType.ARRAY),
                                fieldWithPath("valueErrors[].descriptor").description("오류가 발생한 필드 경로").type(JsonFieldType.STRING),
                                fieldWithPath("valueErrors[].rejectedValue").description("거부된 값").type(JsonFieldType.STRING),
                                fieldWithPath("valueErrors[].reason").description("검증 실패 사유").type(JsonFieldType.STRING)
                        )
                ));
    }

    @Test
    void getBookmarkStatus_nonPositiveIds_returnsBadRequest() throws Exception {
        mvc.perform(post("/api/v1/bookmarks/status")
                        .with(user(memberDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "problemIds": [101, -2, 0]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.valueErrors[0].reason").value("problemIds는 양의 정수만 허용합니다"));
    }

    @Test
    void getBookmarkStatus_tooManyIds_returnsBadRequest() throws Exception {
        String problemIds = IntStream.rangeClosed(1, 501)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(", "));

        mvc.perform(post("/api/v1/bookmarks/status")
                        .with(user(memberDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "problemIds": [%s]
                                }
                                """.formatted(problemIds)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.valueErrors[0].descriptor").value("problemIds"))
                .andExpect(jsonPath("$.valueErrors[0].reason").value("problemIds는 최대 500개까지 허용합니다"));
    }

    @Test
    void getBookmarkStatus_unauthenticated_returnsUnauthorized() throws Exception {
        ResultActions resultActions = mvc.perform(post("/api/v1/bookmarks/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "problemIds": [1, 2]
                        }
                        """));

        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."))
                .andExpect(jsonPath("$.path").value("/api/v1/bookmarks/status"))
                .andDo(document("postBookmarkStatus-unauthorized",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("status").description("HTTP 상태 코드").type(JsonFieldType.NUMBER),
                                fieldWithPath("code").description("보안 오류 코드").type(JsonFieldType.STRING),
                                fieldWithPath("message").description("보안 오류 메시지").type(JsonFieldType.STRING),
                                fieldWithPath("path").description("요청 경로").type(JsonFieldType.STRING)
                        )
                ));
    }

    private Problem createProblem(Long problemId, int number) {
        return problemRepository.save(Problem.builder()
                .id(problemId)
                .number(number)
                .contentJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanationJson(List.of(TextBlock.builder().type("text").tag("p").build()))
                .exam(exam)
                .build());
    }
}
