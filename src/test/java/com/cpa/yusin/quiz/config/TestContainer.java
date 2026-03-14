package com.cpa.yusin.quiz.config;

import com.cpa.yusin.quiz.answer.controller.mapper.AnswerMapper;
import com.cpa.yusin.quiz.answer.controller.port.AnswerService;
import com.cpa.yusin.quiz.answer.service.AnswerServiceImpl;
import com.cpa.yusin.quiz.answer.service.port.AnswerRepository;
import com.cpa.yusin.quiz.bookmark.controller.port.CreateBookmarkService;
import com.cpa.yusin.quiz.bookmark.controller.port.DeleteBookmarkService;
import com.cpa.yusin.quiz.bookmark.controller.port.GetBookmarkedProblemsService;
import com.cpa.yusin.quiz.bookmark.service.CreateBookmarkServiceImpl;
import com.cpa.yusin.quiz.bookmark.service.DeleteBookmarkServiceImpl;
import com.cpa.yusin.quiz.bookmark.service.GetBookmarkedProblemsServiceImpl;
import com.cpa.yusin.quiz.bookmark.service.port.BookmarkRepository;
import com.cpa.yusin.quiz.choice.controller.mapper.ChoiceMapper;
import com.cpa.yusin.quiz.choice.controller.mapper.ChoiceMapperImpl;
import com.cpa.yusin.quiz.choice.controller.port.ChoiceService;
import com.cpa.yusin.quiz.choice.service.ChoiceServiceImpl;
import com.cpa.yusin.quiz.choice.service.port.ChoiceRepository;
import com.cpa.yusin.quiz.common.service.ClockHolder;
import com.cpa.yusin.quiz.common.service.MerchantIdGenerator;
import com.cpa.yusin.quiz.common.service.UuidHolder;
import com.cpa.yusin.quiz.exam.controller.ExamController;
import com.cpa.yusin.quiz.exam.controller.mapper.ExamMapper;
import com.cpa.yusin.quiz.exam.controller.port.ExamService;
import com.cpa.yusin.quiz.exam.infrastructure.ExamValidatorImpl;
import com.cpa.yusin.quiz.exam.service.ExamServiceImpl;
import com.cpa.yusin.quiz.exam.service.port.ExamRepository;
import com.cpa.yusin.quiz.exam.service.port.ExamValidator;
import com.cpa.yusin.quiz.file.controller.port.FileService;
import com.cpa.yusin.quiz.file.service.FileServiceImpl;
import com.cpa.yusin.quiz.global.details.MemberDetailsService;
import com.cpa.yusin.quiz.global.jwt.JwtService;
import com.cpa.yusin.quiz.global.jwt.JwtServiceImpl;
import com.cpa.yusin.quiz.global.security.CustomAuthenticationProvider;
import com.cpa.yusin.quiz.member.controller.mapper.MemberMapper;
import com.cpa.yusin.quiz.member.controller.port.AuthenticationService;
import com.cpa.yusin.quiz.member.controller.port.MemberService;
import com.cpa.yusin.quiz.member.infrastructure.MemberValidatorImpl;
import com.cpa.yusin.quiz.member.service.AuthenticationServiceImpl;
import com.cpa.yusin.quiz.member.service.MemberServiceImpl;
import com.cpa.yusin.quiz.member.service.RandomNicknameGenerator;
import com.cpa.yusin.quiz.member.service.port.MemberRepository;
import com.cpa.yusin.quiz.member.service.port.MemberValidator;
import com.cpa.yusin.quiz.mock.*;
import com.cpa.yusin.quiz.problem.controller.mapper.ProblemMapper;
import com.cpa.yusin.quiz.problem.controller.port.CreateProblemV2Service;
import com.cpa.yusin.quiz.problem.controller.port.GetProblemV2Service;
import com.cpa.yusin.quiz.problem.controller.port.ProblemService;
import com.cpa.yusin.quiz.problem.service.ProblemContentProcessor;
import com.cpa.yusin.quiz.problem.service.ProblemHtmlImageStorageService;
import com.cpa.yusin.quiz.problem.service.CreateProblemV2ServiceImpl;
import com.cpa.yusin.quiz.problem.service.GetProblemV2ServiceImpl;
import com.cpa.yusin.quiz.problem.service.ProblemServiceImpl;
import com.cpa.yusin.quiz.problem.service.ProblemValidator;
import com.cpa.yusin.quiz.problem.service.YoutubeLectureUrlProcessor;
import com.cpa.yusin.quiz.problem.service.port.ProblemRepository;
import com.cpa.yusin.quiz.question.controller.mapper.QuestionMapper;
import com.cpa.yusin.quiz.question.controller.port.QuestionService;
import com.cpa.yusin.quiz.question.service.QuestionAnswerService;
import com.cpa.yusin.quiz.question.service.QuestionServiceImpl;
import com.cpa.yusin.quiz.question.service.port.QuestionRepository;
import com.cpa.yusin.quiz.subject.controller.SubjectController;
import com.cpa.yusin.quiz.subject.controller.mapper.SubjectMapper;
import com.cpa.yusin.quiz.subject.controller.port.SubjectService;
import com.cpa.yusin.quiz.subject.infrastructure.SubjectValidatorImpl;
import com.cpa.yusin.quiz.subject.service.SubjectServiceImpl;
import com.cpa.yusin.quiz.subject.service.port.SubjectRepository;
import com.cpa.yusin.quiz.subject.service.port.SubjectValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TestContainer {
        private static final String FAKE_SECRET_KEY = "thisIsATestSecretKeyUsedOnlyForTesasdfeefsdfewdfesredfesfwqewdasdqrewtingPurposes";

        public final ClockHolder clockHolder;
        public final MerchantIdGenerator merchantIdGenerator;
        public final UuidHolder uuidHolder;

        public final MemberRepository memberRepository;
        public final MemberService memberService;
        public final MemberDetailsService memberDetailsService;
        public final CustomAuthenticationProvider authenticationProvider;
        public final PasswordEncoder passwordEncoder;
        public final JwtService jwtService;
        public final AuthenticationService authenticationService;
        public final MemberMapper memberMapper;
        public final MemberValidator memberValidator;
        public final SubjectRepository subjectRepository;
        public final SubjectValidator subjectValidator;
        public final SubjectService subjectService;
        public final SubjectMapper subjectMapper;
        public final SubjectController subjectController;

        /**
         * exam
         */
        public final ExamRepository examRepository;
        public final ExamMapper examMapper;
        public final ExamService examService;
        public final ExamValidator examValidator;
        public final ExamController examController;

        /**
         * choice
         */
        public final ChoiceMapper choiceMapper;
        public final ChoiceRepository choiceRepository;
        public final ChoiceService choiceService;

        /**
         * problem
         */
        public final ProblemMapper problemMapper;
        public final ProblemRepository problemRepository;
        public final ProblemService problemService;
        public final ProblemValidator problemValidator;
        public final CreateProblemV2Service createProblemV2Service;
        public final GetProblemV2Service getProblemV2Service;
        public final YoutubeLectureUrlProcessor youtubeLectureUrlProcessor;

        /**
         * question
         */
        public final QuestionRepository questionRepository;
        public final QuestionMapper questionMapper;
        public final QuestionService questionService;
        public final QuestionAnswerService questionAnswerService;

        /**
         * answer
         */
        public final AnswerRepository answerRepository;
        public final AnswerMapper answerMapper;
        public final AnswerService answerService;

        /**
         * bookmark
         */
        public final BookmarkRepository bookmarkRepository;
        public final CreateBookmarkService createBookmarkService;
        public final DeleteBookmarkService deleteBookmarkService;
        public final GetBookmarkedProblemsService getBookmarkedProblemsService;

        public final FileService fileService;

        public TestContainer() {
                this.fileService = new FileServiceImpl(null, null, null, null, null);
                this.clockHolder = new FakeClockHolder();
                this.merchantIdGenerator = new FakeMerchantIdGenerator();
                this.uuidHolder = new FakeUuidHolder("test-uuid-holder-value-1234567890");

                this.memberRepository = new FakeMemberRepository();
                this.subjectRepository = new FakeSubjectRepository();
                this.examRepository = new FakeExamRepository();
                this.problemRepository = new FakeProblemRepository();
                this.choiceRepository = new FakeChoiceRepository();
                this.bookmarkRepository = new FakeBookmarkRepository();

                this.memberMapper = new MemberMapper();
                this.memberService = new MemberServiceImpl(this.memberRepository, this.memberMapper);
                this.memberDetailsService = new MemberDetailsService(this.memberRepository);
                this.passwordEncoder = new BCryptPasswordEncoder();
                this.authenticationProvider = new CustomAuthenticationProvider(this.memberDetailsService,
                                this.passwordEncoder);
                this.jwtService = new JwtServiceImpl(FAKE_SECRET_KEY, this.clockHolder);
                this.memberValidator = new MemberValidatorImpl(this.memberRepository);
                this.authenticationService = new AuthenticationServiceImpl(this.passwordEncoder, this.jwtService,
                                this.memberRepository, this.authenticationProvider, this.memberMapper,
                                this.memberValidator, new RandomNicknameGenerator(), this.uuidHolder);
                this.subjectValidator = new SubjectValidatorImpl(this.subjectRepository);
                this.subjectMapper = new SubjectMapper();
                this.subjectService = new SubjectServiceImpl(this.subjectRepository, this.subjectMapper,
                                this.subjectValidator);
                this.subjectController = new SubjectController(subjectService);

                this.examMapper = new ExamMapper();
                this.examValidator = new ExamValidatorImpl(this.examRepository);
                this.examService = new ExamServiceImpl(this.examRepository, this.examMapper, this.subjectService,
                                this.examValidator);
                this.examController = new ExamController(examService);

                this.choiceMapper = new ChoiceMapperImpl();
                this.choiceService = new ChoiceServiceImpl(this.choiceRepository, this.choiceMapper);

                this.problemMapper = new ProblemMapper(this.choiceMapper);
                problemValidator = new ProblemValidator(this.problemRepository);
                ProblemContentProcessor problemContentProcessor = new ProblemContentProcessor(this.fileService,
                                "test-prefix");
                ProblemHtmlImageStorageService problemHtmlImageStorageService =
                                new ProblemHtmlImageStorageService(this.fileService, this.uuidHolder);
                this.youtubeLectureUrlProcessor = new YoutubeLectureUrlProcessor();
                this.problemService = new ProblemServiceImpl(this.problemRepository, this.problemMapper,
                                this.examService, this.choiceService, this.problemValidator,
                                problemContentProcessor, problemHtmlImageStorageService);
                this.createProblemV2Service = new CreateProblemV2ServiceImpl(
                                this.problemRepository,
                                this.examService,
                                this.choiceService,
                                this.problemValidator,
                                this.youtubeLectureUrlProcessor);
                this.getProblemV2Service = new GetProblemV2ServiceImpl(
                                this.problemRepository,
                                this.choiceService,
                                this.examService,
                                problemContentProcessor);

                this.questionRepository = new FakeQuestionRepository();
                this.questionMapper = new QuestionMapper();

                this.answerRepository = new FakeAnswerRepository();
                this.answerMapper = new AnswerMapper();

                this.questionAnswerService = new QuestionAnswerService(questionRepository);

                this.answerService = new AnswerServiceImpl(this.answerRepository, this.answerMapper,
                                this.questionAnswerService);

                this.questionService = new QuestionServiceImpl(questionRepository, problemService, questionMapper,
                                clockHolder);

                // bookmark
                this.createBookmarkService = new CreateBookmarkServiceImpl(
                                this.bookmarkRepository, this.memberRepository, this.problemRepository);
                this.deleteBookmarkService = new DeleteBookmarkServiceImpl(this.bookmarkRepository);
                this.getBookmarkedProblemsService = new GetBookmarkedProblemsServiceImpl(
                                this.bookmarkRepository, this.choiceService, problemContentProcessor);
        }

}
