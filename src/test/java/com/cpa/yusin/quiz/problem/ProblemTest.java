package com.cpa.yusin.quiz.problem;

import com.cpa.yusin.quiz.choice.controller.dto.request.ChoiceRequest;
import com.cpa.yusin.quiz.choice.domain.Choice;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import com.cpa.yusin.quiz.config.TeardownExtension;
import com.cpa.yusin.quiz.exam.domain.Exam;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.member.domain.Member;
import com.cpa.yusin.quiz.member.domain.type.Platform;
import com.cpa.yusin.quiz.member.domain.type.Role;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.problem.controller.dto.request.ProblemRequest;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
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
                .maxProblemCount(40)
                .subjectId(english.getId())
                .build());
    }

    @WithUserDetails(value = "John@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    void saveUpdate_success() throws Exception
    {
        // given
        String content1
                = "If ____ of the items on your order are out of stock, you will be notified by email and the rest of your order will be delivered";
        String content2 = "A small lever on the underside of TEAM 7's coffee table allows you to adjust ____ height smoothly";

        Long examId = exam.getId();

        List<ChoiceRequest> choiceRequests1 = List.of(
                ChoiceRequest.builder().id(null).number(1).content("some").isAnswer(true).isDeleted(false).build(),
                ChoiceRequest.builder().id(null).number(2).content("any").isAnswer(false).isDeleted(false).build(),
                ChoiceRequest.builder().id(null).number(3).content("each").isAnswer(false).isDeleted(false).build(),
                ChoiceRequest.builder().id(null).number(4).content("every").isAnswer(false).isDeleted(false).build()
        );

        List<ChoiceRequest> choiceRequests2 = List.of(
                ChoiceRequest.builder().id(null).number(1).content("those").isAnswer(false).isDeleted(false).build(),
                ChoiceRequest.builder().id(null).number(2).content("which").isAnswer(false).isDeleted(false).build(),
                ChoiceRequest.builder().id(null).number(3).content("tis").isAnswer(true).isDeleted(false).build(),
                ChoiceRequest.builder().id(null).number(4).content("theirs").isAnswer(false).isDeleted(false).build()
        );

        List<ProblemRequest> request = List.of(
                ProblemRequest.builder().id(null).number(1).choices(choiceRequests1).content(content1).deleted(false).build(),
                ProblemRequest.builder().id(null).number(2).choices(choiceRequests2).content(content2).deleted(false).build()
        );

        // when
        ResultActions resultActions = mvc
                .perform(post("/api/v1/admin/problem")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("examId", examId.toString())
                );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("problemSaveUpdateSuccess",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("examId").description("시험 고유 식별자")
                        ),

                        requestFields(
                                fieldWithPath("[].id").description("문제 고유 식별자 (신규 생성시 null)").optional(),
                                fieldWithPath("[].content").description("문제 내용"),
                                fieldWithPath("[].number").description("문제 번호"),
                                fieldWithPath("[].deleted").description("삭제 여부"),
                                fieldWithPath("[].choices").description("선택지 목록"),
                                fieldWithPath("[].choices[].id").description("선택지 고유 식별자 (신규 생성시 null)").optional(),
                                fieldWithPath("[].choices[].number").description("선택지 번호"),
                                fieldWithPath("[].choices[].content").description("선택지 내용"),
                                fieldWithPath("[].choices[].isAnswer").description("정답 여부"),
                                fieldWithPath("[].choices[].isDeleted").description("선택지 삭제 여부")
                        ),

                        responseFields(
                                fieldWithPath("data[].id").description("문제 고유 식별자").type(JsonFieldType.NUMBER).optional(),
                                fieldWithPath("data[].content").description("문제 내용").type(JsonFieldType.STRING),
                                fieldWithPath("data[].number").description("문제 번호").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].choices").description("선택지 목록").type(JsonFieldType.ARRAY),
                                fieldWithPath("data[].choices[].id").description("선택지 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].choices[].number").description("선택지 번호").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].choices[].content").description("선택지 내용").type(JsonFieldType.STRING),
                                fieldWithPath("data[].choices[].isAnswer").description("정답 여부").type(JsonFieldType.BOOLEAN)
                        )
                ))
        ;

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
                .number(1)
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
                .perform(get("/api/v1/admin/problem/" + problem.getId()));

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("getProblemById",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        responseFields(
                                fieldWithPath("data.id").description("문제 고유 식별자").type(JsonFieldType.NUMBER).optional(),
                                fieldWithPath("data.content").description("문제 내용").type(JsonFieldType.STRING),
                                fieldWithPath("data.number").description("문제 번호").type(JsonFieldType.NUMBER),
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
                .perform(get("/api/v1/admin/problem")
                        .queryParam("examId", exam.getId().toString())
                );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(document("getAllProblemsByExamId",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),

                        responseFields(
                                fieldWithPath("data[].id").description("문제 고유 식별자").type(JsonFieldType.NUMBER).optional(),
                                fieldWithPath("data[].content").description("문제 내용").type(JsonFieldType.STRING),
                                fieldWithPath("data[].number").description("문제 번호").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].choices").description("선택지 목록").type(JsonFieldType.ARRAY),
                                fieldWithPath("data[].choices[].id").description("선택지 식별자").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].choices[].number").description("선택지 번호").type(JsonFieldType.NUMBER),
                                fieldWithPath("data[].choices[].content").description("선택지 내용").type(JsonFieldType.STRING),
                                fieldWithPath("data[].choices[].isAnswer").description("정답 여부").type(JsonFieldType.BOOLEAN)
                        )
                ));


    }

}
