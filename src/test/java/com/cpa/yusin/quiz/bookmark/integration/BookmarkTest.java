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
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
        Problem problem = problemRepository.save(Problem.builder()
                .id(1L)
                .number(1)
                .contentJson(java.util.List.of(TextBlock.builder().type("text").tag("p").build()))
                .explanationJson(java.util.List.of(TextBlock.builder().type("text").tag("p").build()))
                .lectureYoutubeUrl("https://www.youtube.com/watch?v=abc123XYZ09")
                .lectureStartSecond(430)
                .exam(exam)
                .build());

        choiceRepository.saveAll(java.util.List.of(
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
}
